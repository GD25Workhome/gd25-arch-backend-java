package com.lance.testall.lock.service;

import com.lance.testall.lock.config.LockDemoRedisProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Collections;
import java.util.UUID;

/**
 * 实验 6：基于 Redis SET NX + TTL 与 Lua 安全释放的分布式锁。
 */
@Service
public class RedisStockLockService {

    private static final Logger log = LoggerFactory.getLogger(RedisStockLockService.class);

    /**
     * 仅当 Key 存在且 value 与 token 一致时才删除，避免误删其它客户端的锁。
     */
    private static final DefaultRedisScript<Long> UNLOCK_SCRIPT = new DefaultRedisScript<>(
            """
                    if redis.call('get', KEYS[1]) == ARGV[1] then
                        return redis.call('del', KEYS[1])
                    else
                        return 0
                    end
                    """,
            Long.class
    );

    private final StringRedisTemplate redisTemplate;
    private final LockDemoRedisProperties redisProperties;

    /** 区分多实例，写入锁 value 便于排查 */
    private final String instanceTokenPrefix;

    public RedisStockLockService(
            StringRedisTemplate redisTemplate,
            LockDemoRedisProperties redisProperties,
            @Value("${spring.application.name:test-all-model}") String applicationName,
            @Value("${server.port:8080}") int serverPort) {
        this.redisTemplate = redisTemplate;
        this.redisProperties = redisProperties;
        this.instanceTokenPrefix = applicationName + ":" + serverPort + ":" + UUID.randomUUID();
    }

    /**
     * 尝试获取 sku 对应分布式锁。
     *
     * @return 成功时返回本次锁 token（释放时须传入）；失败返回 null
     */
    public String tryLock(String skuId) {
        assertRedisEnabled();
        String lockKey = lockKey(skuId);
        String token = instanceTokenPrefix + ":" + Thread.currentThread().getId();
        long deadlineMs = System.currentTimeMillis() + redisProperties.getWaitSeconds() * 1000L;
        Duration lease = Duration.ofSeconds(redisProperties.getLeaseSeconds());

        while (System.currentTimeMillis() < deadlineMs) {
            try {
                Boolean acquired = redisTemplate.opsForValue().setIfAbsent(lockKey, token, lease);
                if (Boolean.TRUE.equals(acquired)) {
                    return token;
                }
            } catch (Exception ex) {
                log.warn("Redis 加锁异常, key={}", lockKey, ex);
                throw new IllegalStateException("Redis 不可用，无法执行 REDIS 策略: " + ex.getMessage(), ex);
            }
            sleepBriefly();
        }
        return null;
    }

    /**
     * 释放锁；token 不匹配时不删除。
     */
    public void unlock(String skuId, String token) {
        if (token == null) {
            return;
        }
        String lockKey = lockKey(skuId);
        try {
            Long result = redisTemplate.execute(
                    UNLOCK_SCRIPT,
                    Collections.singletonList(lockKey),
                    token
            );
            if (log.isTraceEnabled()) {
                log.trace("Redis 释放锁 key={}, deleted={}", lockKey, result);
            }
        } catch (Exception ex) {
            log.warn("Redis 释放锁异常, key={}", lockKey, ex);
        }
    }

    public void assertRedisEnabled() {
        if (!redisProperties.isEnabled()) {
            throw new IllegalStateException("lock.redis.enabled=false，禁止使用 REDIS 锁策略");
        }
    }

    private String lockKey(String skuId) {
        return redisProperties.getKeyPrefix() + skuId;
    }

    private static void sleepBriefly() {
        try {
            Thread.sleep(50);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("等待 Redis 锁被中断", ex);
        }
    }
}
