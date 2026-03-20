package com.epidemic.material.feign;

import com.epidemic.common.result.PageResult;
import com.epidemic.common.result.Result;
import com.epidemic.common.entity.PandemicNews;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

/**
 * PandemicFeignClient 降级工厂
 * 当 pandemic-service 不可用时，提供降级响应
 */
@Component
@Slf4j
public class PandemicFeignClientFallbackFactory implements FallbackFactory<PandemicFeignClient> {

    @Override
    public PandemicFeignClient create(Throwable cause) {
        log.error("PandemicFeignClient 调用失败: {}", cause.getMessage());

        return new PandemicFeignClient() {
            @Override
            public Result<PageResult<PandemicNews>> getNewsList(Integer page, Integer size, String status) {
                log.warn("PandemicFeignClient.getNewsList 降级返回空列表");
                return Result.error(503, "疫情新闻服务暂时不可用");
            }

            @Override
            public Result<PandemicNews> getNewsDetail(String id) {
                log.warn("PandemicFeignClient.getNewsDetail 降级返回null, id={}", id);
                return Result.error(503, "疫情新闻服务暂时不可用");
            }
        };
    }
}
