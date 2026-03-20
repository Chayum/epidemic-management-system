package com.epidemic.material.annotation;

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
     * 例如：物资申请、捐赠管理、物资管理、库存管理、出入库
     */
    String module();

    /**
     * 操作类型/描述
     * 例如：提交申请、审核申请、新增物资、删除物资等
     */
    String operation();
}