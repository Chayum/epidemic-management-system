package com.epidemic.material.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 捐赠展示VO
 */
@Data
@Schema(description = "捐赠展示对象")
public class DonationVO implements Serializable {

    @Schema(description = "捐赠单ID")
    private String id;

    @Schema(description = "物资名称")
    private String materialName;

    @Schema(description = "物资类型")
    private String type;

    @Schema(description = "捐赠数量")
    private Integer quantity;

    @Schema(description = "单位")
    private String unit;

    @Schema(description = "捐赠人ID")
    private Long donorId;

    @Schema(description = "捐赠单位/个人名称")
    private String donorUnit;

    @Schema(description = "联系人")
    private String contactPerson;

    @Schema(description = "联系电话")
    private String contactPhone;

    @Schema(description = "取货地址")
    private String receiveAddress;

    @Schema(description = "来源")
    private String source;

    @Schema(description = "生产日期")
    private LocalDate productionDate;

    @Schema(description = "有效期至")
    private LocalDate expiryDate;

    @Schema(description = "状态")
    private String status;

    @Schema(description = "捐赠时间")
    private LocalDateTime donateTime;

    @Schema(description = "审核时间")
    private LocalDateTime approveTime;

    @Schema(description = "审核人ID")
    private Long approverId;

    @Schema(description = "审核人姓名")
    private String approverName;

    @Schema(description = "审核备注")
    private String approveRemark;

    @Schema(description = "备注")
    private String remark;
}
