package com.epidemic.pandemic.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.epidemic.pandemic.entity.PushRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * 推送记录Mapper接口
 */
@Mapper
public interface PushRecordMapper extends BaseMapper<PushRecord> {
}
