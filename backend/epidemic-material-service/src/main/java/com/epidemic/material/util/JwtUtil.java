package com.epidemic.material.util;

import cn.hutool.jwt.JWT;
import cn.hutool.jwt.JWTUtil;
import org.springframework.stereotype.Component;

/**
 * JWT工具类 (使用Hutool解析)
 */
@Component
public class JwtUtil {

    /**
     * 从token中获取用户ID
     */
    public Long getUserIdFromToken(String token) {
        if (token == null || token.isEmpty()) {
            return null;
        }
        
        // 移除 Bearer 前缀
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        
        try {
            // 使用 Hutool 解析 Token
            JWT jwt = JWTUtil.parseToken(token);
            Object userIdObj = jwt.getPayload("userId");
            if (userIdObj != null) {
                return Long.valueOf(userIdObj.toString());
            }
            return null;
        } catch (Exception e) {
            // 解析失败
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 从token中获取用户名
     */
    public String getUsernameFromToken(String token) {
        if (token == null || token.isEmpty()) {
            return null;
        }

        // 移除 Bearer 前缀
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        try {
            // 使用 Hutool 解析 Token
            JWT jwt = JWTUtil.parseToken(token);
            Object usernameObj = jwt.getPayload("username");
            if (usernameObj != null) {
                return usernameObj.toString();
            }
            // 尝试获取 sub
            Object subObj = jwt.getPayload("sub");
            if (subObj != null) {
                return subObj.toString();
            }
            return null;
        } catch (Exception e) {
            // 解析失败
            e.printStackTrace();
            return null;
        }
    }
}
