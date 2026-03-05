package com.epidemic.material.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 库存单据DTO
 */
@Data
public class StockOrderDTO {

    /**
     * 单据类型：inbound-入库单，outbound-出库单
     */
    @NotBlank(message = "单据类型不能为空")
    private String type;

    /**
     * 来源类型
     */
    private String sourceType;

    private String sourceId;

    private String supplier;

    private String department;

    private String remark;

    @NotEmpty(message = "单据明细不能为空")
    private List<StockOrderItemDTO> items;

    @Data
    public static class StockOrderItemDTO {
        @NotBlank(message = "物资ID不能为空")
        private String materialId;

        @NotNull(message = "数量不能为空")
        private Integer quantity;

        private BigDecimal price;

        private String batchNo;

        private LocalDate expiryDate;

        private String remark;
    }
}
