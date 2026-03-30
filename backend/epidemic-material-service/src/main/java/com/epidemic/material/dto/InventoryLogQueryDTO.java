package com.epidemic.material.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 库存变动日志查询DTO
 */
@Data
@Schema(description = "库存变动日志查询参数")
public class InventoryLogQueryDTO implements Serializable {

    @Schema(description = "页码")
    private Integer page = 1;

    @Schema(description = "每页大小")
    private Integer size = 10;

    @Schema(description = "物资ID")
    private String materialId;

    @Schema(description = "变动类型")
    private String type;

    @Schema(description = "开始时间")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    @Schema(description = "结束时间")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;
}