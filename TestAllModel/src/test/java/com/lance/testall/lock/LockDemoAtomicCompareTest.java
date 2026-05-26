package com.lance.testall.lock;

import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * 实验 3c：AtomicInteger 适用于简单累加，不能替代数据库「读-改-写」库存扣减。
 */
class LockDemoAtomicCompareTest {

    private static final int THREADS = 100;
    private static final int PER_THREAD = 2;
    private static final int EXPECTED = THREADS * PER_THREAD;

    @Test
    void atomicIncrement_alwaysExact() throws InterruptedException {
        AtomicInteger counter = new AtomicInteger();
        ExecutorService pool = Executors.newFixedThreadPool(50);
        CountDownLatch latch = new CountDownLatch(EXPECTED);

        for (int i = 0; i < EXPECTED; i++) {
            pool.execute(() -> {
                try {
                    counter.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        pool.shutdown();
        assertEquals(EXPECTED, counter.get(), "内存 AtomicInteger 并发累加应精确");
    }
}
