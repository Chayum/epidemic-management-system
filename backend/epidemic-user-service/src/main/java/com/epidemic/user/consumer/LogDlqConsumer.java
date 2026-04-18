package com.epidemic.user.consumer;

import com.epidemic.common.mq.LogConstants;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 日志死信队列消费者
 * 处理消费失败的消息，记录到错误日志
 */
@Component
@Slf4j
public class LogDlqConsumer {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 监听日志死信队列，处理失败消息
     *
     * @param message 原始消息
     * @param channel RabbitMQ 通道
     * @param deliveryTag 消息投递标签
     */
    @RabbitListener(queues = LogConstants.LOG_DLX_QUEUE)
    public void handleLogDlq(Message message, Channel channel,
                             @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        String messageBody = new String(message.getBody(), StandardCharsets.UTF_8);
        String timestamp = LocalDateTime.now().format(FORMATTER);

        log.error("=== 日志死信队列消息 ===");
        log.error("时间: {}", timestamp);
        log.error("消息体: {}", messageBody);
        log.error("消息属性: {}", message.getMessageProperties());
        log.error("异常原因: 消费失败，已超过重试次数");
        log.error("========================");

        try {
            // 确认消息，从死信队列中移除
            channel.basicAck(deliveryTag, false);
            log.info("死信消息已处理并确认");

        } catch (Exception e) {
            log.error("处理死信消息失败: {}", e.getMessage(), e);
            try {
                // 即使处理死信失败也确认，避免死循环
                channel.basicAck(deliveryTag, false);
            } catch (Exception ex) {
                log.error("死信消息确认失败: {}", ex.getMessage(), ex);
            }
        }
    }
}
