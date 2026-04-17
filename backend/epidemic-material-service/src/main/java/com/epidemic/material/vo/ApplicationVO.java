package com.epidemic.material.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 物资申领展示VO
 */
@Data
@Schema(description = "物资申领展示对象")
public class ApplicationVO implements Serializable {

    @Schema(description = "申请单ID")
    private String id;

    @Schema(description = "物资ID")
    private String materialId;

    @Schema(description = "物资名称")
    private String materialName;

    @Schema(description = "申请数量")
    private Integer quantity;

    @Schema(description = "单位")
    private String unit;

    @Schema(description = "申请人ID")
    private Long applicantId;

    @Schema(description = "申请人姓名")
    private String applicantName;

    @Schema(description = "用途说明")
    private String purpose;

    @Schema(description = "紧急程度")
    private String urgency;

    @Schema(description = "收货地址")
    private String address;

    @Schema(description = "收货人")
    private String receiver;

    @Schema(description = "联系电话")
    private String receiverPhone;

    @Schema(description = "状态")
    private String status;

    @Schema(description = "申请时间")
    private LocalDateTime applyTime;

    @Schema(description = "审批时间")
    private LocalDateTime approveTime;

    @Schema(description = "审批人ID")
    private Long approverId;

    @Schema(description = "审批人姓名")
    private String approverName;

    @Schema(description = "审批备注")
    private String approveRemark;

    @Schema(description = "申请单位")
    private String applicantUnit;
}
