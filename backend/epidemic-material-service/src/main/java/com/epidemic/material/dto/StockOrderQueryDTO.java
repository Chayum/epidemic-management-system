package com.epidemic.material.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 单据查询DTO
 */
@Data
@Schema(description = "单据查询参数")
public class StockOrderQueryDTO implements Serializable {

    @Schema(description = "页码")
    private Integer page = 1;

    @Schema(description = "每页大小")
    private Integer size = 10;

    @Schema(description = "单据类型")
    private String type;

    @Schema(description = "单据状态")
    private String status;

    @Schema(description = "关键词搜索")
    private String keyword;
}