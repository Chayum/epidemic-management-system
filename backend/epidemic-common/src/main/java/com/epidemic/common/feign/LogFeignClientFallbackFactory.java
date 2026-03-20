package com.epidemic.common.feign;

import com.epidemic.common.entity.OperateLog;
import com.epidemic.common.result.Result;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * 日志服务 Feign 客户端降级工厂
 */
@Component
public class LogFeignClientFallbackFactory implements FallbackFactory<LogFeignClient> {

    @Override
    public LogFeignClient create(Throwable cause) {
        return new LogFeignClient() {
            @Override
            public Result<Void> saveLog(OperateLog operateLog) {
                // 降级处理：记录日志失败不影响业务
                System.err.println("保存操作日志失败，降级处理: " + cause.getMessage());
                return Result.error(500, "日志服务暂时不可用");
            }

            @Override
            public Result<List<OperateLog>> getRecentLogs(Integer limit) {
                // 降级处理：返回空列表
                System.err.println("获取操作日志失败，降级处理: " + cause.getMessage());
                return Result.success(Collections.emptyList());
            }
        };
    }
}