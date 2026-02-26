package com.epidemic.material.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.epidemic.material.entity.Application;
import org.apache.ibatis.annotations.Mapper;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;
import java.util.Map;

/**
 * 申请单Mapper接口
 */
@Mapper
public interface ApplicationMapper extends BaseMapper<Application> {

    @Select("SELECT DATE_FORMAT(approve_time, '%Y-%m-%d') as date, COUNT(*) as count, SUM(quantity) as quantity " +
            "FROM application " +
            "WHERE status = 'approved' AND approve_time >= #{startDate} " +
            "GROUP BY DATE_FORMAT(approve_time, '%Y-%m-%d')")
    List<Map<String, Object>> countApprovedByDate(@Param("startDate") String startDate);
}
