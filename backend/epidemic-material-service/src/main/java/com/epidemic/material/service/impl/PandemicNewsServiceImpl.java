package com.epidemic.material.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.epidemic.common.result.PageResult;
import com.epidemic.material.entity.PandemicNews;
import com.epidemic.material.mapper.PandemicNewsMapper;
import com.epidemic.material.service.PandemicNewsService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 疫情新闻服务实现类
 */
@Service
public class PandemicNewsServiceImpl extends ServiceImpl<PandemicNewsMapper, PandemicNews> implements PandemicNewsService {

    @Override
    public PageResult<PandemicNews> getNewsList(Integer page, Integer size, String status) {
        Page<PandemicNews> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<PandemicNews> wrapper = new LambdaQueryWrapper<>();
        
        if (StringUtils.hasText(status)) {
            wrapper.eq(PandemicNews::getStatus, status);
        }
        
        wrapper.orderByDesc(PandemicNews::getPublishTime);
        
        Page<PandemicNews> result = baseMapper.selectPage(pageParam, wrapper);
        return PageResult.of(result.getRecords(), result.getTotal(), page, size);
    }
}
