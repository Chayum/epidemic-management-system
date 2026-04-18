package com.epidemic.pandemic.subscriber;

import com.epidemic.common.result.Result;
import com.epidemic.pandemic.feign.UserFeignClient;
import com.epidemic.pandemic.service.UserNotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * 库存预警消息订阅者
 * 监听 Redis 频道，接收预警消息并创建通知
 */
@Component
@Slf4j
public class WarningSubscriber implements MessageListener {

    @Autowired
    private UserNotificationService userNotificationService;

    @Autowired
    private UserFeignClient userFeignClient;

    // 用于反序列化消息的 ObjectMapper
    private final com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            String channel = new String(message.getChannel());
            String body = new String(message.getBody());

            log.info("收到库存预警消息，频道：{}，内容：{}", channel, body);

            // 解析预警消息
            @SuppressWarnings("unchecked")
            Map<String, Object> warningData = objectMapper.readValue(body, Map.class);

            String materialId = (String) warningData.get("materialId");
            String materialName = (String) warningData.get("materialName");
            Integer stock = ((Number) warningData.get("stock")).intValue();
            Integer threshold = ((Number) warningData.get("threshold")).intValue();
            String warningLevel = (String) warningData.get("warningLevel");

            // 构建预警通知内容
            String title = "库存预警通知";
            String content = String.format(
                    "物资【%s】库存不足！当前库存：%d，预警阈值：%d，风险等级：%s",
                    materialName, stock, threshold, getWarningLevelText(warningLevel)
            );

            // 获取所有管理员用户ID
            Result<List<Long>> result = userFeignClient.getUserIdsByRole("admin");
            if (result != null && result.getCode() == 200 && result.getData() != null && !result.getData().isEmpty()) {
                // 创建用户通知记录
                userNotificationService.createNotifications(
                        result.getData(),
                        title,
                        content,
                        "warning",
                        null // pushRecordId 为 null
                );
                log.info("库存预警通知已创建：物资={}，等级={}，通知用户数={}", materialName, warningLevel, result.getData().size());
            } else {
                log.warn("未找到管理员用户，无法发送预警通知");
            }

        } catch (Exception e) {
            log.error("处理库存预警消息失败：{}", e.getMessage(), e);
        }
    }

    /**
     * 获取预警等级文本
     */
    private String getWarningLevelText(String level) {
        if (level == null) {
            return "未知";
        }
        switch (level) {
            case "high":
                return "高风险";
            case "medium":
                return "中风险";
            case "low":
                return "低风险";
            default:
                return "未知";
        }
    }
}
