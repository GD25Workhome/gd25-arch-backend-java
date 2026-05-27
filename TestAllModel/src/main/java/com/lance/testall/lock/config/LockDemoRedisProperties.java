package com.lance.testall.lock.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 实验 6：Redis 分布式锁配置，前缀 lock.redis。
 */
@Data
@ConfigurationProperties(prefix = "lock.redis")
public class LockDemoRedisProperties {

    /** 是否允许 lockStrategy=REDIS；false 时调用 REDIS 将快速失败 */
    private boolean enabled = true;

    /** 锁 Key 前缀，完整 Key 为 prefix + skuId */
    private String keyPrefix = "lock:stock:";

    /** 获取锁最长等待秒数 */
    private int waitSeconds = 3;

    /** 锁过期秒数（防止持有者崩溃导致死锁） */
    private int leaseSeconds = 10;
}
