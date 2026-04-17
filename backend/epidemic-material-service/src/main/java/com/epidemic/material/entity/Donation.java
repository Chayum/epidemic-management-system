package com.epidemic.material.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 捐赠信息实体类
 */
@Data
@TableName("donation")
public class Donation implements Serializable {

    private static final long serialVersionUID = 1L;

    // 主键ID（捐赠单号）
    @TableId(value = "id", type = IdType.INPUT)
    private String id;

    // 物资ID（关联material表）
    private String materialId;

    // 物资名称
    private String materialName;

    // 物资类型
    private String type;

    // 捐赠数量
    private Integer quantity;

    // 单位
    private String unit;

    // 捐赠人ID
    private Long donorId;

    // 捐赠单位
    private String donorUnit;

    // 联系人
    private String contactPerson;

    // 联系电话
    private String contactPhone;

    // 收货地址
    private String receiveAddress;

    // 物资来源
    private String source;

    // 生产日期
    private LocalDate productionDate;

    // 有效期至
    private LocalDate expiryDate;

    // 备注
    private String remark;

    // 状态：pending-待审核，approved-已通过，rejected-已驳回
    private String status;

    // 捐赠时间
    private LocalDateTime donateTime;

    // 审核时间
    private LocalDateTime approveTime;

    // 审核人ID
    private Long approverId;

    // 审核人姓名
    private String approverName;

    // 审核意见
    private String approveRemark;

    // 是否删除：0-否，1-是
    @TableLogic
    private Integer deleted;

    // 创建时间
    private LocalDateTime createTime;

    // 更新时间
    private LocalDateTime updateTime;
}
