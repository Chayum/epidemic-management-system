package com.epidemic.material.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

/**
 * 捐赠审批DTO
 */
@Data
@Schema(description = "捐赠审批参数")
public class DonationApproveDTO implements Serializable {

    @Schema(description = "捐赠单ID")
    @NotBlank(message = "捐赠单ID不能为空")
    private String donationId;

    @Schema(description = "审批状态(approved/rejected)")
    @NotBlank(message = "审批状态不能为空")
    private String status;

    @Schema(description = "审批备注")
    private String remark;
    
    @Schema(description = "目标入库物资ID(若为空则仅通过审核不增加库存)")
    private String targetMaterialId;
}
