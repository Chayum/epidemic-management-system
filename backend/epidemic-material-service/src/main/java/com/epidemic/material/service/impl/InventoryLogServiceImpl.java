package com.epidemic.material.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.epidemic.material.entity.InventoryLog;
import com.epidemic.material.mapper.InventoryLogMapper;
import com.epidemic.material.service.CacheService;
import com.epidemic.material.service.InventoryLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
        Map<String, Integer> stats = new HashMap<>();
        LocalDateTime todayStart = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        
        // 统计今日入库
        List<InventoryLog> inboundLogs = list(new LambdaQueryWrapper<InventoryLog>()
                .eq(InventoryLog::getChangeType, "inbound")
                .ge(InventoryLog::getOperateTime, todayStart));
        int inboundCount = inboundLogs.stream().mapToInt(InventoryLog::getChangeQuantity).sum();
        stats.put("todayInbound", inboundCount);
        
        // 统计今日出库 (注意：出库记录的数量可能是负数，取决于记录逻辑，这里取绝对值)
        List<InventoryLog> outboundLogs = list(new LambdaQueryWrapper<InventoryLog>()
                .eq(InventoryLog::getChangeType, "outbound")
                .ge(InventoryLog::getOperateTime, todayStart));
        int outboundCount = outboundLogs.stream().mapToInt(l -> Math.abs(l.getChangeQuantity())).sum();
        stats.put("todayOutbound", outboundCount);
        
        // 3. 存入缓存
        cacheService.setTodayStats(stats);
        log.info("今日出入库统计已缓存");
        
        return stats;
    }

    @Override
    public void log(String materialId, String materialName, String type, Integer quantity, Integer before, Integer after, String relatedId, Long operatorId, String operatorName, String remark) {
        InventoryLog log = new InventoryLog();
        log.setMaterialId(materialId);
        log.setMaterialName(materialName);
        log.setChangeType(type);
        log.setChangeQuantity(quantity);
        log.setBeforeQuantity(before);
        log.setAfterQuantity(after);
        log.setRelatedId(relatedId);
        log.setOperatorId(operatorId);
        log.setOperatorName(operatorName);
        log.setRemark(remark);
        log.setOperateTime(LocalDateTime.now());
        save(log);
        
        // 清除今日出入库缓存
        cacheService.deleteTodayStats();
        log.debug("已清除今日出入库缓存");
    }

    @Override
    public Page<InventoryLog> getLogList(Integer page, Integer size, String materialId, String type, LocalDateTime startTime, LocalDateTime endTime) {
        Page<InventoryLog> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<InventoryLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(materialId != null, InventoryLog::getMaterialId, materialId)
               .eq(type != null, InventoryLog::getChangeType, type)
               .ge(startTime != null, InventoryLog::getOperateTime, startTime)
               .le(endTime != null, InventoryLog::getOperateTime, endTime)
               .orderByDesc(InventoryLog::getOperateTime);
        return page(pageParam, wrapper);
    }
}
