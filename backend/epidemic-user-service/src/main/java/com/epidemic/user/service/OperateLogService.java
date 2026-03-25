package com.epidemic.user.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.epidemic.common.entity.OperateLog;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 操作日志服务接口
 */
public interface OperateLogService extends IService<OperateLog> {

    /**
     * 获取最近操作日志
     */
    List<OperateLog> getRecentLogs(int limit);

    /**
     * 分页查询操作日志
     */
    Page<OperateLog> getLogList(Integer page, Integer size, String username, String module,
                                LocalDateTime startTime, LocalDateTime endTime);
}