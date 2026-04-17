package com.epidemic.material.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 申请单实体类
 */
@Data
@TableName("application")
public class Application implements Serializable {

    private static final long serialVersionUID = 1L;

    // 申请单号
    @TableId(value = "id", type = IdType.INPUT)
    private String id;

    // 物资ID
    private String materialId;

    // 物资名称
    private String materialName;

    // 申请数量
    private Integer quantity;

    // 单位
    private String unit;

    // 申请人ID
    private Long applicantId;

    // 申请人姓名
    private String applicantName;

    // 申请人单位
    @TableField(value = "applicant_unit")
    private String applicantUnit;

    // 用途说明
    private String purpose;

    // 紧急程度：normal-普通，urgent-较急，critical-紧急
    private String urgency;

    // 收货地址
    private String address;

    // 收货人
    private String receiver;

    // 收货电话
    private String receiverPhone;

    // 状态：pending-待审核，approved-已通过，rejected-已驳回，cancelled-已取消，delivered-已发货，received-已收货
    private String status;

    // 申请时间
    private LocalDateTime applyTime;

    // 审核时间
    private LocalDateTime approveTime;

    // 审核人ID
    private Long approverId;

    // 审核人姓名
    private String approverName;

    // 审核意见
    private String approveRemark;

    // 是否删除：0-否，1-是
    @TableLogic
    private Integer deleted;

    // 创建时间
    private LocalDateTime createTime;

    // 更新时间
    private LocalDateTime updateTime;

    // ========== 以下为非数据库字段（用于业务逻辑） ==========

    // 申领原因（非数据库字段）
    @TableField(exist = false)
    private String reason;

    // 捐赠人ID（非数据库字段，用于关联查询）
    @TableField(exist = false)
    private Long donorId;

    // 捐赠人姓名（非数据库字段）
    @TableField(exist = false)
    private String donorName;

    // 备注（非数据库字段）
    @TableField(exist = false)
    private String remark;
}
