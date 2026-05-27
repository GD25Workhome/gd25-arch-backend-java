package com.lance.testall.lock.service;

import com.lance.testall.lock.config.LockDemoRedisProperties;
import com.lance.testall.lock.config.LockDemoRedissonProperties;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * 实验 6d：基于 Redisson {@link RLock} 的分布式锁（默认启用看门狗续期）。
 * <p>
 * 使用 {@link RLock#tryLock(long, TimeUnit)} 两参数形式，不指定 leaseTime，由看门狗维持 TTL。
 */
@Service
@ConditionalOnBean(RedissonClient.class)
public class RedissonStockLockService {

    private static final Logger log = LoggerFactory.getLogger(RedissonStockLockService.class);

    private final RedissonClient redissonClient;
    private final LockDemoRedisProperties redisProperties;
    private final LockDemoRedissonProperties redissonProperties;

    public RedissonStockLockService(
            RedissonClient redissonClient,
            LockDemoRedisProperties redisProperties,
            LockDemoRedissonProperties redissonProperties) {
        this.redissonClient = redissonClient;
        this.redisProperties = redisProperties;
        this.redissonProperties = redissonProperties;
    }

    /**
     * 尝试获取 sku 对应分布式锁（看门狗续期）。
     *
     * @return 成功 true；等待 {@link LockDemoRedisProperties#getWaitSeconds()} 后仍失败 false
     */
    public boolean tryLock(String skuId) {
        assertEnabled();
        RLock lock = lock(skuId);
        try {
            return lock.tryLock(redisProperties.getWaitSeconds(), TimeUnit.SECONDS);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("等待 Redisson 锁被中断", ex);
        } catch (Exception ex) {
            log.warn("Redisson 加锁异常, key={}", lockKey(skuId), ex);
            throw new IllegalStateException("Redis/Redisson 不可用，无法执行 REDIS_REDISSON 策略: " + ex.getMessage(), ex);
        }
    }

    /**
     * 释放锁；仅当当前线程持有锁时 unlock。
     */
    public void unlock(String skuId) {
        RLock lock = lock(skuId);
        try {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        } catch (Exception ex) {
            log.warn("Redisson 释放锁异常, key={}", lockKey(skuId), ex);
        }
    }

    public void assertEnabled() {
        if (!redisProperties.isEnabled()) {
            throw new IllegalStateException("lock.redis.enabled=false，禁止使用 REDIS_REDISSON 策略");
        }
        if (!redissonProperties.isEnabled()) {
            throw new IllegalStateException("lock.redisson.enabled=false，禁止使用 REDIS_REDISSON 策略");
        }
    }

    private RLock lock(String skuId) {
        return redissonClient.getLock(lockKey(skuId));
    }

    private String lockKey(String skuId) {
        return redissonProperties.getKeyPrefix() + skuId;
    }
}
