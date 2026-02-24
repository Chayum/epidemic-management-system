package com.epidemic.pandemic.controller;

import com.epidemic.common.result.PageResult;
import com.epidemic.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Tag(name = "疫情信息管理", description = "疫情新闻、防疫知识、实时数据接口")
@RestController
@RequestMapping("/pandemic")
@CrossOrigin
public class PandemicController {

    @Operation(summary = "获取疫情新闻列表")
    @GetMapping("/news")
    public Result<PageResult<Map<String, Object>>> getNewsList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        
        List<Map<String, Object>> list = new ArrayList<>();
        
        Map<String, Object> news1 = new HashMap<>();
        news1.put("id", "N001");
        news1.put("title", "我市新增3例确诊病例，均在隔离管控中发现");
        news1.put("summary", "今日新增3例确诊病例，均在隔离管控中发现，全市疫情防控形势总体平稳可控。");
        news1.put("author", "疫情防控指挥部");
        news1.put("status", "published");
        news1.put("viewCount", 1520);
        news1.put("publishTime", "2026-02-24 09:00:00");
        list.add(news1);

        Map<String, Object> news2 = new HashMap<>();
        news2.put("id", "N002");
        news2.put("title", "关于加强重点场所疫情防控工作的通知");
        news2.put("summary", "为进一步做好疫情防控工作，现就加强重点场所疫情防控有关事项通知如下。");
        news2.put("author", "疫情防控指挥部");
        news2.put("status", "published");
        news2.put("viewCount", 890);
        news2.put("publishTime", "2026-02-23 15:00:00");
        list.add(news2);

        PageResult<Map<String, Object>> pageResult = PageResult.of(list, 10L, page, size);
        return Result.success("获取成功", pageResult);
    }

    @Operation(summary = "获取防疫知识列表")
    @GetMapping("/knowledge")
    public Result<PageResult<Map<String, Object>>> getKnowledgeList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        
        List<Map<String, Object>> list = new ArrayList<>();
        
        Map<String, Object> knowledge1 = new HashMap<>();
        knowledge1.put("id", "K001");
        knowledge1.put("title", "如何正确佩戴N95口罩");
        knowledge1.put("summary", "正确佩戴N95口罩是预防呼吸道传染病的重要措施，请按照以下步骤操作。");
        knowledge1.put("category", "personal");
        knowledge1.put("author", "疾控中心");
        knowledge1.put("viewCount", 3250);
        knowledge1.put("sortOrder", 1);
        list.add(knowledge1);

        Map<String, Object> knowledge2 = new HashMap<>();
        knowledge2.put("id", "K002");
        knowledge2.put("title", "家庭日常消毒指南");
        knowledge2.put("summary", "家庭日常消毒是预防疫情的重要环节，请了解以下消毒方法和注意事项。");
        knowledge2.put("category", "community");
        knowledge2.put("author", "疾控中心");
        knowledge2.put("viewCount", 2180);
        knowledge2.put("sortOrder", 2);
        list.add(knowledge2);

        Map<String, Object> knowledge3 = new HashMap<>();
        knowledge3.put("id", "K003");
        knowledge3.put("title", "医疗机构内个人防护要点");
        knowledge3.put("summary", "医疗机构内工作人员应严格做好个人防护，防止职业暴露。");
        knowledge3.put("category", "hospital");
        knowledge3.put("author", "卫健委");
        knowledge3.put("viewCount", 1680);
        knowledge3.put("sortOrder", 3);
        list.add(knowledge3);

        PageResult<Map<String, Object>> pageResult = PageResult.of(list, 10L, page, size);
        return Result.success("获取成功", pageResult);
    }

    @Operation(summary = "获取实时疫情数据")
    @GetMapping("/data")
    public Result<Map<String, Object>> getPandemicData() {
        Map<String, Object> data = new HashMap<>();
        data.put("confirmed", 156);
        data.put("cured", 120);
        data.put("dead", 3);
        data.put("suspected", 10);
        data.put("updateTime", "2026-02-24 10:00:00");
        return Result.success("获取成功", data);
    }
}
