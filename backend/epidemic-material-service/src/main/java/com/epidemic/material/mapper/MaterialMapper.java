package com.epidemic.material.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.epidemic.material.entity.Material;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * 物资Mapper接口
 */
@Mapper
public interface MaterialMapper extends BaseMapper<Material> {

    @Select("SELECT IFNULL(SUM(stock), 0) FROM material")
    Integer sumStock();
}
