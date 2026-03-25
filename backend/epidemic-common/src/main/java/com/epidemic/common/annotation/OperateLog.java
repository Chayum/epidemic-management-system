package com.epidemic.common.annotation;

import java.lang.annotation.*;

/**
 * 操作日志注解
 * 标注在需要记录操作日志的 Controller 方法上
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OperateLog {

    /**
     * 操作模块
     * 例如：疫情新闻、疫情数据、消息推送
     */
    String module();

    /**
     * 操作类型/描述
     * 例如：发布新闻、审核数据、发送推送等
     */
    String operation();
}