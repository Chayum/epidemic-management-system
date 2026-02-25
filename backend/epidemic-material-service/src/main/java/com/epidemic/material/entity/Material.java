package com.epidemic.material.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
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

    @TableId(value = "id", type = IdType.INPUT)
    private String id;

    private String name;

    private String type;

    private String specification;

    private String unit;

    private Integer stock;

    private Integer threshold;

    private String warehouse;

    private String description;

    private String status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
