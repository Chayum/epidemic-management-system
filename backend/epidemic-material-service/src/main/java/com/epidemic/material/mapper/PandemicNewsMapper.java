package com.epidemic.material.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.epidemic.material.entity.PandemicNews;
import org.apache.ibatis.annotations.Mapper;

/**
 * 疫情新闻Mapper接口
 */
@Mapper
public interface PandemicNewsMapper extends BaseMapper<PandemicNews> {
}
