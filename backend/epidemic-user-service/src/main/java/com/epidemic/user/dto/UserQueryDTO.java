package com.epidemic.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 用户查询DTO
 */
@Data
@Schema(description = "用户查询参数")
public class UserQueryDTO implements Serializable {

    @Schema(description = "页码")
    private Integer page = 1;

    @Schema(description = "每页大小")
    private Integer size = 10;

    @Schema(description = "用户名（模糊查询）")
    private String username;

    @Schema(description = "姓名（模糊查询）")
    private String name;

    @Schema(description = "电话")
    private String phone;

    @Schema(description = "角色")
    private String role;

    @Schema(description = "状态")
    private String status;
}