package com.epidemic.material.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.epidemic.material.entity.InventoryLog;

import java.time.LocalDateTime;

import java.util.Map;

/**
 * 库存变动日志服务接口
 */
public interface InventoryLogService extends IService<InventoryLog> {

    /**
     * 获取今日出入库统计
     * @return Map containing "todayInbound" and "todayOutbound"
     */
    Map<String, Integer> getTodayStats();

    /**
     * 记录变动日志
     * @param materialId 物资ID
     * @param materialName 物资名称
     * @param type 变动类型
     * @param quantity 变动数量
     * @param before 变动前数量
     * @param after 变动后数量
     * @param relatedId 关联单号
     * @param operatorId 操作人ID
     * @param operatorName 操作人姓名
     * @param remark 备注
     */
    void log(String materialId, String materialName, String type, Integer quantity, Integer before, Integer after, String relatedId, Long operatorId, String operatorName, String remark);

    /**
     * 分页查询日志
     * @param page 页码
     * @param size 每页大小
     * @param materialId 物资ID
     * @param type 变动类型
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 分页结果
     */
    Page<InventoryLog> getLogList(Integer page, Integer size, String materialId, String type, LocalDateTime startTime, LocalDateTime endTime);
}
