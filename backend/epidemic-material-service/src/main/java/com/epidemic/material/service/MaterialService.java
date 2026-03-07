package com.epidemic.material.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.epidemic.common.result.PageResult;
import com.epidemic.material.entity.Material;

import java.util.List;
import java.util.Map;

/**
 * 物资服务接口
 */
public interface MaterialService extends IService<Material> {

    /**
     * 获取物资列表
     * @param page 页码
     * @param size 每页数量
     * @param name 名称
     * @param type 类型
     * @param status 状态
     * @return 分页结果
     */
    PageResult<Material> getMaterialList(Integer page, Integer size, String name, String type, String status);

    /**
     * 新增物资
     * @param material 物资信息
     */
    void addMaterial(Material material);

    /**
     * 更新物资
     * @param material 物资信息
     */
    void updateMaterial(Material material);

    /**
     * 获取库存列表
     * @param page 页码
     * @param size 每页数量
     * @param warehouse 仓库
     * @return 分页结果
     */
    PageResult<Map<String, Object>> getInventoryList(Integer page, Integer size, String warehouse);

    /**
     * 获取库存预警列表
     * @return 预警列表
     */
    List<Map<String, Object>> getWarningList();

    /**
     * 盘点库存
     * @param inventoryId 库存ID (物资ID)
     * @param actualStock 实际库存
     * @param remark 备注
     */
    void checkInventory(String inventoryId, Integer actualStock, String remark);

    /**
     * 获取物资统计
     * @return 统计数据
     */
    Map<String, Object> getStats();
    
    /**
     * 获取可用库存总量
     * @return 库存总量
     */
    Long getTotalStock();

    /**
     * 获取物资类型列表
     * @return 类型列表
     */
    List<Map<String, String>> getTypeList();

    /**
     * 获取仓库列表
     * @return 仓库列表
     */
    List<Map<String, Object>> getWarehouseList();
}
