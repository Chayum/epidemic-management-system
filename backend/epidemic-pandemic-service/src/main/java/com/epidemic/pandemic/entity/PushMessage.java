package com.epidemic.pandemic.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 推送消息实体
 * 用于 RabbitMQ 消息队列的消息对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PushMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 推送记录ID */
    private Long pushRecordId;

    /** 推送标题 */
    private String title;

    /** 推送内容 */
    private String content;

    /** 推送目标：all-全部用户，applicant-申请方，donor-捐赠方 */
    private String target;

    /** 推送渠道列表（逗号分隔） */
    private String channels;

    /** 推送时间 */
    private LocalDateTime pushTime;

    /** 创建时间 */
    private LocalDateTime createTime;
}
