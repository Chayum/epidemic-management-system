package com.epidemic.pandemic.controller;

import com.epidemic.common.result.PageResult;
import com.epidemic.common.result.Result;
import com.epidemic.pandemic.annotation.OperateLog;
import com.epidemic.pandemic.entity.PandemicNews;
import com.epidemic.pandemic.service.PandemicService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 疫情信息管理控制器
 */
@Tag(name = "疫情信息管理", description = "疫情新闻、防疫知识、实时数据接口")
@RestController
@RequestMapping("/pandemic")
@CrossOrigin
public class PandemicController {

    @Autowired
    private PandemicService pandemicService;

    @Operation(summary = "获取疫情新闻列表")
    @GetMapping("/news")
    public Result<PageResult<PandemicNews>> getNewsList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String status) {
        return Result.success(pandemicService.getNewsList(page, size, status));
    }

    @Operation(summary = "获取新闻详情")
    @GetMapping("/news/{id}")
    public Result<PandemicNews> getNewsDetail(@PathVariable String id) {
        return Result.success(pandemicService.getById(id));
    }

    @Operation(summary = "发布新闻")
    @PostMapping("/news")
    @OperateLog(module = "疫情新闻", operation = "发布新闻")
    public Result<String> publishNews(@RequestBody PandemicNews news) {
        pandemicService.publishNews(news);
        return Result.success("发布成功");
    }

    @Operation(summary = "删除新闻")
    @DeleteMapping("/news/{id}")
    @OperateLog(module = "疫情新闻", operation = "删除新闻")
    public Result<String> deleteNews(@PathVariable String id) {
        pandemicService.removeById(id);
        return Result.success("删除成功");
    }

    @Operation(summary = "获取防疫知识列表")
    @GetMapping("/knowledge")
    public Result<PageResult<Map<String, Object>>> getKnowledgeList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        return Result.success(pandemicService.getKnowledgeList(page, size));
    }

    @Operation(summary = "获取实时疫情数据")
    @GetMapping("/data")
    public Result<Map<String, Object>> getPandemicData() {
        return Result.success(pandemicService.getPandemicData());
    }

    @Operation(summary = "获取推送统计")
    @GetMapping("/push/stats")
    public Result<List<Map<String, Object>>> getPushStats() {
        return Result.success(pandemicService.getPushStats());
    }

    @Operation(summary = "获取推送记录")
    @GetMapping("/push/list")
    public Result<List<Map<String, Object>>> getPushList() {
        return Result.success(pandemicService.getPushList());
    }

    @Operation(summary = "发送推送")
    @PostMapping("/push")
    @OperateLog(module = "消息推送", operation = "发送推送")
    public Result<String> sendPush(@RequestBody Map<String, Object> pushData) {
        pandemicService.sendPush(pushData);
        return Result.success("推送发送成功");
    }

    @Operation(summary = "获取用户角色分布统计")
    @GetMapping("/push/role-stats")
    public Result<List<Map<String, Object>>> getUserRoleStats() {
        return Result.success(pandemicService.getUserRoleStats());
    }

    @Operation(summary = "删除推送记录")
    @DeleteMapping("/push/{id}")
    @OperateLog(module = "消息推送", operation = "删除推送记录")
    public Result<String> deletePushRecord(@PathVariable Long id) {
        pandemicService.deletePushRecord(id);
        return Result.success("删除成功");
    }
}
