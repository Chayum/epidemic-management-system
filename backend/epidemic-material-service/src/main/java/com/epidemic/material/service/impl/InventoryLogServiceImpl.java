package com.epidemic.material.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.epidemic.material.dto.InventoryLogQueryDTO;
import com.epidemic.material.entity.InventoryLog;
import com.epidemic.material.mapper.InventoryLogMapper;
import com.epidemic.material.service.CacheService;
import com.epidemic.material.service.InventoryLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 库存变动日志服务实现类
 */
@Service
@Slf4j
public class InventoryLogServiceImpl extends ServiceImpl<InventoryLogMapper, InventoryLog> implements InventoryLogService {

    @Autowired
    private CacheService cacheService;

    @Override
    public Map<String, Integer> getTodayStats() {
        // 1. 尝试从缓存获取
        Map<String, Integer> cachedStats = cacheService.getTodayStats();
        if (cachedStats != null) {
            log.debug("从缓存获取今日出入库统计");
            return cachedStats;
        }

        // 2. 缓存未命中，从数据库查询
        Map<String, Integer> stats = queryStatsForDate(LocalDate.now());
        cacheService.setTodayStats(stats);
        log.info("今日出入库统计已缓存");

        return stats;
    }

    @Override
    public Map<String, Integer> getYesterdayStats() {
        return queryStatsForDate(LocalDate.now().minusDays(1));
    }

    @Override
    public Map<String, Object> getStatsTrend() {
        Map<String, Object> trend = new HashMap<>();
        Map<String, Integer> todayStats = getTodayStats();
        Map<String, Integer> yesterdayStats = getYesterdayStats();

        int todayInbound = todayStats.getOrDefault("todayInbound", 0);
        int yesterdayInbound = yesterdayStats.getOrDefault("todayInbound", 0);
        int todayOutbound = todayStats.getOrDefault("todayOutbound", 0);
        int yesterdayOutbound = yesterdayStats.getOrDefault("todayOutbound", 0);

        // 计算入库趋势百分比
        double inboundTrend = 0;
        if (yesterdayInbound > 0) {
            inboundTrend = Math.round(((double) (todayInbound - yesterdayInbound) / yesterdayInbound) * 100);
        } else if (todayInbound > 0) {
            inboundTrend = 100; // 昨天为0今天有数据，视为增长100%
        }
        trend.put("inboundTrend", inboundTrend);
        trend.put("inboundTrendType", inboundTrend >= 0 ? "up" : "down");

        // 计算出库趋势百分比
        double outboundTrend = 0;
        if (yesterdayOutbound > 0) {
            outboundTrend = Math.round(((double) (todayOutbound - yesterdayOutbound) / yesterdayOutbound) * 100);
        } else if (todayOutbound > 0) {
            outboundTrend = 100;
        }
        trend.put("outboundTrend", outboundTrend);
        trend.put("outboundTrendType", outboundTrend >= 0 ? "up" : "down");

        return trend;
    }

    /**
     * 查询指定日期的出入库统计
     */
    private Map<String, Integer> queryStatsForDate(LocalDate date) {
        Map<String, Integer> stats = new HashMap<>();
        LocalDateTime dayStart = LocalDateTime.of(date, LocalTime.MIN);
        LocalDateTime dayEnd = LocalDateTime.of(date, LocalTime.MAX);

        // 统计入库
        List<InventoryLog> inboundLogs = list(new LambdaQueryWrapper<InventoryLog>()
                .eq(InventoryLog::getChangeType, "inbound")
                .ge(InventoryLog::getOperateTime, dayStart)
                .le(InventoryLog::getOperateTime, dayEnd));
        int inboundCount = inboundLogs.stream().mapToInt(InventoryLog::getChangeQuantity).sum();
        stats.put("todayInbound", inboundCount);

        // 统计出库
        List<InventoryLog> outboundLogs = list(new LambdaQueryWrapper<InventoryLog>()
                .eq(InventoryLog::getChangeType, "outbound")
                .ge(InventoryLog::getOperateTime, dayStart)
                .le(InventoryLog::getOperateTime, dayEnd));
        int outboundCount = outboundLogs.stream().mapToInt(l -> Math.abs(l.getChangeQuantity())).sum();
        stats.put("todayOutbound", outboundCount);

        return stats;
    }

    @Override
    public void log(String materialId, String materialName, String type, Integer quantity, Integer before, Integer after, String relatedId, Long operatorId, String operatorName, String remark) {
        InventoryLog inventoryLog = new InventoryLog();
        inventoryLog.setMaterialId(materialId);
        inventoryLog.setMaterialName(materialName);
        inventoryLog.setChangeType(type);
        inventoryLog.setChangeQuantity(quantity);
        inventoryLog.setBeforeQuantity(before);
        inventoryLog.setAfterQuantity(after);
        inventoryLog.setRelatedId(relatedId);
        inventoryLog.setOperatorId(operatorId);
        inventoryLog.setOperatorName(operatorName);
        inventoryLog.setRemark(remark);
        inventoryLog.setOperateTime(LocalDateTime.now());
        save(inventoryLog);
        
        // 清除今日出入库缓存
        cacheService.deleteTodayStats();
        log.debug("已清除今日出入库缓存");
    }

    @Override
    public Page<InventoryLog> getLogList(InventoryLogQueryDTO queryDTO) {
        Page<InventoryLog> pageParam = new Page<>(queryDTO.getPage(), queryDTO.getSize());
        LambdaQueryWrapper<InventoryLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StringUtils.hasText(queryDTO.getMaterialId()), InventoryLog::getMaterialId, queryDTO.getMaterialId())
               .eq(StringUtils.hasText(queryDTO.getType()), InventoryLog::getChangeType, queryDTO.getType())
               .ge(queryDTO.getStartTime() != null, InventoryLog::getOperateTime, queryDTO.getStartTime())
               .le(queryDTO.getEndTime() != null, InventoryLog::getOperateTime, queryDTO.getEndTime())
               .orderByDesc(InventoryLog::getOperateTime);
        return page(pageParam, wrapper);
    }
}
