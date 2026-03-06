package com.epidemic.material.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.epidemic.common.exception.BusinessException;
import com.epidemic.common.result.PageResult;
import com.epidemic.material.entity.Material;
import com.epidemic.material.mapper.MaterialMapper;
import com.epidemic.material.service.MaterialService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 物资服务实现类
 * 负责物资的增删改查、库存管理、预警分析及统计功能
 */
@Service
public class MaterialServiceImpl extends ServiceImpl<MaterialMapper, Material> implements MaterialService {

    /**
     * 分页查询物资列表
     * 支持名称模糊查询、类型精确查询以及基于库存阈值的状态筛选
     *
     * @param page 页码
     * @param size 每页大小
     * @param name 物资名称（模糊）
     * @param type 物资类型
     * @param status 库存状态（sufficient:充足, warning:预警, insufficient:不足）
     * @return 分页结果
     */
    @Override
    public PageResult<Material> getMaterialList(Integer page, Integer size, String name, String type, String status) {
        Page<Material> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Material> wrapper = new LambdaQueryWrapper<>();
        
        if (StringUtils.hasText(name)) {
            wrapper.like(Material::getName, name);
        }
        if (StringUtils.hasText(type)) {
            wrapper.eq(Material::getType, type);
        }
        // 根据状态码动态构建库存阈值查询条件
        if (StringUtils.hasText(status)) {
            if ("sufficient".equals(status)) {
                // 库存 >= 阈值
                wrapper.apply("stock >= threshold");
            } else if ("warning".equals(status)) {
                // 阈值 * 0.5 <= 库存 < 阈值
                wrapper.apply("stock < threshold AND stock >= threshold * 0.5");
            } else if ("insufficient".equals(status)) {
                // 库存 < 阈值 * 0.5
                wrapper.apply("stock < threshold * 0.5");
            } else {
                // 其他情况按数据库字段匹配
                wrapper.eq(Material::getStatus, status);
            }
        }
        wrapper.orderByDesc(Material::getCreateTime);
        
        Page<Material> result = baseMapper.selectPage(pageParam, wrapper);
        return PageResult.of(result.getRecords(), result.getTotal(), page, size);
    }

    /**
     * 新增物资
     * 自动生成ID、设置创建时间及默认状态
     *
     * @param material 物资实体
     */
    @Override
    public void addMaterial(Material material) {
        // 生成唯一ID（此处简化使用时间戳，生产环境建议使用雪花算法）
        material.setId("M" + System.currentTimeMillis());
        material.setCreateTime(LocalDateTime.now());
        material.setUpdateTime(LocalDateTime.now());
        if (material.getStatus() == null) {
            material.setStatus("normal");
        }
        baseMapper.insert(material);
    }

    /**
     * 更新物资信息
     *
     * @param material 包含更新字段的物资实体
     */
    @Override
    public void updateMaterial(Material material) {
        if (material.getId() == null) {
            throw new BusinessException("物资ID不能为空");
        }
        material.setUpdateTime(LocalDateTime.now());
        baseMapper.updateById(material);
    }

    /**
     * 获取库存列表（带仓库信息）
     * 目前仓库和库位信息为Mock数据，实际需关联Warehouse表
     *
     * @param page 页码
     * @param size 每页大小
     * @param warehouse 仓库筛选
     * @return 包含库存详情的Map列表
     */
    @Override
    public PageResult<Map<String, Object>> getInventoryList(Integer page, Integer size, String warehouse) {
        Page<Material> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Material> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(warehouse)) {
            wrapper.eq(Material::getWarehouse, warehouse);
        }
        Page<Material> result = baseMapper.selectPage(pageParam, wrapper);
        
        // 组装库存展示数据
        List<Map<String, Object>> list = result.getRecords().stream().map(m -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", "INV" + m.getId());
            map.put("materialId", m.getId());
            map.put("materialName", m.getName());
            map.put("warehouse", m.getWarehouse());
            map.put("location", "A区-01架-01层"); // Mock location
            map.put("stock", m.getStock());
            map.put("unit", m.getUnit());
            map.put("lastCheckTime", LocalDateTime.now()); // Mock check time
            return map;
        }).collect(Collectors.toList());
        
        return PageResult.of(list, result.getTotal(), page, size);
    }

    /**
     * 获取库存预警物资列表
     * 查询库存低于阈值的物资，并计算预警等级
     *
     * @return 预警物资列表
     */
    @Override
    public List<Map<String, Object>> getWarningList() {
        LambdaQueryWrapper<Material> wrapper = new LambdaQueryWrapper<>();
        wrapper.apply("stock < threshold"); 
        
        List<Material> list = baseMapper.selectList(wrapper);
        
        return list.stream().map(m -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", m.getId());
            map.put("name", m.getName());
            map.put("stock", m.getStock());
            map.put("threshold", m.getThreshold());
            map.put("unit", m.getUnit());
            map.put("type", m.getType());
            // 库存 < 阈值的一半为高风险(high)，否则为低风险(low)
            map.put("warningLevel", m.getStock() < m.getThreshold() * 0.5 ? "high" : "low");
            return map;
        }).collect(Collectors.toList());
    }

    /**
     * 库存盘点/校准
     * 更新物资的实际库存数量
     *
     * @param inventoryId 库存记录ID（当前逻辑为 "INV" + MaterialId）
     * @param actualStock 盘点后的实际库存
     * @param remark 盘点备注
     */
    @Override
    public void checkInventory(String inventoryId, Integer actualStock, String remark) {
        // 解析物资ID
        String materialId = inventoryId.replace("INV", "");
        Material material = baseMapper.selectById(materialId);
        if (material == null) {
            throw new BusinessException("物资不存在");
        }
        // 更新库存
        material.setStock(actualStock);
        material.setUpdateTime(LocalDateTime.now());
        baseMapper.updateById(material);
    }

    /**
     * 获取物资综合统计数据
     * 包括总库存量和各类型物资的分布情况
     *
     * @return 统计结果Map
     */
    @Override
    public Map<String, Object> getStats() {
        Map<String, Object> stats = new HashMap<>();
        // 统计总库存
        stats.put("totalStock", baseMapper.sumStock());
        
        // 按类型统计库存分布
        List<Map<String, Object>> typeStats = baseMapper.countByType();
        
        // 映射类型编码为中文名称
        Map<String, String> typeNameMap = new HashMap<>();
        typeNameMap.put("protective", "防护物资");
        typeNameMap.put("disinfection", "消杀物资");
        typeNameMap.put("testing", "检测物资");
        typeNameMap.put("equipment", "医疗设备");
        typeNameMap.put("other", "其他物资");
        
        for (Map<String, Object> item : typeStats) {
            String type = (String) item.get("type");
            item.put("name", typeNameMap.getOrDefault(type, type));
        }
        
        stats.put("typeStats", typeStats);
        
        return stats;
    }
    
    /**
     * 获取可用库存总量
     */
    @Override
    public Long getTotalStock() {
        Integer stock = baseMapper.sumStock();
        return stock != null ? stock.longValue() : 0L;
    }
}
