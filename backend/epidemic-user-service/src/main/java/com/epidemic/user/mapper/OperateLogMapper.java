package com.epidemic.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.epidemic.common.entity.OperateLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 操作日志 Mapper
 */
@Mapper
public interface OperateLogMapper extends BaseMapper<OperateLog> {
}