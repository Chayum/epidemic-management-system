package com.epidemic.material.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

/**
 * 物资审批DTO
 */
@Data
@Schema(description = "物资审批参数")
public class ApplicationApproveDTO implements Serializable {

    @Schema(description = "申请单ID")
    @NotBlank(message = "申请单ID不能为空")
    private String applicationId;

    @Schema(description = "审批状态(approved/rejected)")
    @NotBlank(message = "审批状态不能为空")
    private String status;

    @Schema(description = "审批备注")
    private String remark;
}
