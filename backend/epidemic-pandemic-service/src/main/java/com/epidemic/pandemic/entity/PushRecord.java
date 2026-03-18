package com.epidemic.pandemic.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 推送记录实体类
 */
@Data
@TableName("push_record")
public class PushRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String title;

    private String content;

    private String target;

    private String channels;

    private String status;

    private LocalDateTime pushTime;

    private LocalDateTime createTime;
}
