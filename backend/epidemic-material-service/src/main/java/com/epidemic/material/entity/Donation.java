package com.epidemic.material.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
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

    @TableId(value = "id", type = IdType.INPUT)
    private String id;

    private String materialName;

    private String type;

    private Integer quantity;

    private String unit;

    private Long donorId;

    private String donorUnit;

    private String contactPerson;

    private String contactPhone;

    private String receiveAddress;

    private String source;

    private LocalDate productionDate;

    private LocalDate expiryDate;

    private String status;

    private LocalDateTime donateTime;

    private LocalDateTime approveTime;

    private String remark;
}
