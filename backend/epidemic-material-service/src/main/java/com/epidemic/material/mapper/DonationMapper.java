package com.epidemic.material.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.epidemic.material.entity.Donation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * 捐赠Mapper接口
 */
@Mapper
public interface DonationMapper extends BaseMapper<Donation> {

    @Select("SELECT IFNULL(SUM(quantity), 0) FROM donation WHERE status = 'approved'")
    Integer sumApprovedQuantity();
}
