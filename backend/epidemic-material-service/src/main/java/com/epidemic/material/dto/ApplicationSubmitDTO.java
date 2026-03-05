package com.epidemic.material.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * 物资申领提交DTO
 */
@Data
@Schema(description = "物资申领提交参数")
public class ApplicationSubmitDTO implements Serializable {

    @Schema(description = "物资ID")
    @NotBlank(message = "物资ID不能为空")
    private String materialId;

    @Schema(description = "申请数量")
    @NotNull(message = "申请数量不能为空")
    @Min(value = 1, message = "申请数量必须大于0")
    private Integer quantity;

    @Schema(description = "用途说明")
    @NotBlank(message = "用途说明不能为空")
    private String purpose;

    @Schema(description = "紧急程度(normal/urgent/critical)")
    @NotBlank(message = "紧急程度不能为空")
    private String urgency;

    @Schema(description = "收货地址")
    @NotBlank(message = "收货地址不能为空")
    private String address;

    @Schema(description = "收货人")
    @NotBlank(message = "收货人不能为空")
    private String receiver;

    @Schema(description = "联系电话")
    @NotBlank(message = "联系电话不能为空")
    private String receiverPhone;

//    @Schema(description = "申请单位")
//    @NotBlank(message = "申请单位不能为空")
//    private String applicantUnit;
}
