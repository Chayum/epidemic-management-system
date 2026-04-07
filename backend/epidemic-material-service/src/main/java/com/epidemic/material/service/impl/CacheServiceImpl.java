package com.epidemic.material.service.impl;

import com.epidemic.material.service.CacheService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 缓存服务实现类
 * 提供各类业务数据的缓存操作
 */
@Service
@Slf4j
public class CacheServiceImpl implements CacheService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    // ==================== 物资统计缓存 ====================

    @Override
    public Map<String, Object> getMaterialStats() {
        String key = "material:stats:all";
        Object value = redisTemplate.opsForValue().get(key);
        if (value instanceof Map) {
            log.debug("命中物资统计缓存：{}", key);
            return (Map<String, Object>) value;
        }
        return null;
    }

    @Override
    public void setMaterialStats(Map<String, Object> stats) {
        String key = "material:stats:all";
        redisTemplate.opsForValue().set(key, stats, 5, TimeUnit.MINUTES);
        log.info("设置物资统计缓存：{}", key);
    }

    @Override
    public void deleteMaterialStats() {
        String key = "material:stats:all";
        redisTemplate.delete(key);
        log.info("删除物资统计缓存：{}", key);
    }

    // ==================== 库存预警缓存 ====================

    @Override
    public Object getWarningList() {
        String key = "material:warning:list";
        Object value = redisTemplate.opsForValue().get(key);
        if (value != null) {
            log.debug("命中库存预警缓存：{}", key);
        }
        return value;
    }

    @Override
    public void setWarningList(Object warningList) {
        String key = "material:warning:list";
        redisTemplate.opsForValue().set(key, warningList, 1, TimeUnit.MINUTES);
        log.info("设置库存预警缓存：{}", key);
    }

    @Override
    public void deleteWarningList() {
        String key = "material:warning:list";
        redisTemplate.delete(key);
        log.info("删除库存预警缓存：{}", key);
    }

    // ==================== 今日出入库缓存 ====================

    @Override
    public Map<String, Integer> getTodayStats() {
        String key = "material:today:stats:" + java.time.LocalDate.now();
        Object value = redisTemplate.opsForValue().get(key);
        if (value instanceof Map) {
            log.debug("命中今日出入库缓存：{}", key);
            return (Map<String, Integer>) value;
        }
        return null;
    }

    @Override
    public void setTodayStats(Map<String, Integer> stats) {
        String key = "material:today:stats:" + java.time.LocalDate.now();
        redisTemplate.opsForValue().set(key, stats, 1, TimeUnit.HOURS);
        log.info("设置今日出入库缓存：{}", key);
    }

    @Override
    public void deleteTodayStats() {
        String key = "material:today:stats:" + java.time.LocalDate.now();
        redisTemplate.delete(key);
        log.info("删除今日出入库缓存：{}", key);
    }

    // ==================== 热点配置缓存 ====================

    @Override
    public Object getMaterialTypes() {
        String key = "material:config:types";
        Object value = redisTemplate.opsForValue().get(key);
        if (value != null) {
            log.debug("命中物资类型缓存：{}", key);
        }
        return value;
    }

    @Override
    public void setMaterialTypes(Object types) {
        String key = "material:config:types";
        redisTemplate.opsForValue().set(key, types, 24, TimeUnit.HOURS);
        log.info("设置物资类型缓存：{}", key);
    }

    @Override
    public void deleteMaterialTypes() {
        String key = "material:config:types";
        redisTemplate.delete(key);
        log.info("删除物资类型缓存：{}", key);
    }

    @Override
    public Object getWarehouses() {
        String key = "material:config:warehouses";
        Object value = redisTemplate.opsForValue().get(key);
        if (value != null) {
            log.debug("命中仓库缓存：{}", key);
        }
        return value;
    }

    @Override
    public void setWarehouses(Object warehouses) {
        String key = "material:config:warehouses";
        redisTemplate.opsForValue().set(key, warehouses, 24, TimeUnit.HOURS);
        log.info("设置仓库缓存：{}", key);
    }

    @Override
    public void deleteWarehouses() {
        String key = "material:config:warehouses";
        redisTemplate.delete(key);
        log.info("删除仓库缓存：{}", key);
    }

    // ==================== 申请单状态缓存 ====================

    @Override
    public Object getApplicationStatus(String applicationId) {
        String key = "application:status:" + applicationId;
        Object value = redisTemplate.opsForValue().get(key);
        if (value != null) {
            log.debug("命中申请单状态缓存：{}", key);
        }
        return value;
    }

    @Override
    public void setApplicationStatus(String applicationId, Object status) {
        String key = "application:status:" + applicationId;
        // 申请单状态缓存 30 分钟
        redisTemplate.opsForValue().set(key, status, 30, TimeUnit.MINUTES);
        log.info("设置申请单状态缓存：{}", key);
    }

    @Override
    public void deleteApplicationStatus(String applicationId) {
        String key = "application:status:" + applicationId;
        redisTemplate.delete(key);
        log.info("删除申请单状态缓存：{}", key);
    }

    // ==================== 捐赠单状态缓存 ====================

    @Override
    public Object getDonationStatus(String donationId) {
        String key = "donation:status:" + donationId;
        Object value = redisTemplate.opsForValue().get(key);
        if (value != null) {
            log.debug("命中捐赠单状态缓存：{}", key);
        }
        return value;
    }

    @Override
    public void setDonationStatus(String donationId, Object status) {
        String key = "donation:status:" + donationId;
        // 捐赠单状态缓存 30 分钟
        redisTemplate.opsForValue().set(key, status, 30, TimeUnit.MINUTES);
        log.info("设置捐赠单状态缓存：{}", key);
    }

    @Override
    public void deleteDonationStatus(String donationId) {
        String key = "donation:status:" + donationId;
        redisTemplate.delete(key);
        log.info("删除捐赠单状态缓存：{}", key);
    }
}
