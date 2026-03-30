package com.epidemic.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.epidemic.common.dto.OperateLogQueryDTO;
import com.epidemic.common.entity.OperateLog;
import com.epidemic.user.mapper.OperateLogMapper;
import com.epidemic.user.service.OperateLogService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

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
    public Page<OperateLog> getLogList(OperateLogQueryDTO queryDTO) {
        Page<OperateLog> pageParam = new Page<>(queryDTO.getPage(), queryDTO.getSize());
        LambdaQueryWrapper<OperateLog> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(queryDTO.getUsername())) {
            wrapper.like(OperateLog::getUsername, queryDTO.getUsername());
        }
        if (StringUtils.hasText(queryDTO.getModule())) {
            wrapper.eq(OperateLog::getModule, queryDTO.getModule());
        }
        if (queryDTO.getStartTime() != null) {
            wrapper.ge(OperateLog::getOperateTime, queryDTO.getStartTime());
        }
        if (queryDTO.getEndTime() != null) {
            wrapper.le(OperateLog::getOperateTime, queryDTO.getEndTime());
        }

        wrapper.orderByDesc(OperateLog::getOperateTime);
        return this.page(pageParam, wrapper);
    }
}