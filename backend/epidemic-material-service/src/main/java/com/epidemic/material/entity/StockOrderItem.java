package com.epidemic.material.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 库存单据明细
 */
@Data
@TableName("stock_order_item")
public class StockOrderItem implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String orderId;

    private String materialId;

    private String materialName;

    private String specification;

    private String unit;

    private Integer quantity;

    private BigDecimal price;

    private BigDecimal amount;

    private String batchNo;

    private LocalDate expiryDate;

    private String remark;
}
