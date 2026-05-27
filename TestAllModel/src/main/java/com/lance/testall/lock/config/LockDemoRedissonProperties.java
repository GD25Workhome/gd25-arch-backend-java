package com.lance.testall.lock.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 实验 6d：Redisson 分布式锁配置，前缀 lock.redisson。
 * <p>
 * 等待时间复用 {@link LockDemoRedisProperties#getWaitSeconds()}；租约由看门狗续期，不使用 lock.redis.lease-seconds。
 */
@Data
@ConfigurationProperties(prefix = "lock.redisson")
public class LockDemoRedissonProperties {

    /** 是否允许 lockStrategy=REDIS_REDISSON；通常与 lock.redis.enabled 同时为 true */
    private boolean enabled = true;

    /** 锁 Key 前缀，完整 Key 为 prefix + skuId（与自研 REDIS 前缀隔离） */
    private String keyPrefix = "lock:redisson:stock:";

    /** 看门狗单次续期目标 TTL（秒），映射 Redisson Config.lockWatchdogTimeout */
    private int watchdogTimeoutSeconds = 30;
}
