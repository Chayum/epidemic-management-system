package com.epidemic.material.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.epidemic.material.entity.ApplicationTrack;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 申请物流追踪 Mapper 接口
 */
@Mapper
public interface ApplicationTrackMapper extends BaseMapper<ApplicationTrack> {

    /**
     * 根据申请单号查询追踪记录
     * @param applicationId 申请单号
     * @return 追踪记录列表
     */
    @Select("SELECT * FROM application_track WHERE application_id = #{applicationId} ORDER BY operate_time ASC")
    List<ApplicationTrack> selectByApplicationId(String applicationId);
}
