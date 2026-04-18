package com.epidemic.common.config;

import com.epidemic.common.mq.LogConstants;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * RabbitMQ 配置类
 * 统一管理日志队列配置，供所有微服务共享
 * 只有当classpath中存在RabbitMQ相关类时才加载此配置
 */
@Configuration
@ConditionalOnClass(MessageConverter.class)
@ConditionalOnProperty(name = "spring.rabbitmq.host")
public class RabbitMQConfig {

    // ==================== 日志队列配置 ====================

    /**
     * 创建日志交换机
     */
    @Bean
    public DirectExchange logExchange() {
        return new DirectExchange(LogConstants.LOG_EXCHANGE);
    }

    /**
     * 创建日志队列（配置死信队列）
     */
    @Bean
    public Queue logQueue() {
        Map<String, Object> args = new HashMap<>();
        // 配置死信交换机
        args.put("x-dead-letter-exchange", LogConstants.LOG_DLX_EXCHANGE);
        // 配置死信路由键
        args.put("x-dead-letter-routing-key", LogConstants.LOG_DLX_ROUTING_KEY);
        return new Queue(LogConstants.LOG_QUEUE, true, false, false, args);
    }

    /**
     * 绑定日志队列到交换机
     */
    @Bean
    public Binding logBinding(Queue logQueue, DirectExchange logExchange) {
        return BindingBuilder.bind(logQueue).to(logExchange).with(LogConstants.LOG_ROUTING_KEY);
    }

    // ==================== 日志死信队列配置 ====================

    /**
     * 创建日志死信交换机
     */
    @Bean
    public DirectExchange logDlxExchange() {
        return new DirectExchange(LogConstants.LOG_DLX_EXCHANGE);
    }

    /**
     * 创建日志死信队列
     */
    @Bean
    public Queue logDlxQueue() {
        return new Queue(LogConstants.LOG_DLX_QUEUE, true);
    }

    /**
     * 绑定日志死信队列到死信交换机
     */
    @Bean
    public Binding logDlxBinding(Queue logDlxQueue, DirectExchange logDlxExchange) {
        return BindingBuilder.bind(logDlxQueue).to(logDlxExchange).with(LogConstants.LOG_DLX_ROUTING_KEY);
    }

    // ==================== 推送队列配置 ====================

    /**
     * 创建推送交换机
     */
    @Bean
    public DirectExchange pushExchange() {
        return new DirectExchange(LogConstants.PUSH_EXCHANGE);
    }

    /**
     * 创建推送队列（配置死信队列）
     */
    @Bean
    public Queue pushQueue() {
        Map<String, Object> args = new HashMap<>();
        // 配置死信交换机
        args.put("x-dead-letter-exchange", LogConstants.PUSH_DLX_EXCHANGE);
        // 配置死信路由键
        args.put("x-dead-letter-routing-key", LogConstants.PUSH_DLX_ROUTING_KEY);
        return new Queue(LogConstants.PUSH_QUEUE, true, false, false, args);
    }

    /**
     * 绑定推送队列到交换机
     */
    @Bean
    public Binding pushBinding(Queue pushQueue, DirectExchange pushExchange) {
        return BindingBuilder.bind(pushQueue).to(pushExchange).with(LogConstants.PUSH_ROUTING_KEY);
    }

    // ==================== 推送死信队列配置 ====================

    /**
     * 创建推送死信交换机
     */
    @Bean
    public DirectExchange pushDlxExchange() {
        return new DirectExchange(LogConstants.PUSH_DLX_EXCHANGE);
    }

    /**
     * 创建推送死信队列
     */
    @Bean
    public Queue pushDlxQueue() {
        return new Queue(LogConstants.PUSH_DLX_QUEUE, true);
    }

    /**
     * 绑定推送死信队列到死信交换机
     */
    @Bean
    public Binding pushDlxBinding(Queue pushDlxQueue, DirectExchange pushDlxExchange) {
        return BindingBuilder.bind(pushDlxQueue).to(pushDlxExchange).with(LogConstants.PUSH_DLX_ROUTING_KEY);
    }

    // ==================== 通用配置 ====================

    /**
     * JSON 消息转换器
     */
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /**
     * RabbitTemplate 配置
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }
}