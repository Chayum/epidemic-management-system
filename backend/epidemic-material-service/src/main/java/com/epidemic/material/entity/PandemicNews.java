package com.epidemic.material.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 疫情新闻实体类
 */
@Data
@TableName("pandemic_news")
public class PandemicNews implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private String id;

    private String title;

    private String summary;

    private String content;

    private String author;

    private String status;

    private Integer viewCount;

    private LocalDateTime publishTime;

    private LocalDateTime createTime;
}
