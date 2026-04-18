package com.epidemic.user.service;

import com.epidemic.user.entity.User;

/**
 * 用户缓存服务接口
 * 提供用户信息的 Redis 缓存操作
 */
public interface UserCacheService {

    /**
     * 根据用户ID获取用户信息（优先从缓存获取）
     *
     * @param userId 用户ID
     * @return 用户实体，不存在时返回null
     */
    User getUserById(Long userId);

    /**
     * 缓存用户信息
     *
     * @param user 用户实体
     */
    void cacheUser(User user);

    /**
     * 清除指定用户的缓存
     *
     * @param userId 用户ID
     */
    void evictUser(Long userId);

    /**
     * 清除所有用户缓存
     */
    void evictAll();
}
