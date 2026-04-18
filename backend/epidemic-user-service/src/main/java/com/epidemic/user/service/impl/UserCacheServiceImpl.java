package com.epidemic.user.service.impl;

import com.epidemic.user.entity.User;
import com.epidemic.user.mapper.UserMapper;
import com.epidemic.user.service.UserCacheService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * 用户缓存服务实现类
 * 提供用户信息的 Redis 缓存管理
 */
@Service
@Slf4j
public class UserCacheServiceImpl implements UserCacheService {

    // 缓存 Key 前缀
    private static final String CACHE_PREFIX = "user:info:";

    // 缓存过期时间：30 分钟
    private static final long CACHE_TTL = 30;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private UserMapper userMapper;

    @Override
    public User getUserById(Long userId) {
        if (userId == null) {
            return null;
        }

        String key = CACHE_PREFIX + userId;

        // 1. 尝试从缓存获取
        try {
            Object cached = redisTemplate.opsForValue().get(key);
            if (cached instanceof User) {
                log.debug("命中用户缓存：userId={}", userId);
                return (User) cached;
            }
        } catch (Exception e) {
            log.warn("读取用户缓存失败：userId={}，error={}", userId, e.getMessage());
        }

        // 2. 缓存未命中，从数据库查询
        User user = userMapper.selectById(userId);
        if (user == null) {
            return null;
        }

        // 3. 密码脱敏后存入缓存
        user.setPassword(null);

        try {
            redisTemplate.opsForValue().set(key, user, CACHE_TTL, TimeUnit.MINUTES);
            log.debug("用户信息已缓存：userId={}", userId);
        } catch (Exception e) {
            log.warn("缓存用户信息失败：userId={}，error={}", userId, e.getMessage());
        }

        return user;
    }

    @Override
    public void cacheUser(User user) {
        if (user == null || user.getId() == null) {
            return;
        }

        String key = CACHE_PREFIX + user.getId();

        // 密码脱敏
        User cacheUser = new User();
        cacheUser.setId(user.getId());
        cacheUser.setUsername(user.getUsername());
        cacheUser.setName(user.getName());
        cacheUser.setPhone(user.getPhone());
        cacheUser.setUnit(user.getUnit());
        cacheUser.setRole(user.getRole());
        cacheUser.setStatus(user.getStatus());
        cacheUser.setCreateTime(user.getCreateTime());
        // password 不缓存

        try {
            redisTemplate.opsForValue().set(key, cacheUser, CACHE_TTL, TimeUnit.MINUTES);
            log.info("用户信息已缓存：userId={}", user.getId());
        } catch (Exception e) {
            log.warn("缓存用户信息失败：userId={}，error={}", user.getId(), e.getMessage());
        }
    }

    @Override
    public void evictUser(Long userId) {
        if (userId == null) {
            return;
        }

        String key = CACHE_PREFIX + userId;

        try {
            redisTemplate.delete(key);
            log.info("用户缓存已清除：userId={}", userId);
        } catch (Exception e) {
            log.warn("清除用户缓存失败：userId={}，error={}", userId, e.getMessage());
        }
    }

    @Override
    public void evictAll() {
        try {
            // 扫描并删除所有用户缓存 Key
            Set<String> keys = redisTemplate.keys(CACHE_PREFIX + "*");
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
                log.info("已清除所有用户缓存，共 {} 条", keys.size());
            }
        } catch (Exception e) {
            log.warn("清除所有用户缓存失败：error={}", e.getMessage());
        }
    }
}
