package com.epidemic.gateway.filter;

import com.epidemic.gateway.util.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.factory.rewrite.RewriteFunction;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
@Slf4j
/**
 * 登录响应处理过滤器
 * 用于生成Token并重写响应体
 */
public class LoginResponseRewriteFunction implements RewriteFunction<Map, Map> {

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public Publisher<Map> apply(ServerWebExchange exchange, Map body) {
        try {
            // 检查响应状态和内容
            if (body == null || !body.containsKey("code")) {
                return Mono.justOrEmpty(body);
            }

            Integer code = (Integer) body.get("code");
            if (code != 200) {
                return Mono.just(body);
            }

            // 获取用户数据
            Object dataObj = body.get("data");
            if (dataObj == null) {
                return Mono.just(body);
            }

            // dataObj 应该是 Map (LinkedHashMap for Jackson)
            if (!(dataObj instanceof Map)) {
                log.warn("Login response data is not a Map: {}", dataObj.getClass());
                return Mono.just(body);
            }

            Map<String, Object> userMap = (Map<String, Object>) dataObj;
            
            // 提取用户信息生成Token
            // 注意：数字类型在反序列化时可能是 Integer 或 Long
            Object idObj = userMap.get("id");
            Long userId = null;
            if (idObj instanceof Integer) {
                userId = ((Integer) idObj).longValue();
            } else if (idObj instanceof Long) {
                userId = (Long) idObj;
            }

            String username = (String) userMap.get("username");
            String role = (String) userMap.get("role");

            if (userId != null && username != null) {
                String token = jwtUtil.generateToken(userId, username, role);
                
                // 构建 LoginResponse 结构
                Map<String, Object> loginResponse = new LinkedHashMap<>();
                loginResponse.put("token", token);
                loginResponse.put("userInfo", userMap);

                // 替换 data
                Map<String, Object> newBody = new LinkedHashMap<>(body);
                newBody.put("data", loginResponse);
                
                log.info("Token generated for user: {}", username);
                return Mono.just(newBody);
            }
            
            return Mono.just(body);

        } catch (Exception e) {
            log.error("Error rewriting login response", e);
            return Mono.just(body);
        }
    }
}
