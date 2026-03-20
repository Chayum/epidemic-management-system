package com.epidemic.user.annotation;

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
     * 例如：用户管理、认证授权
     */
    String module();

    /**
     * 操作类型/描述
     * 例如：用户登录、创建用户、更新用户等
     */
    String operation();
}