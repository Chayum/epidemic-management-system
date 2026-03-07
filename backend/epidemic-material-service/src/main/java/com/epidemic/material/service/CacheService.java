package com.epidemic.material.service;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 缓存服务接口
 * 提供各类业务数据的缓存操作
 */
public interface CacheService {

    /**
     * 获取物资统计数据
     * @return 统计数据
     */
    Map<String, Object> getMaterialStats();

    /**
     * 设置物资统计数据
     * @param stats 统计数据
     */
    void setMaterialStats(Map<String, Object> stats);

    /**
     * 删除物资统计缓存
     */
    void deleteMaterialStats();

    /**
     * 获取库存预警列表
     * @return 预警列表
     */
    Object getWarningList();

    /**
     * 设置库存预警列表
     * @param warningList 预警列表
     */
    void setWarningList(Object warningList);

    /**
     * 删除库存预警缓存
     */
    void deleteWarningList();

    /**
     * 获取今日出入库统计
     * @return 统计数据
     */
    Map<String, Integer> getTodayStats();

    /**
     * 设置今日出入库统计
     * @param stats 统计数据
     */
    void setTodayStats(Map<String, Integer> stats);

    /**
     * 删除今日出入库缓存
     */
    void deleteTodayStats();

    /**
     * 获取物资类型列表
     * @return 类型列表
     */
    Object getMaterialTypes();

    /**
     * 设置物资类型列表
     * @param types 类型列表
     */
    void setMaterialTypes(Object types);

    /**
     * 删除物资类型缓存
     */
    void deleteMaterialTypes();

    /**
     * 获取仓库列表
     * @return 仓库列表
     */
    Object getWarehouses();

    /**
     * 设置仓库列表
     * @param warehouses 仓库列表
     */
    void setWarehouses(Object warehouses);

    /**
     * 删除仓库缓存
     */
    void deleteWarehouses();

    /**
     * 获取申请单状态
     * @param applicationId 申请单 ID
     * @return 状态
     */
    Object getApplicationStatus(String applicationId);

    /**
     * 设置申请单状态
     * @param applicationId 申请单 ID
     * @param status 状态
     */
    void setApplicationStatus(String applicationId, Object status);

    /**
     * 删除申请单状态缓存
     * @param applicationId 申请单 ID
     */
    void deleteApplicationStatus(String applicationId);

    /**
     * 获取捐赠单状态
     * @param donationId 捐赠单 ID
     * @return 状态
     */
    Object getDonationStatus(String donationId);

    /**
     * 设置捐赠单状态
     * @param donationId 捐赠单 ID
     * @param status 状态
     */
    void setDonationStatus(String donationId, Object status);

    /**
     * 删除捐赠单状态缓存
     * @param donationId 捐赠单 ID
     */
    void deleteDonationStatus(String donationId);
}
