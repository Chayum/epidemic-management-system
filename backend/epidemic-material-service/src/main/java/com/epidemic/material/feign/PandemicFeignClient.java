package com.epidemic.material.feign;

import com.epidemic.common.result.PageResult;
import com.epidemic.common.result.Result;
import com.epidemic.common.entity.PandemicNews;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 疫情新闻服务 Feign Client
 * 用于调用 pandemic-service 的疫情新闻接口
 */
@FeignClient(name = "epidemic-pandemic-service", path = "/pandemic",
             fallbackFactory = PandemicFeignClientFallbackFactory.class)
public interface PandemicFeignClient {

    /**
     * 获取新闻列表
     * @param page 页码
     * @param size 每页数量
     * @param status 状态（可选）
     * @return 分页结果
     */
    @GetMapping("/news")
    Result<PageResult<PandemicNews>> getNewsList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String status);

    /**
     * 获取新闻详情
     * @param id 新闻ID
     * @return 新闻详情
     */
    @GetMapping("/news/{id}")
    Result<PandemicNews> getNewsDetail(@PathVariable("id") String id);
}
