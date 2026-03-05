package com.epidemic.material.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.epidemic.material.dto.StockOrderDTO;
import com.epidemic.material.entity.*;
import com.epidemic.material.mapper.MaterialCostMapper;
import com.epidemic.material.mapper.StockOrderItemMapper;
import com.epidemic.material.mapper.StockOrderMapper;
import com.epidemic.material.service.InventoryLogService;
import com.epidemic.material.service.MaterialService;
import com.epidemic.material.service.StockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.epidemic.material.vo.InventoryLedgerVO;
import java.util.ArrayList;

/**
 * 库存单据服务实现类
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class StockServiceImpl extends ServiceImpl<StockOrderMapper, StockOrder> implements StockService {

    private final StockOrderItemMapper stockOrderItemMapper;
    private final MaterialService materialService;
    private final InventoryLogService inventoryLogService;
    private final MaterialCostMapper materialCostMapper;

    @Override
    public Page<InventoryLedgerVO> getInventoryLedger(Integer page, Integer size, String keyword) {
        // 1. 查询物资列表
        Page<Material> materialPage = new Page<>(page, size);
        LambdaQueryWrapper<Material> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.like(Material::getName, keyword).or().like(Material::getId, keyword);
        }
        Page<Material> result = materialService.page(materialPage, wrapper);
        
        // 2. 组装台账信息
        List<InventoryLedgerVO> voList = new ArrayList<>();
        for (Material material : result.getRecords()) {
            InventoryLedgerVO vo = new InventoryLedgerVO();
            vo.setMaterialId(material.getId());
            vo.setMaterialName(material.getName());
            vo.setSpecification(material.getSpecification());
            vo.setUnit(material.getUnit());
            vo.setStock(material.getStock());
            vo.setThreshold(material.getThreshold());
            vo.setStatus(material.getStatus());
            vo.setWarehouse(material.getWarehouse());
            
            // 查询成本信息
            MaterialCost cost = materialCostMapper.selectById(material.getId());
            if (cost != null) {
                vo.setAvgPrice(cost.getAvgPrice());
                vo.setTotalValue(cost.getTotalValue());
            } else {
                vo.setAvgPrice(BigDecimal.ZERO);
                vo.setTotalValue(BigDecimal.ZERO);
            }
            voList.add(vo);
        }
        
        Page<InventoryLedgerVO> voPage = new Page<>(page, size);
        voPage.setRecords(voList);
        voPage.setTotal(result.getTotal());
        return voPage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String createOrder(StockOrderDTO dto, Long userId, String username) {
        // 创建单据主表
        StockOrder order = new StockOrder();
        order.setId(UUID.randomUUID().toString().replace("-", ""));
        order.setType(dto.getType());
        order.setSourceType(dto.getSourceType());
        order.setSourceId(dto.getSourceId());
        order.setStatus("pending"); // 默认待审核，若是草稿则为draft
        order.setSupplier(dto.getSupplier());
        order.setDepartment(dto.getDepartment());
        order.setHandlerId(userId);
        order.setHandlerName(username);
        order.setRemark(dto.getRemark());
        order.setCreateTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());

        // 计算总金额并保存明细
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (StockOrderDTO.StockOrderItemDTO itemDto : dto.getItems()) {
            StockOrderItem item = new StockOrderItem();
            item.setOrderId(order.getId());
            item.setMaterialId(itemDto.getMaterialId());
            
            // 查询物资名称和规格
            Material material = materialService.getById(itemDto.getMaterialId());
            if (material != null) {
                item.setMaterialName(material.getName());
                item.setSpecification(material.getSpecification());
                item.setUnit(material.getUnit());
            } else {
                throw new RuntimeException("物资不存在: " + itemDto.getMaterialId());
            }
            
            item.setQuantity(itemDto.getQuantity());
            item.setPrice(itemDto.getPrice() != null ? itemDto.getPrice() : BigDecimal.ZERO);
            item.setAmount(item.getPrice().multiply(new BigDecimal(item.getQuantity())));
            item.setBatchNo(itemDto.getBatchNo());
            item.setExpiryDate(itemDto.getExpiryDate());
            item.setRemark(itemDto.getRemark());
            
            stockOrderItemMapper.insert(item);
            totalAmount = totalAmount.add(item.getAmount());
        }
        
        order.setTotalAmount(totalAmount);
        save(order);
        
        return order.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void auditOrder(String orderId, Long userId, String username, boolean approved, String remark) {
        StockOrder order = getById(orderId);
        if (order == null) {
            throw new RuntimeException("单据不存在");
        }
        if (!"pending".equals(order.getStatus())) {
            throw new RuntimeException("单据状态不是待审核");
        }

        if (approved) {
            order.setStatus("approved");
            // 执行库存变动
            executeStockChange(order, userId, username);
        } else {
            order.setStatus("rejected");
        }
        
        order.setAuditorId(userId);
        order.setAuditorName(username);
        order.setAuditTime(LocalDateTime.now());
        order.setRemark(remark); // 追加审核备注
        updateById(order);
    }

    private void executeStockChange(StockOrder order, Long userId, String username) {
        List<StockOrderItem> items = stockOrderItemMapper.selectList(new LambdaQueryWrapper<StockOrderItem>()
                .eq(StockOrderItem::getOrderId, order.getId()));

        for (StockOrderItem item : items) {
            Material material = materialService.getById(item.getMaterialId());
            if (material == null) {
                throw new RuntimeException("物资不存在: " + item.getMaterialId());
            }

            int currentStock = material.getStock();
            int changeQty = item.getQuantity();
            int afterStock;

            if ("inbound".equals(order.getType())) {
                afterStock = currentStock + changeQty;
                // 更新物资成本（移动加权平均）
                updateMaterialCost(item.getMaterialId(), item.getPrice(), item.getQuantity(), currentStock);
            } else if ("outbound".equals(order.getType())) {
                if (currentStock < changeQty) {
                    throw new RuntimeException("库存不足: " + material.getName());
                }
                afterStock = currentStock - changeQty;
            } else {
                throw new RuntimeException("未知单据类型");
            }

            // 更新库存
            material.setStock(afterStock);
            // 检查预警状态
            checkWarningStatus(material);
            materialService.updateById(material);

            // 记录日志
            inventoryLogService.log(
                    material.getId(),
                    material.getName(),
                    order.getType(), // in/out/adjust
                    "inbound".equals(order.getType()) ? changeQty : -changeQty,
                    currentStock,
                    afterStock,
                    order.getId(),
                    userId,
                    username,
                    order.getRemark()
            );
        }
    }

    private void updateMaterialCost(String materialId, BigDecimal newPrice, Integer newQty, Integer oldQty) {
        if (newPrice == null || newQty == 0) return;
        
        MaterialCost cost = materialCostMapper.selectById(materialId);
        if (cost == null) {
            cost = new MaterialCost();
            cost.setMaterialId(materialId);
            cost.setAvgPrice(newPrice);
            cost.setLastPrice(newPrice);
            cost.setTotalValue(newPrice.multiply(new BigDecimal(newQty)));
            materialCostMapper.insert(cost);
        } else {
            // 计算新平均价 = (旧总值 + 新总值) / (旧数量 + 新数量)
            BigDecimal oldTotalValue = cost.getAvgPrice().multiply(new BigDecimal(oldQty));
            BigDecimal newTotalValue = newPrice.multiply(new BigDecimal(newQty));
            BigDecimal totalValue = oldTotalValue.add(newTotalValue);
            BigDecimal totalQty = new BigDecimal(oldQty + newQty);
            
            if (totalQty.compareTo(BigDecimal.ZERO) > 0) {
                cost.setAvgPrice(totalValue.divide(totalQty, 2, BigDecimal.ROUND_HALF_UP));
            }
            cost.setLastPrice(newPrice);
            cost.setTotalValue(cost.getAvgPrice().multiply(totalQty)); // 更新总价值 based on new avg
            materialCostMapper.updateById(cost);
        }
    }

    private void checkWarningStatus(Material material) {
        if (material.getStock() < material.getThreshold()) {
            material.setStatus("warning");
            // 这里可以触发预警通知，例如发送消息给管理员
            log.warn("物资库存预警: {} 当前库存: {}", material.getName(), material.getStock());
        } else {
            if ("warning".equals(material.getStatus())) {
                material.setStatus("normal");
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void voidOrder(String orderId, Long userId) {
        StockOrder order = getById(orderId);
        if (order == null) return;
        
        if ("approved".equals(order.getStatus())) {
            // 已审核单据作废需要回滚库存，暂时简单处理为不允许作废已审核单据，或者实现反向操作
            // 根据需求，通常作废只能针对未生效单据，已生效需开红字单据
            throw new RuntimeException("已审核单据不能直接作废，请开具红字冲销单");
        }
        
        order.setStatus("void");
        updateById(order);
    }

    @Override
    public Page<StockOrder> getOrderList(Integer page, Integer size, String type, String status, String keyword) {
        Page<StockOrder> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<StockOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StringUtils.hasText(type), StockOrder::getType, type)
               .eq(StringUtils.hasText(status), StockOrder::getStatus, status)
               .like(StringUtils.hasText(keyword), StockOrder::getId, keyword) // 简单按ID搜，也可关联查询
               .orderByDesc(StockOrder::getCreateTime);
        return page(pageParam, wrapper);
    }
}
