package com.epidemic.material.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 物资成本
 */
@Data
@TableName("material_cost")
public class MaterialCost implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "material_id")
    private String materialId;

    private BigDecimal avgPrice;

    private BigDecimal lastPrice;

    private BigDecimal totalValue;

    private LocalDateTime updateTime;
}
