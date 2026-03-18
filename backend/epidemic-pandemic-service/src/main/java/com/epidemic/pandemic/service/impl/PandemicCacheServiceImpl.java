package com.epidemic.pandemic.service.impl;

import com.epidemic.pandemic.service.PandemicCacheService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 疫情服务缓存实现类
 */
@Service
@Slf4j
public class PandemicCacheServiceImpl implements PandemicCacheService {

    private static final String NEWS_LIST_KEY_PREFIX = "pandemic:news:list:";
    private static final String PUSH_STATS_KEY = "pandemic:push:stats";
    private static final String PUSH_LIST_KEY = "pandemic:push:list";
    private static final String USER_ROLE_STATS_KEY = "pandemic:user:role:stats";

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    // ==================== 新闻列表缓存 ====================

    @Override
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> getNewsListCache(String status) {
        String key = NEWS_LIST_KEY_PREFIX + (status != null ? status : "all");
        Object value = redisTemplate.opsForValue().get(key);
        if (value instanceof List) {
            log.debug("命中新闻列表缓存：{}", key);
            return (List<Map<String, Object>>) value;
        }
        return null;
    }

    @Override
    public void setNewsListCache(String status, List<Map<String, Object>> newsList) {
        String key = NEWS_LIST_KEY_PREFIX + (status != null ? status : "all");
        redisTemplate.opsForValue().set(key, newsList, 5, TimeUnit.MINUTES);
        log.info("设置新闻列表缓存：{}", key);
    }

    @Override
    public void deleteNewsListCache() {
        // 清除所有新闻列表缓存
        redisTemplate.delete(NEWS_LIST_KEY_PREFIX + "all");
        redisTemplate.delete(NEWS_LIST_KEY_PREFIX + "published");
        redisTemplate.delete(NEWS_LIST_KEY_PREFIX + "draft");
        log.info("清除新闻列表缓存");
    }

    // ==================== 推送统计缓存 ====================

    @Override
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> getPushStatsCache() {
        Object value = redisTemplate.opsForValue().get(PUSH_STATS_KEY);
        if (value instanceof List) {
            log.debug("命中推送统计缓存：{}", PUSH_STATS_KEY);
            return (List<Map<String, Object>>) value;
        }
        return null;
    }

    @Override
    public void setPushStatsCache(List<Map<String, Object>> stats) {
        redisTemplate.opsForValue().set(PUSH_STATS_KEY, stats, 5, TimeUnit.MINUTES);
        log.info("设置推送统计缓存：{}", PUSH_STATS_KEY);
    }

    @Override
    public void deletePushStatsCache() {
        redisTemplate.delete(PUSH_STATS_KEY);
        log.info("清除推送统计缓存");
    }

    // ==================== 推送列表缓存 ====================

    @Override
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> getPushListCache() {
        Object value = redisTemplate.opsForValue().get(PUSH_LIST_KEY);
        if (value instanceof List) {
            log.debug("命中推送列表缓存：{}", PUSH_LIST_KEY);
            return (List<Map<String, Object>>) value;
        }
        return null;
    }

    @Override
    public void setPushListCache(List<Map<String, Object>> pushList) {
        redisTemplate.opsForValue().set(PUSH_LIST_KEY, pushList, 5, TimeUnit.MINUTES);
        log.info("设置推送列表缓存：{}", PUSH_LIST_KEY);
    }

    @Override
    public void deletePushListCache() {
        redisTemplate.delete(PUSH_LIST_KEY);
        log.info("清除推送列表缓存");
    }

    // ==================== 用户角色统计缓存 ====================

    @Override
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> getUserRoleStatsCache() {
        Object value = redisTemplate.opsForValue().get(USER_ROLE_STATS_KEY);
        if (value instanceof List) {
            log.debug("命中用户角色统计缓存：{}", USER_ROLE_STATS_KEY);
            return (List<Map<String, Object>>) value;
        }
        return null;
    }

    @Override
    public void setUserRoleStatsCache(List<Map<String, Object>> stats) {
        redisTemplate.opsForValue().set(USER_ROLE_STATS_KEY, stats, 30, TimeUnit.MINUTES);
        log.info("设置用户角色统计缓存：{}", USER_ROLE_STATS_KEY);
    }

    @Override
    public void deleteUserRoleStatsCache() {
        redisTemplate.delete(USER_ROLE_STATS_KEY);
        log.info("清除用户角色统计缓存");
    }
}