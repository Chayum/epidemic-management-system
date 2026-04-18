package com.epidemic.user.consumer;

import com.epidemic.common.entity.OperateLog;
import com.epidemic.common.mq.LogConstants;
import com.epidemic.user.mapper.OperateLogMapper;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.concurrent.TimeUnit;

/**
 * 日志消息消费者
 * 从消息队列接收日志并保存到数据库
 * 实现手动确认和幂等性
 */
@Component
@Slf4j
public class LogConsumer {

    @Autowired
    private OperateLogMapper operateLogMapper;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    // 幂等性缓存 key 前缀
    private static final String LOG_IDEMPOTENT_KEY = "mq:log:idempotent:";
    // 幂等性缓存过期时间（小时）
    private static final long IDEMPOTENT_EXPIRE_HOURS = 24;

    /**
     * 监听日志队列，接收并保存日志
     * 手动确认模式，支持幂等性
     *
     * @param operateLog 操作日志实体
     * @param channel RabbitMQ 通道
     * @param deliveryTag 消息投递标签
     */
    @RabbitListener(queues = LogConstants.LOG_QUEUE)
    public void handleLogMessage(OperateLog operateLog, Channel channel,
                                 @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        // 生成消息幂等键（基于消息内容 hash）
        String messageKey = generateMessageKey(operateLog);
        String redisKey = LOG_IDEMPOTENT_KEY + messageKey;

        try {
            log.info("收到操作日志: module={}, operation={}, user={}, ip={}",
                    operateLog.getModule(), operateLog.getOperation(),
                    operateLog.getUsername(), operateLog.getIp());

            // 幂等性检查：使用 Redis setIfAbsent 实现原子性
            Boolean isNew = stringRedisTemplate.opsForValue()
                    .setIfAbsent(redisKey, "1", IDEMPOTENT_EXPIRE_HOURS, TimeUnit.HOURS);

            if (Boolean.FALSE.equals(isNew)) {
                log.warn("重复消息，跳过处理: messageKey={}", messageKey);
                // 重复消息也算处理成功，直接确认
                channel.basicAck(deliveryTag, false);
                return;
            }

            // 保存日志到数据库
            operateLogMapper.insert(operateLog);
            log.debug("操作日志保存成功: id={}", operateLog.getId());

            // 手动确认消息处理成功
            channel.basicAck(deliveryTag, false);

        } catch (Exception e) {
            log.error("保存操作日志失败: {}, operateLog={}", e.getMessage(), operateLog, e);
            try {
                // 处理失败，拒绝消息，requeue=false 让消息进入死信队列
                channel.basicNack(deliveryTag, false, false);
            } catch (Exception ex) {
                log.error("消息拒绝失败: {}", ex.getMessage(), ex);
            }
        }
    }

    /**
     * 生成消息幂等键
     * 基于消息内容生成唯一 hash，用于幂等性检查
     */
    private String generateMessageKey(OperateLog log) {
        // 组合关键字段作为唯一标识
        StringBuilder sb = new StringBuilder();
        if (log.getUserId() != null) {
            sb.append(log.getUserId());
        }
        sb.append(":").append(log.getModule());
        sb.append(":").append(log.getOperation());
        sb.append(":").append(log.getOperateTime());
        sb.append(":").append(log.getIp());

        // 使用 MD5 生成 hash 作为 key
        return md5Hash(sb.toString());
    }

    /**
     * MD5 hash 工具方法
     */
    private String md5Hash(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hashBytes = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            // 降级处理：直接返回原始字符串的 hashCode
            return String.valueOf(input.hashCode());
        }
    }
}