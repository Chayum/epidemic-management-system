package com.epidemic.material.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 库存单据（出入库单）
 */
@Data
@TableName("stock_order")
public class StockOrder implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.INPUT)
    private String id;

    /**
     * 单据类型：inbound-入库单，outbound-出库单
     */
    private String type;

    /**
     * 来源类型：purchase-采购，donation-捐赠，application-申领，transfer-调拨，check-盘点
     */
    private String sourceType;

    /**
     * 来源单号
     */
    private String sourceId;

    /**
     * 状态：draft-草稿，pending-待审核，approved-已审核，void-已作废
     */
    private String status;

    private BigDecimal totalAmount;

    private String supplier;

    private String department;

    private Long handlerId;

    private String handlerName;

    private Long auditorId;

    private String auditorName;

    private LocalDateTime auditTime;

    private String remark;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
