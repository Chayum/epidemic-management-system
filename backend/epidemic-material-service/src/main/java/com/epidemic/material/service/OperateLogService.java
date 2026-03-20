package com.epidemic.material.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.epidemic.material.entity.OperateLog;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 操作日志服务接口
 */
public interface OperateLogService extends IService<OperateLog> {

    /**
     * 获取最近操作日志
     * @param limit 返回条数
     * @return 操作日志列表
     */
    List<OperateLog> getRecentLogs(int limit);

    /**
     * 分页查询操作日志
     * @param page 页码
     * @param size 每页大小
     * @param username 用户名
     * @param module 模块
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 分页结果
     */
    Page<OperateLog> getLogList(Integer page, Integer size, String username, String module, LocalDateTime startTime, LocalDateTime endTime);
}