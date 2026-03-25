package com.epidemic.common.feign;

import com.epidemic.common.result.PageResult;
import com.epidemic.common.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import com.epidemic.common.entity.OperateLog;

/**
 * 日志服务 Feign 客户端降级处理
 */
@Component
@Slf4j
public class LogFeignClientFallbackFactory implements FallbackFactory<LogFeignClient> {

    @Override
    public LogFeignClient create(Throwable cause) {
        log.error("日志服务调用失败: {}", cause.getMessage(), cause);
        return new LogFeignClient() {
            @Override
            public Result<List<OperateLog>> getRecentLogs(Integer limit) {
                log.warn("Feign client: getRecentLogs fallback, limit={}", limit);
                return Result.success(new ArrayList<>());
            }

            @Override
            public Result<PageResult<OperateLog>> getLogList(Integer page, Integer size, String username,
                                                               String module, LocalDateTime startTime, LocalDateTime endTime) {
                log.warn("Feign client: getLogList fallback, page={}, size={}", page, size);
                return Result.success(new PageResult<>(new ArrayList<>(), 0L, page, size));
            }
        };
    }
}