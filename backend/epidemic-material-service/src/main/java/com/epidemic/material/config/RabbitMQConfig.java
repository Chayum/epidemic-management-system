package com.epidemic.material.config;

import com.epidemic.common.mq.LogConstants;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ 配置类
 * 配置日志队列
 */
@Configuration
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