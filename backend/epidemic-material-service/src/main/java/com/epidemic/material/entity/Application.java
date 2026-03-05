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

    @TableId(value = "id", type = IdType.INPUT)
    private String id;

    private String materialId;
    
    private String materialName;

    private Integer quantity;

    private String unit;

    private Long applicantId;

    private String applicantName;

    /**
     * 申请人单位
     */
    @TableField(value = "applicant_unit")
    private String department;

    /**
     * 审核人姓名
     */
    private String approverName;

    private String purpose;

    private String urgency;

    private String address;

    private String receiver;

    private String receiverPhone;

    private String status;

    private LocalDateTime applyTime;

    private LocalDateTime approveTime;

    private Long approverId;

    private String approveRemark;
}
