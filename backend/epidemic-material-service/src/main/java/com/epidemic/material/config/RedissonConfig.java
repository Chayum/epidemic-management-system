package com.epidemic.material.config;

import lombok.extern.slf4j.Slf4j;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Redisson 配置类
 * 用于配置 Redisson 客户端，支持分布式锁等功能
 */
@Configuration
@Slf4j
public class RedissonConfig {

    @Value("${spring.data.redis.host:localhost}")
    private String redisHost;

    @Value("${spring.data.redis.port:6379}")
    private int redisPort;

    @Value("${spring.data.redis.password:}")
    private String redisPassword;

    @Value("${spring.data.redis.database:0}")
    private int database;

    @Bean
    public RedissonClient redissonClient() {
        Config config = new Config();

        // 配置单机模式
        String address = String.format("redis://%s:%d", redisHost, redisPort);
        config.useSingleServer()
              .setAddress(address)
              .setDatabase(database)
              .setConnectionMinimumIdleSize(5)
              .setConnectionPoolSize(10);

        // 如果有密码则设置密码
        if (redisPassword != null && !redisPassword.isEmpty()) {
            config.useSingleServer().setPassword(redisPassword);
            log.info("Redisson 配置：使用密码认证连接 Redis {}:{}", redisHost, redisPort);
        } else {
            log.info("Redisson 配置：使用无密码连接 Redis {}:{}", redisHost, redisPort);
        }

        return Redisson.create(config);
    }
}