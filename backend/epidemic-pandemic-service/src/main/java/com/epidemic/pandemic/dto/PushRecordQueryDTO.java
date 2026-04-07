package com.epidemic.pandemic.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 推送记录查询DTO
 */
@Data
@Schema(description = "推送记录查询参数")
public class PushRecordQueryDTO implements Serializable {

    @Schema(description = "页码")
    private Integer page = 1;

    @Schema(description = "每页大小")
    private Integer size = 10;

    @Schema(description = "推送状态")
    private String status;
}