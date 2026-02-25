package com.epidemic.material.controller;

import com.epidemic.common.result.PageResult;
import com.epidemic.common.result.Result;
import com.epidemic.material.entity.PandemicNews;
import com.epidemic.material.service.PandemicNewsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 疫情新闻控制器
 */
@Tag(name = "疫情新闻", description = "疫情新闻相关接口")
@RestController
@RequestMapping("/stats/news")
public class PandemicNewsController {

    @Autowired
    private PandemicNewsService pandemicNewsService;

    @Operation(summary = "获取新闻列表")
    @GetMapping("/list")
    public Result<PageResult<PandemicNews>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String status) {
        return Result.success(pandemicNewsService.getNewsList(page, size, status));
    }

    @Operation(summary = "获取新闻详情")
    @GetMapping("/{id}")
    public Result<PandemicNews> getById(@PathVariable String id) {
        return Result.success(pandemicNewsService.getById(id));
    }
}
