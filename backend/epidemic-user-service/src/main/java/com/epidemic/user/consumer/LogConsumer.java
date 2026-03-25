package com.epidemic.user.consumer;

import com.epidemic.common.entity.OperateLog;
import com.epidemic.common.mq.LogConstants;
import com.epidemic.user.mapper.OperateLogMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * 日志消息消费者
 * 从消息队列接收日志并保存到数据库
 */
@Component
@Slf4j
public class LogConsumer {

    private final OperateLogMapper operateLogMapper;

    public LogConsumer(OperateLogMapper operateLogMapper) {
        this.operateLogMapper = operateLogMapper;
    }

    /**
     * 监听日志队列，接收并保存日志
     */
    @RabbitListener(queues = LogConstants.LOG_QUEUE)
    public void handleLogMessage(OperateLog operateLog) {
        try {
            log.info("收到操作日志: module={}, operation={}, user={}, ip={}",
                    operateLog.getModule(), operateLog.getOperation(),
                    operateLog.getUsername(), operateLog.getIp());
            operateLogMapper.insert(operateLog);
            log.debug("操作日志保存成功: id={}", operateLog.getId());
        } catch (Exception e) {
            log.error("保存操作日志失败: {}, operateLog={}", e.getMessage(), operateLog, e);
        }
    }
}