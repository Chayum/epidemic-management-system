package com.epidemic.material.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 捐赠查询DTO
 */
@Data
@Schema(description = "捐赠查询参数")
public class DonationQueryDTO implements Serializable {

    @Schema(description = "页码")
    private Integer page = 1;

    @Schema(description = "每页大小")
    private Integer size = 10;

    @Schema(description = "状态")
    private String status;

    @Schema(description = "捐赠单位")
    private String donorUnit;

    @Schema(description = "捐赠人ID")
    private Long donorId;
    
    @Schema(description = "物资类型")
    private String type;
    
    @Schema(description = "捐赠单ID")
    private String id;

    @Schema(description = "关键字搜索（捐赠单号）")
    private String keyword;
}
