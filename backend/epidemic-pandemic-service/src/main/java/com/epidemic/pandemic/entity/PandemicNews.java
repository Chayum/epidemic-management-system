package com.epidemic.pandemic.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
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

    // 新闻ID
    @TableId(value = "id", type = IdType.INPUT)
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
    @TableLogic
    private Integer deleted;

    // 创建时间
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    // 更新时间
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
