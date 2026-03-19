package com.epidemic.common.entity;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 疫情新闻实体类
 * 用于在各服务间通过 Feign 传递数据
 */
@Data
public class PandemicNews implements Serializable {

    private static final long serialVersionUID = 1L;

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