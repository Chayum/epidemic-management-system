package com.epidemic.material.controller;

import com.epidemic.common.result.PageResult;
import com.epidemic.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Tag(name = "物资管理", description = "物资相关接口")
@RestController
@RequestMapping("/material")
@CrossOrigin
public class MaterialController {

    @Operation(summary = "获取物资列表")
    @GetMapping("/list")
    public Result<PageResult<Map<String, Object>>> getMaterialList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        
        List<Map<String, Object>> list = new ArrayList<>();
        
        Map<String, Object> material1 = new HashMap<>();
        material1.put("id", "M001");
        material1.put("name", "N95医用口罩");
        material1.put("type", "protective");
        material1.put("typeName", "防护物资");
        material1.put("specification", "N95");
        material1.put("unit", "个");
        material1.put("stock", 8500);
        material1.put("threshold", 5000);
        material1.put("status", "normal");
        material1.put("statusName", "正常");
        material1.put("warehouse", "一号仓库");
        list.add(material1);

        Map<String, Object> material2 = new HashMap<>();
        material2.put("id", "M002");
        material2.put("name", "医用外科口罩");
        material2.put("type", "protective");
        material2.put("typeName", "防护物资");
        material2.put("specification", "灭菌级");
        material2.put("unit", "个");
        material2.put("stock", 15000);
        material2.put("threshold", 10000);
        material2.put("status", "normal");
        material2.put("statusName", "正常");
        material2.put("warehouse", "一号仓库");
        list.add(material2);

        Map<String, Object> material3 = new HashMap<>();
        material3.put("id", "M005");
        material3.put("name", "75%酒精");
        material3.put("type", "disinfection");
        material3.put("typeName", "消杀物资");
        material3.put("specification", "500ml/瓶");
        material3.put("unit", "瓶");
        material3.put("stock", 3200);
        material3.put("threshold", 5000);
        material3.put("status", "warning");
        material3.put("statusName", "预警");
        material3.put("warehouse", "二号仓库");
        list.add(material3);

        PageResult<Map<String, Object>> pageResult = PageResult.of(list, 10L, page, size);
        return Result.success("获取成功", pageResult);
    }
}
