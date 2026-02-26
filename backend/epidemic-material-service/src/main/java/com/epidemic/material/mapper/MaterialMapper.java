package com.epidemic.material.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.epidemic.material.entity.Material;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 物资Mapper接口
 */
@Mapper
public interface MaterialMapper extends BaseMapper<Material> {

    @Select("SELECT IFNULL(SUM(stock), 0) FROM material")
    Integer sumStock();

    @Select("SELECT type, COUNT(*) as count, SUM(stock) as stock FROM material GROUP BY type")
    List<Map<String, Object>> countByType();
}
