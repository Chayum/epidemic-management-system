package com.epidemic.material.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 库存台账VO
 */
@Data
public class InventoryLedgerVO {
    private String materialId;
    private String materialName;
    private String specification;
    private String unit;
    private Integer stock;
    private BigDecimal avgPrice;
    private BigDecimal totalValue;
    private Integer threshold;
    private String status; // normal, warning, etc.
    private String warehouse;
}
