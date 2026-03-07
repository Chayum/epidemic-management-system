package com.epidemic.gateway.filter;

import com.epidemic.gateway.util.JwtUtil;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.concurrent.TimeUnit;

/**
 * 登出响应处理函数
 * 用于删除 Redis 中的 Token 或将 Token 加入黑名单
 */
@Component
@Slf4j
public class LogoutResponseRewriteFunction {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private org.springframework.data.redis.core.RedisTemplate<String, Object> redisTemplate;

    public Mono<Void> apply(ServerWebExchange exchange) {
        try {
            // 从请求头获取 Token
            String authHeader = exchange.getRequest().getHeaders().getFirst("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                
                // 解析 Token 获取用户 ID
                try {
                    Claims claims = jwtUtil.getClaimsFromToken(token);
                    Long userId = claims.get("userId", Long.class);
                    String username = claims.getSubject();
                    
                    // 方案 A：删除 Redis 中的 Token
                    String tokenKey = "auth:token:" + userId;
                    redisTemplate.delete(tokenKey);
                    log.info("用户 {} 登出成功，已删除 Redis 中的 Token", username);
                    
                    // 方案 B：将 Token 加入黑名单（直到原 Token 自然过期）
                    // long remainingTime = jwtUtil.getRemainingTime(token);
                    // String blacklistKey = "auth:blacklist:" + token;
                    // redisTemplate.opsForValue().set(blacklistKey, "1", remainingTime, TimeUnit.SECONDS);
                    
                } catch (Exception e) {
                    log.warn("Token 解析失败，但仍允许登出：{}", e.getMessage());
                }
            }
        } catch (Exception e) {
            log.error("处理登出请求失败", e);
        }
        
        return Mono.empty();
    }
}
