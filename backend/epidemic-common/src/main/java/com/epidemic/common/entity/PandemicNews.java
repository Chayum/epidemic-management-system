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

    // 新闻ID
    private String id;

    // 标题
    private String title;

    // 摘要
    private String summary;

    // 内容
    private String content;

    // 封面图
    private String coverImage;

    // 作者
    private String author;

    // 作者ID
    private Long authorId;

    // 状态：published-已发布，draft-草稿
    private String status;

    // 浏览次数
    private Integer viewCount;

    // 发布时间
    private LocalDateTime publishTime;

    // 是否删除：0-否，1-是
    private Integer deleted;

    // 创建时间
    private LocalDateTime createTime;

    // 更新时间
    private LocalDateTime updateTime;
}