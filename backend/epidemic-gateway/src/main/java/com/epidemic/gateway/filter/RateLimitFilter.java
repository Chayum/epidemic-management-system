package com.epidemic.gateway.filter;

import com.epidemic.gateway.config.RateLimitConfig;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.epidemic.common.result.Result;
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
import java.util.concurrent.TimeUnit;

/**
 * 分布式限流过滤器
 * 基于 Redis + 滑动窗口算法实现多级限流：
 * 1. 全局限流：保护系统整体稳定性
 * 2. 用户限流：防止单用户过度调用
 * 3. IP 限流：防止恶意攻击
 */
@Component
@Slf4j
public class RateLimitFilter implements GlobalFilter, Ordered {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private RateLimitConfig rateLimitConfig;

    private final AntPathMatcher pathMatcher = new AntPathMatcher();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // 限流未启用，直接放行
        if (!rateLimitConfig.isEnabled()) {
            return chain.filter(exchange);
        }

        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        // 白名单路径不限流
        if (isWhitelisted(path)) {
            return chain.filter(exchange);
        }

        // 1. 全局限流检查
        if (!checkGlobalLimit()) {
            log.warn("全局限流触发，路径：{}", path);
            return tooManyRequests(exchange, "系统繁忙，请稍后重试");
        }

        // 2. IP 限流检查
        String clientIp = getClientIp(request);
        if (!checkIpLimit(clientIp)) {
            log.warn("IP 限流触发，IP：{}，路径：{}", clientIp, path);
            return tooManyRequests(exchange, "请求过于频繁，请稍后重试");
        }

        // 3. 用户限流检查（仅对已登录用户）
        String userIdStr = request.getHeaders().getFirst("X-User-Id");
        if (StringUtils.hasText(userIdStr)) {
            if (!checkUserLimit(userIdStr)) {
                log.warn("用户限流触发，用户ID：{}，路径：{}", userIdStr, path);
                return tooManyRequests(exchange, "您的请求过于频繁，请稍后重试");
            }
        }

        return chain.filter(exchange);
    }

    /**
     * 检查全局限流
     * 使用滑动窗口算法，窗口大小 1 秒
     */
    private boolean checkGlobalLimit() {
        String key = "rate_limit:global";
        return checkRateLimit(key, rateLimitConfig.getGlobalLimit(), 1, TimeUnit.SECONDS);
    }

    /**
     * 检查 IP 限流
     * 使用滑动窗口算法，窗口大小 1 分钟
     */
    private boolean checkIpLimit(String clientIp) {
        String key = "rate_limit:ip:" + clientIp;
        return checkRateLimit(key, rateLimitConfig.getIpLimit(), 1, TimeUnit.MINUTES);
    }

    /**
     * 检查用户限流
     * 使用滑动窗口算法，窗口大小 1 分钟
     */
    private boolean checkUserLimit(String userId) {
        String key = "rate_limit:user:" + userId;
        return checkRateLimit(key, rateLimitConfig.getUserLimit(), 1, TimeUnit.MINUTES);
    }

    /**
     * 通用的限流检查方法
     * 基于 Redis INCR + EXPIRE 实现滑动窗口限流
     *
     * @param key     Redis key
     * @param limit   限制次数
     * @param period  时间窗口
     * @param unit    时间单位
     * @return true-允许访问，false-触发限流
     */
    private boolean checkRateLimit(String key, int limit, long period, TimeUnit unit) {
        try {
            // 获取当前计数
            Object countObj = redisTemplate.opsForValue().get(key);
            long currentCount = 0;

            if (countObj instanceof Number) {
                currentCount = ((Number) countObj).longValue();
            } else if (countObj instanceof String) {
                currentCount = Long.parseLong((String) countObj);
            }

            // 已超过限制
            if (currentCount >= limit) {
                return false;
            }

            // 计数器自增
            Long newCount = redisTemplate.opsForValue().increment(key);

            // 首次访问时设置过期时间
            if (newCount != null && newCount == 1) {
                redisTemplate.expire(key, period, unit);
            }

            return true;
        } catch (Exception e) {
            log.error("限流检查异常，放行请求：{}", e.getMessage());
            // Redis 异常时放行，避免影响正常业务
            return true;
        }
    }

    /**
     * 判断路径是否在白名单中
     */
    private boolean isWhitelisted(String path) {
        for (String pattern : rateLimitConfig.getWhitelist()) {
            if (pathMatcher.match(pattern, path)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 获取客户端真实 IP
     */
    private String getClientIp(ServerHttpRequest request) {
        String ip = request.getHeaders().getFirst("X-Forwarded-For");
        if (StringUtils.hasText(ip) && !"unknown".equalsIgnoreCase(ip)) {
            // 多次反向代理后会有多个IP值，第一个才是真实IP
            int index = ip.indexOf(',');
            if (index != -1) {
                return ip.substring(0, index).trim();
            }
            return ip.trim();
        }

        ip = request.getHeaders().getFirst("X-Real-IP");
        if (StringUtils.hasText(ip) && !"unknown".equalsIgnoreCase(ip)) {
            return ip.trim();
        }

        // 最后从 RemoteAddress 获取
        if (request.getRemoteAddress() != null) {
            return request.getRemoteAddress().getAddress().getHostAddress();
        }

        return "unknown";
    }

    /**
     * 返回 429 Too Many Requests 响应
     */
    private Mono<Void> tooManyRequests(ServerWebExchange exchange, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        Result<String> result = Result.error(429, message);
        byte[] bytes;
        try {
            bytes = objectMapper.writeValueAsString(result).getBytes(StandardCharsets.UTF_8);
        } catch (JsonProcessingException e) {
            bytes = "{\"code\":429,\"message\":\"Too Many Requests\"}".getBytes(StandardCharsets.UTF_8);
        }

        DataBuffer buffer = response.bufferFactory().wrap(bytes);
        return response.writeWith(Mono.just(buffer));
    }

    @Override
    public int getOrder() {
        // 在认证过滤器之前执行
        return -150;
    }
}
