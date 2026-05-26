package com.lance.testall.lock;

import com.lance.testall.lock.dto.LockRunRequest;
import com.lance.testall.lock.dto.LockRunResponse;
import com.lance.testall.lock.service.LockDemoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class LockDemoServiceTest {

    private static final int INITIAL = 100;
    private static final int THREADS = 50;
    private static final int PER_THREAD = 4;

    @Autowired
    private LockDemoService lockDemoService;

    @Test
    void run_reentrant_noAnomaly_correctCounts() {
        LockRunResponse response = runWithStrategy("REENTRANT");
        assertFalse(response.getAnomaly(), "加锁后不应超卖: " + response);
        assertEquals(INITIAL, response.getSuccessCount());
        assertEquals(THREADS * PER_THREAD - INITIAL, response.getFailCount());
        assertEquals(0, response.getFinalStock());
    }

    @Test
    void run_syncStatic_noAnomaly_correctCounts() {
        LockRunResponse response = runWithStrategy("SYNC_STATIC");
        assertFalse(response.getAnomaly());
        assertEquals(INITIAL, response.getSuccessCount());
        assertEquals(0, response.getFinalStock());
    }

    @Test
    void run_dbAtomicUpdate_noAnomaly() {
        LockRunResponse response = runWithStrategy("DB_ATOMIC_UPDATE");
        assertFalse(response.getAnomaly());
        assertEquals(INITIAL, response.getSuccessCount());
        assertEquals(0, response.getFinalStock());
    }

    @Test
    void run_dbOptimistic_noAnomaly() {
        LockRunResponse response = runWithStrategy("DB_OPTIMISTIC");
        assertFalse(response.getAnomaly());
        assertEquals(INITIAL, response.getSuccessCount());
        assertEquals(0, response.getFinalStock());
    }

    @Test
    void run_dbPessimistic_noAnomaly() {
        LockRunResponse response = runWithStrategy("DB_PESSIMISTIC");
        assertFalse(response.getAnomaly());
        assertEquals(INITIAL, response.getSuccessCount());
        assertEquals(0, response.getFinalStock());
    }

    @Test
    void run_semaphore_noAnomaly() {
        LockRunRequest request = baseRequest("SEMAPHORE");
        request.setSemaphorePermits(10);
        LockRunResponse response = lockDemoService.run(request);
        assertFalse(response.getAnomaly());
        assertEquals(INITIAL, response.getSuccessCount());
        assertEquals(0, response.getFinalStock());
    }

    @Test
    void run_readWrite_noAnomaly() {
        LockRunResponse response = runWithStrategy("READ_WRITE");
        assertFalse(response.getAnomaly());
        assertEquals(INITIAL, response.getSuccessCount());
        assertEquals(0, response.getFinalStock());
    }

    @Test
    void run_none_likelyAnomaly() {
        boolean sawAnomaly = false;
        boolean sawOversell = false;
        for (int i = 0; i < 5; i++) {
            LockRunResponse response = runWithStrategy("NONE");
            if (Boolean.TRUE.equals(response.getAnomaly())) {
                sawAnomaly = true;
            }
            if (response.getSuccessCount() > INITIAL) {
                sawOversell = true;
            }
        }
        assertTrue(sawAnomaly || sawOversell, "无锁实验应能观察到超卖或 anomaly");
    }

    private LockRunResponse runWithStrategy(String strategy) {
        return lockDemoService.run(baseRequest(strategy));
    }

    private LockRunRequest baseRequest(String strategy) {
        LockRunRequest request = new LockRunRequest();
        request.setLockStrategy(strategy);
        request.setInitialStock(INITIAL);
        request.setThreadCount(THREADS);
        request.setRequestsPerThread(PER_THREAD);
        request.setResetStockBeforeRun(true);
        request.setPoolCoreSize(50);
        request.setPoolMaxSize(50);
        request.setOptimisticMaxRetries(10);
        request.setBatchTag("unit-test-" + strategy);
        return request;
    }
}
