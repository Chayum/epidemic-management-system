package com.epidemic.pandemic.config;

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
 * 用于消息推送系统的队列和交换机配置
 */
@Configuration
public class RabbitMQConfig {

    /** 交换机名称 */
    public static final String PUSH_EXCHANGE = "pandemic.push.exchange";

    /** 队列名称 */
    public static final String PUSH_QUEUE = "pandemic.push.queue";

    /** 路由键 */
    public static final String PUSH_ROUTING_KEY = "pandemic.push";

    /**
     * 创建交换机
     */
    @Bean
    public DirectExchange pushExchange() {
        return new DirectExchange(PUSH_EXCHANGE);
    }

    /**
     * 创建队列
     */
    @Bean
    public Queue pushQueue() {
        return new Queue(PUSH_QUEUE, true);
    }

    /**
     * 绑定队列到交换机
     */
    @Bean
    public Binding pushBinding(Queue pushQueue, DirectExchange pushExchange) {
        return BindingBuilder.bind(pushQueue).to(pushExchange).with(PUSH_ROUTING_KEY);
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
