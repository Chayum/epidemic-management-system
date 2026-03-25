package com.epidemic.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.epidemic.common.entity.OperateLog;
import com.epidemic.user.mapper.OperateLogMapper;
import com.epidemic.user.service.OperateLogService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 操作日志服务实现
 */
@Service
public class OperateLogServiceImpl extends ServiceImpl<OperateLogMapper, OperateLog> implements OperateLogService {

    @Override
    public List<OperateLog> getRecentLogs(int limit) {
        LambdaQueryWrapper<OperateLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(OperateLog::getOperateTime)
               .last("LIMIT " + limit);
        return this.list(wrapper);
    }

    @Override
    public Page<OperateLog> getLogList(Integer page, Integer size, String username, String module,
                                       LocalDateTime startTime, LocalDateTime endTime) {
        Page<OperateLog> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<OperateLog> wrapper = new LambdaQueryWrapper<>();

        if (username != null && !username.isEmpty()) {
            wrapper.like(OperateLog::getUsername, username);
        }
        if (module != null && !module.isEmpty()) {
            wrapper.eq(OperateLog::getModule, module);
        }
        if (startTime != null) {
            wrapper.ge(OperateLog::getOperateTime, startTime);
        }
        if (endTime != null) {
            wrapper.le(OperateLog::getOperateTime, endTime);
        }

        wrapper.orderByDesc(OperateLog::getOperateTime);
        return this.page(pageParam, wrapper);
    }
}