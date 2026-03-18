package com.epidemic.pandemic.service;

import java.util.List;
import java.util.Map;

/**
 * 疫情服务缓存接口
 */
public interface PandemicCacheService {

    /**
     * 获取新闻列表缓存
     */
    List<Map<String, Object>> getNewsListCache(String status);

    /**
     * 设置新闻列表缓存
     */
    void setNewsListCache(String status, List<Map<String, Object>> newsList);

    /**
     * 清除新闻列表缓存
     */
    void deleteNewsListCache();

    /**
     * 获取推送统计缓存
     */
    List<Map<String, Object>> getPushStatsCache();

    /**
     * 设置推送统计缓存
     */
    void setPushStatsCache(List<Map<String, Object>> stats);

    /**
     * 清除推送统计缓存
     */
    void deletePushStatsCache();

    /**
     * 获取推送列表缓存
     */
    List<Map<String, Object>> getPushListCache();

    /**
     * 设置推送列表缓存
     */
    void setPushListCache(List<Map<String, Object>> pushList);

    /**
     * 清除推送列表缓存
     */
    void deletePushListCache();

    /**
     * 获取用户角色统计缓存
     */
    List<Map<String, Object>> getUserRoleStatsCache();

    /**
     * 设置用户角色统计缓存
     */
    void setUserRoleStatsCache(List<Map<String, Object>> stats);

    /**
     * 清除用户角色统计缓存
     */
    void deleteUserRoleStatsCache();
}