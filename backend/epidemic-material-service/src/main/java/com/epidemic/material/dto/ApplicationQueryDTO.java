package com.epidemic.material.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 物资申领查询DTO
 */
@Data
@Schema(description = "物资申领查询参数")
public class ApplicationQueryDTO implements Serializable {

    @Schema(description = "页码")
    private Integer page = 1;

    @Schema(description = "每页大小")
    private Integer size = 10;

    @Schema(description = "申请状态")
    private String status;

    @Schema(description = "申请状态列表（支持多状态查询，逗号分隔）")
    private String statuses;

    @Schema(description = "申请人ID")
    private Long applicantId;

    @Schema(description = "申请人姓名(模糊查询)")
    private String applicantName;
}
