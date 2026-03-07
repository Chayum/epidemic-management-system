package com.epidemic.material.util;

import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * 分布式锁工具类
 * 基于 Redisson 实现分布式锁，用于库存扣减、盘点等并发操作
 */
@Component
@Slf4j
public class DistributedLockUtil {

    @Autowired
    private RedissonClient redissonClient;

    /**
     * 执行加锁操作
     * 
     * @param lockKey 锁的 key
     * @param action 要执行的操作
     * @param waitTime 等待时间（秒）
     * @param leaseTime 锁持有时间（秒），-1 表示不自动释放
     * @return 操作结果
     */
    public <T> T executeWithLock(String lockKey, Supplier<T> action, long waitTime, long leaseTime) {
        RLock lock = redissonClient.getLock(lockKey);
        boolean locked = false;
        try {
            // 尝试获取锁
            locked = lock.tryLock(waitTime, leaseTime, TimeUnit.SECONDS);
            if (!locked) {
                log.warn("获取分布式锁失败：{}", lockKey);
                throw new RuntimeException("系统繁忙，请稍后重试");
            }
            log.debug("成功获取分布式锁：{}", lockKey);
            
            // 执行操作
            return action.get();
            
        } catch (InterruptedException e) {
            log.error("获取分布式锁被中断：{}", lockKey, e);
            Thread.currentThread().interrupt();
            throw new RuntimeException("获取锁失败");
        } finally {
            if (locked && lock.isHeldByCurrentThread()) {
                lock.unlock();
                log.debug("释放分布式锁：{}", lockKey);
            }
        }
    }

    /**
     * 执行加锁操作（默认等待时间和锁持有时间）
     */
    public <T> T executeWithLock(String lockKey, Supplier<T> action) {
        return executeWithLock(lockKey, action, 3, -1);
    }

    /**
     * 执行加锁操作（无返回值）
     */
    public void executeWithLock(String lockKey, Runnable action) {
        executeWithLock(lockKey, () -> {
            action.run();
            return null;
        });
    }
}
