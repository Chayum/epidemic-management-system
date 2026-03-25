package com.epidemic.material.controller;

import com.epidemic.common.result.PageResult;
import com.epidemic.common.result.Result;
import com.epidemic.common.annotation.OperateLog;
import com.epidemic.material.service.MaterialService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 库存管理控制器
 */
@Tag(name = "库存管理", description = "库存相关接口")
@RestController
@RequestMapping("/inventory")
@CrossOrigin
public class InventoryController {

    @Autowired
    private MaterialService materialService;

    @Operation(summary = "获取库存列表")
    @GetMapping("/list")
    public Result<PageResult<Map<String, Object>>> getInventoryList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String warehouse) {
        return Result.success(materialService.getInventoryList(page, size, warehouse));
    }

    @Operation(summary = "获取库存预警列表")
    @GetMapping("/warning")
    public Result<List<Map<String, Object>>> getWarningList() {
        return Result.success(materialService.getWarningList());
    }

    @Operation(summary = "盘点库存")
    @PostMapping("/check")
    @OperateLog(module = "库存管理", operation = "盘点库存")
    public Result<String> checkInventory(@RequestBody Map<String, Object> params) {
        String inventoryId = (String) params.get("inventoryId");
        Integer actualStock = (Integer) params.get("actualStock");
        String remark = (String) params.get("remark");
        materialService.checkInventory(inventoryId, actualStock, remark);
        return Result.success("盘点成功");
    }
}
