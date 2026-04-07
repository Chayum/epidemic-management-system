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

/**
 * RabbitMQ 配置类
 * 统一管理日志队列配置，供所有微服务共享
 * 只有当classpath中存在RabbitMQ相关类时才加载此配置
 */
@Configuration
@ConditionalOnClass(MessageConverter.class)
@ConditionalOnProperty(name = "spring.rabbitmq.host")
public class RabbitMQConfig {

    /**
     * 创建交换机
     */
    @Bean
    public DirectExchange logExchange() {
        return new DirectExchange(LogConstants.LOG_EXCHANGE);
    }

    /**
     * 创建队列
     */
    @Bean
    public Queue logQueue() {
        return new Queue(LogConstants.LOG_QUEUE, true);
    }

    /**
     * 绑定队列到交换机
     */
    @Bean
    public Binding logBinding(Queue logQueue, DirectExchange logExchange) {
        return BindingBuilder.bind(logQueue).to(logExchange).with(LogConstants.LOG_ROUTING_KEY);
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
     * 创建推送队列
     */
    @Bean
    public Queue pushQueue() {
        return new Queue(LogConstants.PUSH_QUEUE, true);
    }

    /**
     * 绑定推送队列到交换机
     */
    @Bean
    public Binding pushBinding(Queue pushQueue, DirectExchange pushExchange) {
        return BindingBuilder.bind(pushQueue).to(pushExchange).with(LogConstants.PUSH_ROUTING_KEY);
    }

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