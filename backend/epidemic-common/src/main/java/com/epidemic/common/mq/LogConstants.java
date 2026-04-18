package com.epidemic.common.mq;

/**
 * 消息队列常量配置
 */
public final class LogConstants {

    private LogConstants() {
    }

    // ==================== 日志队列 ====================

    /** 日志交换机名称 */
    public static final String LOG_EXCHANGE = "epidemic.log.exchange";

    /** 日志队列名称 */
    public static final String LOG_QUEUE = "epidemic.log.queue";

    /** 日志路由键 */
    public static final String LOG_ROUTING_KEY = "epidemic.log.routing";

    // ==================== 日志死信队列 ====================

    /** 日志死信交换机名称 */
    public static final String LOG_DLX_EXCHANGE = "epidemic.log.dlx.exchange";

    /** 日志死信队列名称 */
    public static final String LOG_DLX_QUEUE = "epidemic.log.dlx.queue";

    /** 日志死信路由键 */
    public static final String LOG_DLX_ROUTING_KEY = "epidemic.log.dlx.routing";

    // ==================== 推送队列 ====================

    /** 推送交换机名称 */
    public static final String PUSH_EXCHANGE = "pandemic.push.exchange";

    /** 推送队列名称 */
    public static final String PUSH_QUEUE = "pandemic.push.queue";

    /** 推送路由键 */
    public static final String PUSH_ROUTING_KEY = "pandemic.push";

    // ==================== 推送死信队列 ====================

    /** 推送死信交换机名称 */
    public static final String PUSH_DLX_EXCHANGE = "pandemic.push.dlx.exchange";

    /** 推送死信队列名称 */
    public static final String PUSH_DLX_QUEUE = "pandemic.push.dlx.queue";

    /** 推送死信路由键 */
    public static final String PUSH_DLX_ROUTING_KEY = "pandemic.push.dlx";
}