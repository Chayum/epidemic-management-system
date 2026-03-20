package com.epidemic.common.feign;

import com.epidemic.common.entity.OperateLog;
import com.epidemic.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * 日志服务 Feign 客户端
 */
@FeignClient(name = "epidemic-log-service", path = "/log", fallbackFactory = LogFeignClientFallbackFactory.class)
public interface LogFeignClient {

    /**
     * 保存操作日志
     * @param operateLog 日志对象
     * @return 操作结果
     */
    @PostMapping("/save")
    Result<Void> saveLog(@RequestBody OperateLog operateLog);

    /**
     * 获取最近操作日志
     * @param limit 返回条数
     * @return 操作日志列表
     */
    @GetMapping("/recent")
    Result<List<OperateLog>> getRecentLogs(@RequestParam(defaultValue = "10") Integer limit);
}