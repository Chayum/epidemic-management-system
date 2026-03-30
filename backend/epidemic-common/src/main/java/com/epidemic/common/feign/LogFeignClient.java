package com.epidemic.common.feign;

import com.epidemic.common.dto.OperateLogQueryDTO;
import com.epidemic.common.result.PageResult;
import com.epidemic.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import com.epidemic.common.entity.OperateLog;

/**
 * 日志服务 Feign 客户端
 * 供其他微服务查询操作日志
 */
@FeignClient(name = "epidemic-user-service", path = "/log", contextId = "logFeignClient")
public interface LogFeignClient {

    /**
     * 获取最近操作日志
     */
    @GetMapping("/recent")
    Result<List<OperateLog>> getRecentLogs(@RequestParam(defaultValue = "10") Integer limit);

    /**
     * 分页查询操作日志
     */
    @GetMapping("/list")
    Result<PageResult<OperateLog>> getLogList(@ModelAttribute OperateLogQueryDTO queryDTO);
}