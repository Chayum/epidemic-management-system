package com.epidemic.material.service;

import com.epidemic.common.result.PageResult;
import com.epidemic.common.entity.PandemicNews;

/**
 * 疫情新闻服务接口
 */
public interface PandemicNewsService {

    /**
     * 获取新闻列表
     * @param page 页码
     * @param size 每页数量
     * @param status 状态
     * @return 分页结果
     */
    PageResult<PandemicNews> getNewsList(Integer page, Integer size, String status);

    /**
     * 获取新闻详情
     * @param id 新闻ID
     * @return 新闻详情
     */
    PandemicNews getById(String id);
}
