package com.epidemic.gateway.filter;

import com.epidemic.common.result.Result;
import com.epidemic.gateway.config.AuthConfig;
import com.epidemic.gateway.util.JwtUtil;
import io.jsonwebtoken.Claims;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

/**
 * 统一认证过滤器
 * 对请求进行身份验证，解析 Token 并传递用户信息
 * 集成 Redis 验证：检查 Token 是否在黑名单中、是否为用户当前有效 Token
 */
@Component
@Slf4j
public class AuthGlobalFilter implements GlobalFilter, Ordered {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private AuthConfig authConfig;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private final AntPathMatcher pathMatcher = new AntPathMatcher();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        // 1. 白名单放行
        if (isWhitelisted(path)) {
            return chain.filter(exchange);
        }

        // 2. 获取 Token
        String token = getToken(request);
        if (!StringUtils.hasText(token)) {
            return unauthorized(exchange, "未登录或 Token 为空");
        }

        // 3. JWT 基础验证（签名、过期时间）
        if (!jwtUtil.validateToken(token)) {
            return unauthorized(exchange, "Token 无效或已过期");
        }

        // 4. Redis 验证：检查是否在黑名单中
        String blacklistKey = "auth:blacklist:" + token;
        Boolean isBlacklisted = redisTemplate.hasKey(blacklistKey);
        if (Boolean.TRUE.equals(isBlacklisted)) {
            return unauthorized(exchange, "Token 已失效，请重新登录");
        }

        // 5. Redis 验证：检查是否为用户当前有效 Token
        try {
            Claims claims = jwtUtil.getClaimsFromToken(token);
            Long userId = claims.get("userId", Long.class);
            String username = claims.getSubject();
            String role = claims.get("role", String.class);

            // 从 Redis 获取当前用户的有效 Token
            String tokenKey = "auth:token:" + userId;
            String currentToken = (String) redisTemplate.opsForValue().get(tokenKey);

            // 如果 Redis 中没有该用户的 Token，说明用户从未登录或 Token 已被清除
            if (currentToken == null) {
                log.warn("Redis 中未找到用户 {} 的 Token，可能已被强制登出", username);
                return unauthorized(exchange, "Token 已失效，请重新登录");
            }

            // 比对 Redis 中的 Token 与请求中的 Token 是否一致
            if (!token.equals(currentToken)) {
                log.info("用户 {} 的 Token 不匹配，可能已在其他地方重新登录", username);
                return unauthorized(exchange, "Token 已过期，请重新登录");
            }

            // 6. 验证通过，将用户信息放入请求头，供下游微服务使用
            ServerHttpRequest mutatedRequest = request.mutate()
                    .header("X-User-Id", String.valueOf(userId))
                    .header("X-User-Name", username)
                    .header("X-User-Role", role)
                    .build();

            return chain.filter(exchange.mutate().request(mutatedRequest).build());
        } catch (Exception e) {
            log.error("Token 解析失败：{}", e.getMessage());
            return unauthorized(exchange, "Token 解析失败");
        }
    }

    /**
     *  判断是否在白名单中
     * @param path
     * @return
     */
    private boolean isWhitelisted(String path) {
        if (authConfig.getWhitelist() == null) {
            return false;
        }
        for (String pattern : authConfig.getWhitelist()) {
            if (pathMatcher.match(pattern, path)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 获取 Token
     *
     * */
    private String getToken(ServerHttpRequest request) {
        String authHeader = request.getHeaders().getFirst("Authorization");
        if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }

    /**
     * 未授权处理
     * @param exchange
     * @param message
     * @return
     */
    private Mono<Void> unauthorized(ServerWebExchange exchange, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        
        Result<String> result = Result.error(401, message);
        byte[] bytes;
        try {
            bytes = objectMapper.writeValueAsString(result).getBytes(StandardCharsets.UTF_8);
        } catch (JsonProcessingException e) {
            bytes = "{\"code\":401,\"message\":\"Unauthorized\"}".getBytes(StandardCharsets.UTF_8);
        }
        
        DataBuffer buffer = response.bufferFactory().wrap(bytes);
        return response.writeWith(Mono.just(buffer));
    }

    @Override
    public int getOrder() {
        return -100; // 优先级较高
    }
}
