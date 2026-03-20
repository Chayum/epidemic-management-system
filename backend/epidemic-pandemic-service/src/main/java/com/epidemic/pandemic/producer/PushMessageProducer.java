package com.epidemic.pandemic.producer;

import com.epidemic.pandemic.config.RabbitMQConfig;
import com.epidemic.pandemic.entity.PushMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 推送消息生产者
 * 将推送任务发送到 RabbitMQ 队列
 */
@Component
@Slf4j
public class PushMessageProducer {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * 发送推送消息到队列
     *
     * @param pushMessage 推送消息
     */
    public void sendPushMessage(PushMessage pushMessage) {
        log.info("发送推送消息到MQ: pushRecordId={}, title={}, target={}",
                pushMessage.getPushRecordId(), pushMessage.getTitle(), pushMessage.getTarget());

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.PUSH_EXCHANGE,
                RabbitMQConfig.PUSH_ROUTING_KEY,
                pushMessage
        );

        log.info("推送消息已发送到队列: pushRecordId={}", pushMessage.getPushRecordId());
    }
}
