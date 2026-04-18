package com.epidemic.material.service;

/**
 * 库存预警消息发布服务接口
 * 通过 Redis 发布订阅机制推送预警消息
 */
public interface WarningPublisher {

    /**
     * 发布库存预警消息
     *
     * @param materialId   物资ID
     * @param materialName 物资名称
     * @param stock        当前库存
     * @param threshold    预警阈值
     */
    void publishWarning(String materialId, String materialName, Integer stock, Integer threshold);
}
