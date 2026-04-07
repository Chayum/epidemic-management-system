package com.epidemic.pandemic.consumer;

import com.epidemic.common.mq.LogConstants;
import com.epidemic.common.result.Result;
import com.epidemic.pandemic.entity.PushMessage;
import com.epidemic.pandemic.feign.UserFeignClient;
import com.epidemic.pandemic.service.UserNotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 推送消息消费者
 * 从 RabbitMQ 队列中消费推送消息，异步发送通知
 */
@Component
@Slf4j
public class PushMessageConsumer {

    @Autowired
    private UserFeignClient userFeignClient;

    @Autowired
    private UserNotificationService userNotificationService;

    /**
     * 监听推送队列，处理推送消息
     */
    @RabbitListener(queues = LogConstants.PUSH_QUEUE)
    public void handlePushMessage(PushMessage pushMessage) {
        log.info("消费推送消息: pushRecordId={}, title={}, target={}",
                pushMessage.getPushRecordId(), pushMessage.getTitle(), pushMessage.getTarget());

        try {
            // 根据推送目标获取用户ID列表
            List<Long> userIds = getUserIdsByTarget(pushMessage.getTarget());

            if (userIds.isEmpty()) {
                log.warn("推送目标 {} 没有匹配的用户，跳过发送通知", pushMessage.getTarget());
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

        } catch (Exception e) {
            log.error("处理推送消息失败: pushRecordId={}, error={}",
                    pushMessage.getPushRecordId(), e.getMessage(), e);
            // 抛出异常让 MQ 重试
            throw e;
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
}
