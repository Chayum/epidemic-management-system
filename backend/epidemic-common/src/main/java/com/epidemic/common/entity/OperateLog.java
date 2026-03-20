package com.epidemic.common.entity;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 操作日志实体
 */
@Data
public class OperateLog implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 日志ID
     */
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 操作模块
     */
    private String module;

    /**
     * 操作描述
     */
    private String operation;

    /**
     * 请求方法
     */
    private String method;

    /**
     * 请求参数
     */
    private String params;

    /**
     * IP地址
     */
    private String ip;

    /**
     * 状态：0-失败，1-成功
     */
    private Integer status;

    /**
     * 错误信息
     */
    private String errorMsg;

    /**
     * 执行时长（毫秒）
     */
    private Long executeTime;

    /**
     * 操作时间
     */
    private LocalDateTime operateTime;
}