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
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

        // 计算入库趋势百分比（返回整数）
        // 特殊值约定：100 表示"新增"（昨天为0今天有数据），-100 表示"归零"（昨天有数据今天为0）
        int inboundTrend = 0;
        if (yesterdayInbound > 0 && todayInbound > 0) {
            // 正常计算增减百分比
            inboundTrend = (int) Math.round(((double) (todayInbound - yesterdayInbound) / yesterdayInbound) * 100);
        } else if (yesterdayInbound == 0 && todayInbound > 0) {
            inboundTrend = 100; // 新增
        } else if (yesterdayInbound > 0 && todayInbound == 0) {
            inboundTrend = -100; // 归零
        }
        trend.put("inboundTrend", inboundTrend);
        trend.put("inboundTrendType", inboundTrend >= 0 ? "up" : "down");

        // 计算出库趋势百分比（返回整数）
        int outboundTrend = 0;
        if (yesterdayOutbound > 0 && todayOutbound > 0) {
            outboundTrend = (int) Math.round(((double) (todayOutbound - yesterdayOutbound) / yesterdayOutbound) * 100);
        } else if (yesterdayOutbound == 0 && todayOutbound > 0) {
            outboundTrend = 100; // 新增
        } else if (yesterdayOutbound > 0 && todayOutbound == 0) {
            outboundTrend = -100; // 归零
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

        // 统计入库 (change_type = 'in')
        List<InventoryLog> inboundLogs = list(new LambdaQueryWrapper<InventoryLog>()
                .eq(InventoryLog::getChangeType, "in")
                .ge(InventoryLog::getOperateTime, dayStart)
                .le(InventoryLog::getOperateTime, dayEnd));
        int inboundCount = inboundLogs.stream().mapToInt(InventoryLog::getChangeQuantity).sum();
        stats.put("todayInbound", inboundCount);

        // 统计出库 (change_type = 'out')
        List<InventoryLog> outboundLogs = list(new LambdaQueryWrapper<InventoryLog>()
                .eq(InventoryLog::getChangeType, "out")
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

    @Override
    public Map<String, List<Map<String, Object>>> getTrendDataByDateRange(String startDate, String endDate) {
        Map<String, List<Map<String, Object>>> result = new HashMap<>();

        LocalDate start = LocalDate.parse(startDate);
        LocalDate end = LocalDate.parse(endDate);
        LocalDateTime startTime = start.atStartOfDay();
        LocalDateTime endTime = end.atTime(LocalTime.MAX);

        // 查询入库数据 (change_type = 'in')
        List<InventoryLog> inboundLogs = list(new LambdaQueryWrapper<InventoryLog>()
                .eq(InventoryLog::getChangeType, "in")
                .ge(InventoryLog::getOperateTime, startTime)
                .le(InventoryLog::getOperateTime, endTime));

        // 查询出库数据 (change_type = 'out')
        List<InventoryLog> outboundLogs = list(new LambdaQueryWrapper<InventoryLog>()
                .eq(InventoryLog::getChangeType, "out")
                .ge(InventoryLog::getOperateTime, startTime)
                .le(InventoryLog::getOperateTime, endTime));

        // 按日期分组统计
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        Map<String, Integer> inboundMap = inboundLogs.stream()
                .collect(Collectors.groupingBy(
                        log -> log.getOperateTime().format(formatter),
                        Collectors.summingInt(InventoryLog::getChangeQuantity)
                ));

        Map<String, Integer> outboundMap = outboundLogs.stream()
                .collect(Collectors.groupingBy(
                        log -> log.getOperateTime().format(formatter),
                        Collectors.summingInt(log -> Math.abs(log.getChangeQuantity()))
                ));

        // 转换为接口需要的格式
        List<Map<String, Object>> inboundList = new ArrayList<>();
        List<Map<String, Object>> outboundList = new ArrayList<>();

        for (Map.Entry<String, Integer> entry : inboundMap.entrySet()) {
            Map<String, Object> item = new HashMap<>();
            item.put("date", entry.getKey());
            item.put("count", entry.getValue());
            inboundList.add(item);
        }

        for (Map.Entry<String, Integer> entry : outboundMap.entrySet()) {
            Map<String, Object> item = new HashMap<>();
            item.put("date", entry.getKey());
            item.put("count", entry.getValue());
            outboundList.add(item);
        }

        result.put("inbound", inboundList);
        result.put("outbound", outboundList);

        return result;
    }
}
