package com.epidemic.material.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.epidemic.material.entity.OperateLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 操作日志 Mapper
 */
@Mapper
public interface OperateLogMapper extends BaseMapper<OperateLog> {
}