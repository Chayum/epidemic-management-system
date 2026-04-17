package com.epidemic.user.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户实体类
 */
@Data
@TableName("sys_user")
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码
     */
    private String password;

    /**
     * 姓名
     */
    private String name;

    /**
     * 角色：admin-管理员，applicant-申请方(医院/社区)，donor-捐赠方
     */
    private String role;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 所属单位
     */
    private String unit;

    /**
     * 头像
     */
    private String avatar;

    /**
     * 状态：active-正常，disabled-禁用
     */
    private String status;

    /**
     * 是否删除：0-否，1-是
     */
    @TableLogic
    private Integer deleted;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
