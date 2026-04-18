package com.epidemic.material.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.epidemic.common.enums.MaterialTypeEnum;
import com.epidemic.common.exception.BusinessException;
import com.epidemic.common.result.PageResult;
import com.epidemic.common.util.InventoryWarningUtil;
import com.epidemic.material.entity.Material;
import com.epidemic.material.mapper.MaterialMapper;
import com.epidemic.material.service.CacheService;
import com.epidemic.material.service.InventoryLogService;
import com.epidemic.material.service.MaterialService;
import com.epidemic.material.util.DistributedLockUtil;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 物资服务实现类
 * 负责物资的增删改查、库存管理、预警分析及统计功能
 */
@Service
@Slf4j
public class MaterialServiceImpl extends ServiceImpl<MaterialMapper, Material> implements MaterialService {

    @Autowired
    private CacheService cacheService;

    @Autowired
    private InventoryLogService inventoryLogService;

    @Autowired
    private DistributedLockUtil distributedLockUtil;

    /**
     * 应用启动时预热缓存
     * 加载物资类型、仓库等基础配置数据到 Redis
     */
    @PostConstruct
    public void warmUpCache() {
        try {
            log.info("开始缓存预热...");

            // 预热物资类型列表
            List<Map<String, String>> typeList = MaterialTypeEnum.toList();
            cacheService.setMaterialTypes(typeList);
            log.info("物资类型缓存预热完成，共 {} 种", typeList.size());

            // 预热仓库列表
            List<String> warehouses = baseMapper.selectMaps(new LambdaQueryWrapper<Material>()
                    .select(Material::getWarehouse)
                    .groupBy(Material::getWarehouse))
                    .stream()
                    .map(m -> (String) m.get("warehouse"))
                    .filter(StringUtils::hasText)
                    .distinct()
                    .collect(Collectors.toList());

            List<Map<String, Object>> warehouseList = warehouses.stream().map(warehouse -> {
                Map<String, Object> map = new HashMap<>();
                map.put("name", warehouse);
                return map;
            }).collect(Collectors.toList());

            cacheService.setWarehouses(warehouseList);
            log.info("仓库列表缓存预热完成，共 {} 个", warehouseList.size());

            log.info("缓存预热完成");
        } catch (Exception e) {
            log.warn("缓存预热失败，将在首次访问时加载: {}", e.getMessage());
        }
    }

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
        // 生成唯一ID（使用UUID替代时间戳，避免高并发碰撞）
        material.setId("M" + UUID.randomUUID().toString().replace("-", "").substring(0, 16));
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

        // 检查库存预警状态
        if (material.getStock() != null && material.getThreshold() != null) {
            material.setStatus(InventoryWarningUtil.calculateStatus(material.getStock(), material.getThreshold()));
        }

        baseMapper.updateById(material);

        // 清除缓存，确保预警列表实时更新
        cacheService.deleteMaterialStats();
        cacheService.deleteWarningList();
        log.info("物资 {} 更新已保存，预警缓存已清除", material.getId());
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
        // 1. 尝试从缓存获取
        Object cachedList = cacheService.getWarningList();
        if (cachedList != null) {
            log.debug("从缓存获取预警列表");
            return (List<Map<String, Object>>) cachedList;
        }
        
        // 2. 缓存未命中，从数据库查询
        LambdaQueryWrapper<Material> wrapper = new LambdaQueryWrapper<>();
        wrapper.apply("stock < `threshold`"); 
        
        List<Material> list = baseMapper.selectList(wrapper);
        
        List<Map<String, Object>> resultList = list.stream().map(m -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", m.getId());
            map.put("name", m.getName());
            map.put("stock", m.getStock());
            map.put("threshold", m.getThreshold());
            map.put("unit", m.getUnit());
            map.put("type", m.getType());
            map.put("warningLevel", InventoryWarningUtil.calculateWarningLevel(m.getStock(), m.getThreshold()));
            return map;
        }).collect(Collectors.toList());
        
        // 3. 存入缓存
        cacheService.setWarningList(resultList);
        log.info("库存预警列表已缓存，共 {} 条", resultList.size());
        
        return resultList;
    }

    /**
     * 获取物资类型列表（带缓存）
     */
    public List<Map<String, String>> getTypeList() {
        // 1. 尝试从缓存获取
        Object cachedTypes = cacheService.getMaterialTypes();
        if (cachedTypes != null) {
            log.debug("从缓存获取物资类型列表");
            return (List<Map<String, String>>) cachedTypes;
        }

        // 2. 缓存未命中，使用枚举获取类型列表
        List<Map<String, String>> typeList = MaterialTypeEnum.toList();

        // 3. 存入缓存
        cacheService.setMaterialTypes(typeList);
        log.info("物资类型列表已缓存");

        return typeList;
    }

    /**
     * 获取仓库列表（带缓存）
     */
    public List<Map<String, Object>> getWarehouseList() {
        // 1. 尝试从缓存获取
        Object cachedWarehouses = cacheService.getWarehouses();
        if (cachedWarehouses != null) {
            log.debug("从缓存获取仓库列表");
            return (List<Map<String, Object>>) cachedWarehouses;
        }
        
        // 2. 缓存未命中，从数据库查询 distinct warehouse
        List<String> warehouses = baseMapper.selectMaps(new LambdaQueryWrapper<Material>()
                .select(Material::getWarehouse)
                .groupBy(Material::getWarehouse))
                .stream()
                .map(m -> (String) m.get("warehouse"))
                .filter(StringUtils::hasText)
                .distinct()
                .collect(Collectors.toList());
        
        List<Map<String, Object>> resultList = warehouses.stream().map(warehouse -> {
            Map<String, Object> map = new HashMap<>();
            map.put("name", warehouse);
            return map;
        }).collect(Collectors.toList());
        
        // 3. 存入缓存
        cacheService.setWarehouses(resultList);
        log.info("仓库列表已缓存，共 {} 个", resultList.size());
        
        return resultList;
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
        // 解析物资 ID
        String materialId = inventoryId.replace("INV", "");
        // 使用分布式锁保证并发安全
        String lockKey = "inventory:check:" + materialId;

        distributedLockUtil.executeWithLock(lockKey, () -> {
            Material material = baseMapper.selectById(materialId);
            if (material == null) {
                throw new BusinessException("物资不存在");
            }

            // 如果盘点数量与账面数量不一致，则生成盘点差异记录
            if (!actualStock.equals(material.getStock())) {
                Integer difference = actualStock - material.getStock();

                // 更新库存
                material.setStock(actualStock);
                material.setUpdateTime(LocalDateTime.now());
                material.setStatus(InventoryWarningUtil.calculateStatus(actualStock, material.getThreshold()));
                baseMapper.updateById(material);

                // 清除缓存
                cacheService.deleteMaterialStats();
                cacheService.deleteWarningList();

                // 记录库存变动日志
                inventoryLogService.log(
                    materialId,
                    material.getName(),
                    "adjust",
                    difference,
                    material.getStock() - difference,
                    actualStock,
                    null,
                    null,
                    "系统",
                    remark != null ? remark : "库存盘点"
                );
            }
        });
    }

    /**
     * 获取物资综合统计数据
     * 包括总库存量和各类型物资的分布情况
     *
     * @return 统计结果Map
     */
    @Override
    public Map<String, Object> getStats() {
        // 1. 尝试从缓存获取
        Map<String, Object> cachedStats = cacheService.getMaterialStats();
        if (cachedStats != null) {
            return cachedStats;
        }
        
        // 2. 缓存未命中，从数据库查询
        Map<String, Object> stats = new HashMap<>();
        // 统计总库存
        stats.put("totalStock", baseMapper.sumStock());

        // 按类型统计库存分布
        List<Map<String, Object>> typeStats = baseMapper.countByType();

        // 映射类型编码为中文名称
        Map<String, String> typeNameMap = MaterialTypeEnum.toNameMap();

        for (Map<String, Object> item : typeStats) {
            String type = (String) item.get("type");
            item.put("name", typeNameMap.getOrDefault(type, type));
        }

        stats.put("typeStats", typeStats);

        // 3. 存入缓存
        cacheService.setMaterialStats(stats);
        log.info("物资统计数据已缓存");

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
