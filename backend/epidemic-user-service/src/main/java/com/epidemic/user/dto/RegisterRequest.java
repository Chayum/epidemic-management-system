package com.epidemic.user.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * 注册请求DTO
 * 用于用户注册时的参数校验
 */
@Data
public class RegisterRequest {

    // 用户名：必填，3-20位，只能包含字母数字下划线
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 20, message = "用户名长度3-20位")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "用户名只能包含字母、数字、下划线")
    private String username;

    // 密码：必填，6-20位
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度6-20位")
    private String password;

    // 姓名：必填
    @NotBlank(message = "姓名不能为空")
    private String name;

    // 手机号：必填，格式校验
    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    // 所属单位：选填
    private String unit;

    // 角色：必填，只能选择申请方或捐赠方
    @NotBlank(message = "请选择角色")
    @Pattern(regexp = "^(applicant|donor)$", message = "角色只能选择申请方或捐赠方")
    private String role;
}
