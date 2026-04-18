package com.epidemic.pandemic.consumer;

import com.epidemic.common.mq.LogConstants;
import com.epidemic.common.result.Result;
import com.epidemic.pandemic.entity.PushMessage;
import com.epidemic.pandemic.feign.UserFeignClient;
import com.epidemic.pandemic.service.UserNotificationService;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 推送消息消费者
 * 从 RabbitMQ 队列中消费推送消息，异步发送通知
 * 实现手动确认和幂等性
 */
@Component
@Slf4j
public class PushMessageConsumer {

    @Autowired
    private UserFeignClient userFeignClient;

    @Autowired
    private UserNotificationService userNotificationService;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    // 幂等性缓存 key 前缀
    private static final String PUSH_IDEMPOTENT_KEY = "mq:push:idempotent:";
    // 幂等性缓存过期时间（小时）
    private static final long IDEMPOTENT_EXPIRE_HOURS = 24;

    /**
     * 监听推送队列，处理推送消息
     * 手动确认模式，支持幂等性
     *
     * @param pushMessage 推送消息实体
     * @param channel RabbitMQ 通道
     * @param deliveryTag 消息投递标签
     */
    @RabbitListener(queues = LogConstants.PUSH_QUEUE)
    public void handlePushMessage(PushMessage pushMessage, Channel channel,
                                   @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        log.info("消费推送消息: pushRecordId={}, title={}, target={}",
                pushMessage.getPushRecordId(), pushMessage.getTitle(), pushMessage.getTarget());

        // 生成消息幂等键
        String messageKey = generateMessageKey(pushMessage);
        String redisKey = PUSH_IDEMPOTENT_KEY + messageKey;

        try {
            // 幂等性检查：使用 Redis setIfAbsent 实现原子性
            Boolean isNew = stringRedisTemplate.opsForValue()
                    .setIfAbsent(redisKey, "1", IDEMPOTENT_EXPIRE_HOURS, TimeUnit.HOURS);

            if (Boolean.FALSE.equals(isNew)) {
                log.warn("重复推送消息，跳过处理: pushRecordId={}", pushMessage.getPushRecordId());
                // 重复消息也算处理成功，直接确认
                channel.basicAck(deliveryTag, false);
                return;
            }

            // 根据推送目标获取用户ID列表
            List<Long> userIds = getUserIdsByTarget(pushMessage.getTarget());

            if (userIds.isEmpty()) {
                log.warn("推送目标 {} 没有匹配的用户，跳过发送通知", pushMessage.getTarget());
                // 没有用户也算处理成功，直接确认
                channel.basicAck(deliveryTag, false);
                return;
            }

            log.info("为 {} 个用户创建通知", userIds.size());

            // 为目标用户创建通知
            userNotificationService.createNotifications(
                    userIds,
                    pushMessage.getTitle(),
                    pushMessage.getContent(),
                    "push",
                    pushMessage.getPushRecordId()
            );

            log.info("推送通知创建成功: pushRecordId={}, 通知数量={}",
                    pushMessage.getPushRecordId(), userIds.size());

            // 手动确认消息处理成功
            channel.basicAck(deliveryTag, false);

        } catch (Exception e) {
            log.error("处理推送消息失败: pushRecordId={}, error={}",
                    pushMessage.getPushRecordId(), e.getMessage(), e);
            try {
                // 处理失败，拒绝消息，requeue=false 让消息进入死信队列
                channel.basicNack(deliveryTag, false, false);
            } catch (Exception ex) {
                log.error("消息拒绝失败: {}", ex.getMessage(), ex);
            }
        }
    }

    /**
     * 根据推送目标获取用户ID列表
     */
    private List<Long> getUserIdsByTarget(String target) {
        try {
            Result<List<Long>> result;
            if (target == null || "all".equals(target)) {
                // 推送给所有用户（排除管理员）
                result = userFeignClient.getUserIdsByRole(null);
            } else {
                // 推送给指定角色的用户
                result = userFeignClient.getUserIdsByRole(target);
            }

            if (result != null && result.getData() != null) {
                return result.getData();
            }
        } catch (Exception e) {
            log.error("获取用户列表失败: target={}, error={}", target, e.getMessage());
        }
        return new ArrayList<>();
    }

    /**
     * 生成消息幂等键
     * 基于 pushRecordId 生成唯一标识
     */
    private String generateMessageKey(PushMessage pushMessage) {
        // 使用 pushRecordId 作为唯一标识
        return String.valueOf(pushMessage.getPushRecordId());
    }
}
