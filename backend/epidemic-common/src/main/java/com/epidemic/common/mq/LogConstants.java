package com.epidemic.common.mq;

/**
 * 日志消息队列常量配置
 */
public final class LogConstants {

    private LogConstants() {
    }

    /** 交换机名称 */
    public static final String LOG_EXCHANGE = "epidemic.log.exchange";

    /** 队列名称 */
    public static final String LOG_QUEUE = "epidemic.log.queue";

    /** 路由键 */
    public static final String LOG_ROUTING_KEY = "epidemic.log.routing";
}