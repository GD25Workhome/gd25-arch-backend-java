package com.lance.testall.lock.dto;

import java.util.concurrent.Semaphore;

/**
 * 单次扣减的扩展参数（由 {@link com.lance.testall.lock.service.LockDemoService#run} 构造并传入底层）。
 */
public class DeductOptions {

    private final int tryLockTimeoutMs;
    private final int optimisticMaxRetries;
    private final Semaphore semaphore;
    private final int simulateDelayMs;

    public DeductOptions(int tryLockTimeoutMs, int optimisticMaxRetries, Semaphore semaphore, int simulateDelayMs) {
        this.tryLockTimeoutMs = tryLockTimeoutMs;
        this.optimisticMaxRetries = optimisticMaxRetries;
        this.semaphore = semaphore;
        this.simulateDelayMs = simulateDelayMs;
    }

    public int getTryLockTimeoutMs() {
        return tryLockTimeoutMs;
    }

    public int getOptimisticMaxRetries() {
        return optimisticMaxRetries;
    }

    public Semaphore getSemaphore() {
        return semaphore;
    }

    public int getSimulateDelayMs() {
        return simulateDelayMs;
    }
}
