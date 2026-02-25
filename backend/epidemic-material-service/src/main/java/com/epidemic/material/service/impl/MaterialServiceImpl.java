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
 */
@Service
public class MaterialServiceImpl extends ServiceImpl<MaterialMapper, Material> implements MaterialService {

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
        if (StringUtils.hasText(status)) {
            wrapper.eq(Material::getStatus, status);
        }
        wrapper.orderByDesc(Material::getCreateTime);
        
        Page<Material> result = baseMapper.selectPage(pageParam, wrapper);
        return PageResult.of(result.getRecords(), result.getTotal(), page, size);
    }

    @Override
    public void addMaterial(Material material) {
        material.setId("M" + System.currentTimeMillis());
        material.setCreateTime(LocalDateTime.now());
        material.setUpdateTime(LocalDateTime.now());
        if (material.getStatus() == null) {
            material.setStatus("normal");
        }
        baseMapper.insert(material);
    }

    @Override
    public void updateMaterial(Material material) {
        if (material.getId() == null) {
            throw new BusinessException("物资ID不能为空");
        }
        material.setUpdateTime(LocalDateTime.now());
        baseMapper.updateById(material);
    }

    @Override
    public PageResult<Map<String, Object>> getInventoryList(Integer page, Integer size, String warehouse) {
        Page<Material> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Material> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(warehouse)) {
            wrapper.eq(Material::getWarehouse, warehouse);
        }
        Page<Material> result = baseMapper.selectPage(pageParam, wrapper);
        
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
            map.put("warningLevel", m.getStock() < m.getThreshold() * 0.5 ? "high" : "low");
            return map;
        }).collect(Collectors.toList());
    }

    @Override
    public void checkInventory(String inventoryId, Integer actualStock, String remark) {
        // inventoryId is mapped to INV + MaterialId
        String materialId = inventoryId.replace("INV", "");
        Material material = baseMapper.selectById(materialId);
        if (material == null) {
            throw new BusinessException("物资不存在");
        }
        material.setStock(actualStock);
        material.setUpdateTime(LocalDateTime.now());
        baseMapper.updateById(material);
    }

    @Override
    public Map<String, Object> getStats() {
        Map<String, Object> stats = new HashMap<>();
        // Total Stock
        stats.put("totalStock", baseMapper.sumStock());
        
        List<Map<String, Object>> typeStats = new ArrayList<>();
        // 简单按类型统计数量（非库存）
        // 实际可以做更复杂的聚合
        Map<String, Object> type1 = new HashMap<>();
        type1.put("type", "protective");
        type1.put("name", "防护物资");
        type1.put("count", 0); // 暂未实现按类型统计库存，可后续完善
        typeStats.add(type1);
        
        stats.put("typeStats", typeStats);
        
        return stats;
    }
}
