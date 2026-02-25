package com.epidemic.material.controller;

import com.epidemic.common.result.PageResult;
import com.epidemic.common.result.Result;
import com.epidemic.material.entity.Material;
import com.epidemic.material.service.MaterialService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 物资管理控制器
 */
@Tag(name = "物资管理", description = "物资相关接口")
@RestController
@RequestMapping("/material")
@CrossOrigin
public class MaterialController {

    @Autowired
    private MaterialService materialService;

    @Operation(summary = "获取物资列表")
    @GetMapping("/list")
    public Result<PageResult<Material>> getMaterialList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String status) {
        return Result.success(materialService.getMaterialList(page, size, name, type, status));
    }

    @Operation(summary = "获取物资详情")
    @GetMapping("/{id}")
    public Result<Material> getMaterialDetail(@PathVariable String id) {
        return Result.success(materialService.getById(id));
    }

    @Operation(summary = "新增物资")
    @PostMapping
    public Result<String> addMaterial(@RequestBody Material material) {
        materialService.addMaterial(material);
        return Result.success("新增成功");
    }

    @Operation(summary = "更新物资")
    @PutMapping
    public Result<String> updateMaterial(@RequestBody Material material) {
        materialService.updateMaterial(material);
        return Result.success("更新成功");
    }

    @Operation(summary = "删除物资")
    @DeleteMapping("/{id}")
    public Result<String> deleteMaterial(@PathVariable String id) {
        materialService.removeById(id);
        return Result.success("删除成功");
    }
}
