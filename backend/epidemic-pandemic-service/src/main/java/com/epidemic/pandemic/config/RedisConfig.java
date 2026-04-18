package com.epidemic.pandemic.config;

import com.epidemic.pandemic.subscriber.WarningSubscriber;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.util.ObjectUtils;

/**
 * Redis 配置类
 * 配置 RedisTemplate 序列化方式，使用安全的 Jackson 序列化器
 */
@Configuration
@Slf4j
public class RedisConfig {

    @Autowired
    private WarningSubscriber warningSubscriber;

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

        // 配置 ObjectMapper，使用安全的 Jackson 序列化器
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        // 不启用类型信息，避免反序列化安全风险，同时提高兼容性

        // 使用 Jackson2JsonRedisSerializer 作为 value 的序列化器
        Jackson2JsonRedisSerializer<Object> jsonSerializer = new Jackson2JsonRedisSerializer<>(objectMapper, Object.class);
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

    /**
     * Redis 消息监听容器
     * 用于订阅库存预警消息
     */
    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(RedisConnectionFactory connectionFactory) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);

        // 订阅库存预警频道
        container.addMessageListener(warningSubscriber, new PatternTopic("channel:inventory:warning"));

        log.info("Redis 消息监听容器已初始化，订阅频道：channel:inventory:warning");
        return container;
    }
}