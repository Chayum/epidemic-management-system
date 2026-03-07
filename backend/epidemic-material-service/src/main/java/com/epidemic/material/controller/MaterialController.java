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
 * 提供物资信息的增删改查功能接口
 */
@Tag(name = "物资管理", description = "物资相关接口")
@RestController
@RequestMapping("/material")
@CrossOrigin
public class MaterialController {

    @Autowired
    private MaterialService materialService;

    /**
     * 获取物资列表
     * @param page 页码，默认为1
     * @param size 每页大小，默认为10
     * @param name 物资名称（可选，模糊查询）
     * @param type 物资类型（可选）
     * @param status 物资状态（可选）
     * @return 分页后的物资列表
     */
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

    /**
     * 获取物资详情
     * @param id 物资 ID
     * @return 物资实体对象
     */
    @Operation(summary = "获取物资详情")
    @GetMapping("/{id}")
    public Result<Material> getMaterialDetail(@PathVariable String id) {
        return Result.success(materialService.getById(id));
    }

    /**
     * 获取物资类型列表
     * @return 物资类型列表
     */
    @Operation(summary = "获取物资类型列表")
    @GetMapping("/types")
    public Result<List<Map<String, String>>> getTypeList() {
        return Result.success(materialService.getTypeList());
    }

    /**
     * 获取仓库列表
     * @return 仓库列表
     */
    @Operation(summary = "获取仓库列表")
    @GetMapping("/warehouses")
    public Result<List<Map<String, Object>>> getWarehouseList() {
        return Result.success(materialService.getWarehouseList());
    }

    /**
     * 新增物资
     * @param material 物资实体对象
     * @return 操作结果消息
     */
    @Operation(summary = "新增物资")
    @PostMapping
    public Result<String> addMaterial(@RequestBody Material material) {
        materialService.addMaterial(material);
        return Result.success("新增成功");
    }

    /**
     * 更新物资信息
     * @param material 物资实体对象（必须包含ID）
     * @return 操作结果消息
     */
    @Operation(summary = "更新物资")
    @PutMapping
    public Result<String> updateMaterial(@RequestBody Material material) {
        materialService.updateMaterial(material);
        return Result.success("更新成功");
    }

    /**
     * 删除物资
     * @param id 物资ID
     * @return 操作结果消息
     */
    @Operation(summary = "删除物资")
    @DeleteMapping("/{id}")
    public Result<String> deleteMaterial(@PathVariable String id) {
        materialService.removeById(id);
        return Result.success("删除成功");
    }
}
