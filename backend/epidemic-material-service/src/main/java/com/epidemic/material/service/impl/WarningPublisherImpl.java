package com.epidemic.material.service.impl;

import com.epidemic.material.service.WarningPublisher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * 库存预警消息发布服务实现类
 * 通过 Redis 发布订阅机制将预警消息推送给订阅者
 */
@Service
@Slf4j
public class WarningPublisherImpl implements WarningPublisher {

    // 预警消息频道名称
    public static final String WARNING_CHANNEL = "channel:inventory:warning";

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public void publishWarning(String materialId, String materialName, Integer stock, Integer threshold) {
        try {
            // 构建预警消息
            Map<String, Object> message = new HashMap<>();
            message.put("materialId", materialId);
            message.put("materialName", materialName);
            message.put("stock", stock);
            message.put("threshold", threshold);
            message.put("warningLevel", calculateWarningLevel(stock, threshold));
            message.put("timestamp", System.currentTimeMillis());

            // 发布消息到 Redis 频道
            redisTemplate.convertAndSend(WARNING_CHANNEL, message);

            log.warn("发布库存预警消息：物资ID={}, 名称={}, 库存={}, 阈值={}",
                    materialId, materialName, stock, threshold);
        } catch (Exception e) {
            log.error("发布库存预警消息失败：{}", e.getMessage());
        }
    }

    /**
     * 计算预警等级
     */
    private String calculateWarningLevel(Integer stock, Integer threshold) {
        if (stock <= threshold * 0.3) {
            return "high"; // 高风险：库存低于阈值 30%
        } else if (stock <= threshold * 0.6) {
            return "medium"; // 中风险：库存低于阈值 60%
        } else {
            return "low"; // 低风险：库存低于阈值
        }
    }
}
