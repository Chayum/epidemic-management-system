package com.epidemic.material.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.epidemic.material.entity.Donation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Map;

/**
 * 捐赠Mapper接口
 */
@Mapper
public interface DonationMapper extends BaseMapper<Donation> {

    @Select("SELECT IFNULL(SUM(quantity), 0) FROM donation WHERE status = 'approved'")
    Integer sumApprovedQuantity();

    @Select("SELECT DATE_FORMAT(approve_time, '%Y-%m-%d') as date, COUNT(*) as count, SUM(quantity) as quantity " +
            "FROM donation " +
            "WHERE status = 'approved' AND approve_time >= #{startDate} " +
            "GROUP BY DATE_FORMAT(approve_time, '%Y-%m-%d')")
    List<Map<String, Object>> countApprovedByDate(@Param("startDate") String startDate);
}
