package com.epidemic.material.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 捐赠证书实体类
 * 用于记录捐赠方获得的捐赠证书信息
 */
@Data
@TableName("donation_certificate")
public class DonationCertificate implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 证书ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 捐赠ID（关联 donation 表）
     */
    private String donationId;

    /**
     * 捐赠人ID
     */
    private Long donorId;

    /**
     * 捐赠人姓名
     */
    private String donorName;

    /**
     * 证书编号（唯一）
     */
    private String certificateNo;

    /**
     * 捐赠物资类型
     */
    private String materialType;

    /**
     * 捐赠数量
     */
    private Integer quantity;

    /**
     * 物资单位
     */
    private String unit;

    /**
     * 颁发时间
     */
    private LocalDateTime issueTime;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
