package com.epidemic.user.controller;

import com.epidemic.common.entity.OperateLog;
import com.epidemic.common.result.PageResult;
import com.epidemic.common.result.Result;
import com.epidemic.user.service.OperateLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 操作日志控制器
 */
@Tag(name = "操作日志", description = "操作日志查询和管理")
@RestController
@RequestMapping("/log")
@CrossOrigin
public class OperateLogController {

    private final OperateLogService operateLogService;

    public OperateLogController(OperateLogService operateLogService) {
        this.operateLogService = operateLogService;
    }

    /**
     * 保存操作日志（供其他微服务调用）
     */
    @Operation(summary = "保存操作日志")
    @PostMapping("/save")
    public Result<Void> saveLog(@RequestBody OperateLog operateLog) {
        if (operateLog.getOperateTime() == null) {
            operateLog.setOperateTime(LocalDateTime.now());
        }
        operateLogService.save(operateLog);
        return Result.success();
    }

    /**
     * 获取最近操作日志
     */
    @Operation(summary = "获取最近操作日志")
    @GetMapping("/recent")
    public Result<List<OperateLog>> getRecentLogs(@RequestParam(defaultValue = "10") Integer limit) {
        return Result.success(operateLogService.getRecentLogs(limit));
    }

    /**
     * 分页查询操作日志
     */
    @Operation(summary = "分页查询操作日志")
    @GetMapping("/list")
    public Result<PageResult<OperateLog>> getLogList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String module,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {
        var result = operateLogService.getLogList(page, size, username, module, startTime, endTime);
        return Result.success(new PageResult<>(result.getRecords(), result.getTotal(), page, size));
    }
}