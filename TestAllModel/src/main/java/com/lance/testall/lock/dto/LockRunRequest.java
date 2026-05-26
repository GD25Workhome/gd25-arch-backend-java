package com.lance.testall.lock.dto;

import lombok.Data;

/**
 * POST /api/lock-demo/run 请求体。
 */
@Data
public class LockRunRequest {

    private String lockStrategy = "NONE";
    private String skuId;
    private Integer initialStock = 100;
    private Integer threadCount = 200;
    private Integer requestsPerThread = 1;
    private Integer poolCoreSize;
    private Integer poolMaxSize;
    private Boolean resetStockBeforeRun = true;
    private Integer tryLockTimeoutMs;
    /** 实验 3a：Semaphore 许可数，仅 lockStrategy=SEMAPHORE 时生效 */
    private Integer semaphorePermits;
    /** 实验 4a：乐观锁最大重试次数 */
    private Integer optimisticMaxRetries;
    /** 实验 4c / 5b：持锁或事务内模拟延迟（毫秒），扩大冲突/阻塞窗口 */
    private Integer simulateDelayMs;
    private String batchTag;
}
