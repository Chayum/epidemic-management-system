package com.epidemic.user.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.util.ObjectUtils;

/**
 * Redis 配置类
 * 配置 RedisTemplate 序列化方式，方便存储和读取数据
 */
@Configuration
@Slf4j
public class RedisConfig {

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        if (ObjectUtils.isEmpty(connectionFactory)) {
            throw new IllegalStateException("RedisConnectionFactory cannot be null");
        }

        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(connectionFactory);

        // 使用 String 序列化器作为 key 的序列化器
        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        redisTemplate.setKeySerializer(stringSerializer);
        redisTemplate.setHashKeySerializer(stringSerializer);

        // 使用 JSON 序列化器作为 value 的序列化器
        // 注意：GenericJackson2JsonRedisSerializer 可反序列化任意类型，
        // 确保 Redis 服务安全，不暴露在公网，并启用认证
        GenericJackson2JsonRedisSerializer jsonSerializer = new GenericJackson2JsonRedisSerializer();
        redisTemplate.setValueSerializer(jsonSerializer);
        redisTemplate.setHashValueSerializer(jsonSerializer);

        try {
            redisTemplate.afterPropertiesSet();
        } catch (Exception e) {
            log.error("Failed to initialize RedisTemplate", e);
            throw e;
        }

        return redisTemplate;
    }
}