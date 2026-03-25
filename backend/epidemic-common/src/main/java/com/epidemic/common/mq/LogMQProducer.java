package com.epidemic.common.mq;

import com.epidemic.common.entity.OperateLog;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

/**
 * 日志消息生产者
 * 负责将操作日志发送到消息队列
 */
@Component
public class LogMQProducer {

    private final RabbitTemplate rabbitTemplate;

    public LogMQProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    /**
     * 发送日志消息到队列
     */
    public void sendLog(OperateLog operateLog) {
        rabbitTemplate.convertAndSend(
                LogConstants.LOG_EXCHANGE,
                LogConstants.LOG_ROUTING_KEY,
                operateLog
        );
    }
}