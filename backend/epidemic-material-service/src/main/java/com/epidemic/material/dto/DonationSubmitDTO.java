package com.epidemic.material.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * 捐赠申请提交DTO
 */
@Data
@Schema(description = "捐赠申请提交参数")
public class DonationSubmitDTO implements Serializable {

    @Schema(description = "物资名称")
    @NotBlank(message = "物资名称不能为空")
    private String materialName;

    @Schema(description = "物资类型")
    @NotBlank(message = "物资类型不能为空")
    private String type;

    @Schema(description = "捐赠数量")
    @NotNull(message = "捐赠数量不能为空")
    @Min(value = 1, message = "捐赠数量必须大于0")
    private Integer quantity;

    @Schema(description = "单位")
    private String unit;

    @Schema(description = "捐赠单位/个人名称")
    @NotBlank(message = "捐赠方名称不能为空")
    private String donorUnit;

    @Schema(description = "联系人")
    @NotBlank(message = "联系人不能为空")
    private String contactPerson;

    @Schema(description = "联系电话")
    @NotBlank(message = "联系电话不能为空")
    private String contactPhone;

    @Schema(description = "取货地址")
    private String receiveAddress;

    @Schema(description = "来源(personal/enterprise)")
    private String source;

    @Schema(description = "生产日期")
    private LocalDate productionDate;

    @Schema(description = "有效期至")
    private LocalDate expiryDate;

    @Schema(description = "备注")
    private String remark;
}
