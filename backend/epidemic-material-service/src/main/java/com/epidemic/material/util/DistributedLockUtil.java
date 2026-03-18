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
     * 等待 3 秒，锁持有时间 30 秒，避免死锁
     */
    public <T> T executeWithLock(String lockKey, Supplier<T> action) {
        return executeWithLock(lockKey, action, 3, 30);
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

    /**
     * 执行加锁操作（带重试机制）
     * 
     * @param lockKey 锁的 key
     * @param action 要执行的操作
     * @param maxRetries 最大重试次数
     * @param waitTime 每次等待时间（秒）
     * @param leaseTime 锁持有时间（秒）
     * @return 操作结果
     */
    public <T> T executeWithLockWithRetry(String lockKey, Supplier<T> action, 
                                         int maxRetries, long waitTime, long leaseTime) {
        RuntimeException lastException = null;
        
        for (int retry = 0; retry <= maxRetries; retry++) {
            try {
                return executeWithLock(lockKey, action, waitTime, leaseTime);
            } catch (RuntimeException e) {
                lastException = e;
                if (retry < maxRetries) {
                    log.warn("获取分布式锁失败，第 {} 次重试，key: {}", retry + 1, lockKey);
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("重试被中断", ie);
                    }
                }
            }
        }
        
        log.error("获取分布式锁失败，已重试 {} 次，key: {}", maxRetries, lockKey);
        throw lastException;
    }

    /**
     * 执行加锁操作（带默认重试机制）
     * 默认重试 2 次，等待 3 秒，锁持有 30 秒
     */
    public <T> T executeWithLockWithRetry(String lockKey, Supplier<T> action) {
        return executeWithLockWithRetry(lockKey, action, 2, 3, 30);
    }

    /**
     * 执行加锁操作（带重试机制，无返回值）
     */
    public void executeWithLockWithRetry(String lockKey, Runnable action) {
        executeWithLockWithRetry(lockKey, () -> {
            action.run();
            return null;
        });
    }
}
