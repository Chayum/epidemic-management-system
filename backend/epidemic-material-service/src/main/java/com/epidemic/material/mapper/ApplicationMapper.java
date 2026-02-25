package com.epidemic.material.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.epidemic.material.entity.Application;
import org.apache.ibatis.annotations.Mapper;

/**
 * 申请单Mapper接口
 */
@Mapper
public interface ApplicationMapper extends BaseMapper<Application> {
}
