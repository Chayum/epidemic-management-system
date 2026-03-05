package com.epidemic.material.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 库存变动记录实体
 */
@Data
@TableName("inventory_log")
public class InventoryLog implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String materialId;

    private String materialName;

    /**
     * 变动类型：in-入库，out-出库，donation-捐赠，adjust-盘点调整
     */
    private String changeType;

    /**
     * 变动数量（正数增加，负数减少）
     */
    private Integer changeQuantity;

    private Integer beforeQuantity;

    private Integer afterQuantity;

    /**
     * 关联单号（申请单号或捐赠单号或出入库单号）
     */
    private String relatedId;

    private String remark;

    private Long operatorId;

    private String operatorName;

    private LocalDateTime operateTime;
}
