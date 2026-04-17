package com.epidemic.material.vo;

import lombok.Data;

/**
 * 库存台账VO
 */
@Data
public class InventoryLedgerVO {
    // 物资ID
    private String materialId;
    // 物资名称
    private String materialName;
    // 规格型号
    private String specification;
    // 单位
    private String unit;
    // 库存数量
    private Integer stock;
    // 预警阈值
    private Integer threshold;
    // 状态：normal-正常，warning-预警
    private String status;
    // 存放仓库
    private String warehouse;
}
