package com.epidemic.material.service.impl;

import com.epidemic.common.result.PageResult;
import com.epidemic.common.result.Result;
import com.epidemic.common.entity.PandemicNews;
import com.epidemic.material.feign.PandemicFeignClient;
import com.epidemic.material.service.PandemicNewsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 疫情新闻服务实现类
 * 使用 OpenFeign 调用 pandemic-service 获取数据
 */
@Service
public class PandemicNewsServiceImpl implements PandemicNewsService {

    @Autowired
    private PandemicFeignClient pandemicFeignClient;

    @Override
    public PageResult<PandemicNews> getNewsList(Integer page, Integer size, String status) {
        Result<PageResult<PandemicNews>> result = pandemicFeignClient.getNewsList(page, size, status);
        if (result != null && result.getData() != null) {
            return result.getData();
        }
        return PageResult.of(null, 0L, page, size);
    }

    @Override
    public PandemicNews getById(String id) {
        Result<PandemicNews> result = pandemicFeignClient.getNewsDetail(id);
        if (result != null && result.getData() != null) {
            return result.getData();
        }
        return null;
    }
}
