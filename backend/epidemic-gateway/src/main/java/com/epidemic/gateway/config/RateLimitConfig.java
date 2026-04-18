package com.epidemic.gateway.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;

/**
 * 限流配置类
 * 从 application.yml 读取限流相关配置
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "rate-limit")
public class RateLimitConfig {

    // 是否启用限流
    private boolean enabled = true;

    // 全局限流：每秒最大请求数
    private int globalLimit = 1000;

    // 用户限流：每分钟最大请求数
    private int userLimit = 100;

    // IP 限流：每分钟最大请求数
    private int ipLimit = 200;

    // 白名单路径（不限流）
    private List<String> whitelist = Arrays.asList(
            "/api/auth/login",
            "/api/auth/register",
            "/doc.html",
            "/swagger-ui.html",
            "/webjars/**",
            "/v3/api-docs/**"
    );
}
