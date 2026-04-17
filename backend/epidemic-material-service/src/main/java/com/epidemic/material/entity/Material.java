package com.epidemic.material.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 物资实体类
 */
@Data
@TableName("material")
public class Material implements Serializable {

    private static final long serialVersionUID = 1L;

    // 物资ID
    @TableId(value = "id", type = IdType.INPUT)
    private String id;

    // 物资名称
    private String name;

    // 物资类型：protective-防护，disinfection-消杀，testing-检测，equipment-设备，other-其他
    private String type;

    // 规格型号
    private String specification;

    // 单位
    private String unit;

    // 库存数量
    private Integer stock;

    // 预警阈值
    private Integer threshold;

    // 仓库
    private String warehouse;

    // 库位
    private String location;

    // 描述
    private String description;

    // 状态：normal-正常，warning-预警，low-低库存
    private String status;

    // 是否删除：0-否，1-是
    @TableLogic
    private Integer deleted;

    // 创建时间
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    // 更新时间
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
