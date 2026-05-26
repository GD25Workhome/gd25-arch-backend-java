package com.lance.testall.lock.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 锁实验配置，前缀 lock.demo（见 application.yml）。
 */
@Data
@ConfigurationProperties(prefix = "lock.demo")
public class LockDemoProperties {

    /** 请求未传 skuId 时的默认商品 */
    private String defaultSkuId = "SKU-DEMO-001";

    /** 每批 /run 创建临时线程池时的默认参数 */
    private ThreadPool threadPool = new ThreadPool();

    /** {@link com.lance.testall.lock.entity.LockStrategy#REENTRANT_TRY} 默认等待毫秒数 */
    private int tryLockTimeoutMs = 100;

    /** {@link com.lance.testall.lock.entity.LockStrategy#SEMAPHORE} 默认许可数 */
    private int semaphorePermits = 10;

    /** {@link com.lance.testall.lock.entity.LockStrategy#DB_OPTIMISTIC} 默认最大重试 */
    private int optimisticMaxRetries = 5;

    @Data
    public static class ThreadPool {

        private int corePoolSize = 50;
        private int maxPoolSize = 50;
        private int queueCapacity = 500;
        /** 线程名前缀，便于 jstack / 日志辨认 */
        private String threadNamePrefix = "lock-demo-";
    }
}
