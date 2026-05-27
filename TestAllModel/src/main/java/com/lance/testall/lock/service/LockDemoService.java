package com.lance.testall.lock.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lance.testall.lock.config.LockDemoProperties;
import com.lance.testall.lock.dto.DeductOptions;
import com.lance.testall.lock.dto.LockRunRequest;
import com.lance.testall.lock.dto.LockRunResponse;
import com.lance.testall.lock.dto.StockResetRequest;
import com.lance.testall.lock.dto.StockViewResponse;
import com.lance.testall.lock.entity.DeductResult;
import com.lance.testall.lock.entity.LockDemoRunLog;
import com.lance.testall.lock.entity.LockDemoStock;
import com.lance.testall.lock.entity.LockStrategy;
import com.lance.testall.lock.mapper.LockDemoRunLogMapper;
import com.lance.testall.lock.mapper.LockDemoStockMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 锁实验编排层：负责「并发怎么发起、结果怎么汇总」，不负责具体加锁实现。
 * <p>
 * 与 {@link StockDeductService} 的分工：
 * <ul>
 *   <li>本类：线程池、CountDownLatch、计数器、anomaly 判定、lock_demo_run_log 落库</li>
 *   <li>StockDeductService：单次扣减 + synchronized / ReentrantLock 临界区</li>
 * </ul>
 */
@Service
public class LockDemoService {
    private static final Logger log = LoggerFactory.getLogger(LockDemoService.class);
    private final LockDemoStockMapper stockMapper;
    private final LockDemoRunLogMapper runLogMapper;
    private final StockDeductService stockDeductService;
    private final LockDemoProperties properties;

    /** 多实例实验时写入 run_log.instance_id，格式：应用名:端口 */
    @Value("${spring.application.name:test-all-model}")
    private String applicationName;

    @Value("${server.port:8080}")
    private int serverPort;

    public LockDemoService(
            LockDemoStockMapper stockMapper,
            LockDemoRunLogMapper runLogMapper,
            StockDeductService stockDeductService,
            LockDemoProperties properties) {
        this.stockMapper = stockMapper;
        this.runLogMapper = runLogMapper;
        this.stockDeductService = stockDeductService;
        this.properties = properties;
    }

    /**
     * 执行一整批并发扣减实验（由 {@link com.lance.testall.lock.controller.LockDemoController#run} 调用）。
     */
    public LockRunResponse run(LockRunRequest request) {
        validateRunRequest(request);

        LockStrategy strategy = LockStrategy.fromApiValue(request.getLockStrategy());
        String skuId = resolveSkuId(request.getSkuId());
        int initialStock = request.getInitialStock();
        int threadCount = request.getThreadCount();
        int requestsPerThread = request.getRequestsPerThread();
        int totalTasks = threadCount * requestsPerThread;
        DeductOptions deductOptions = buildDeductOptions(request, strategy);

        String batchId = UUID.randomUUID().toString().replace("-", "");
        String instanceId = applicationName + ":" + serverPort;

        // ---------- 1. 准备库存 ----------
        if (Boolean.TRUE.equals(request.getResetStockBeforeRun())) {
            resetStock(skuId, initialStock);
        }

        LockDemoStock before = stockMapper.selectById(skuId);
        if (before == null) {
            throw new IllegalStateException("库存记录不存在，请先重置: skuId=" + skuId);
        }
        // 以 DB 中实际库存为准，作为 anomaly 判定基准
        int recordedInitial = before.getStock();

        // ---------- 2. 并发计数器（线程安全，供池内任务累加） ----------
        RunResultCounter resultCounter = new RunResultCounter();
        CountDownLatch latch = new CountDownLatch(totalTasks);

        // ---------- 3. 为本批实验创建临时线程池（与 threadpool 实验的常驻池隔离） ----------
        int core = request.getPoolCoreSize() != null
                ? request.getPoolCoreSize()
                : properties.getThreadPool().getCorePoolSize();
        int max = request.getPoolMaxSize() != null
                ? request.getPoolMaxSize()
                : properties.getThreadPool().getMaxPoolSize();
        int queueCap = properties.getThreadPool().getQueueCapacity();
        String prefix = properties.getThreadPool().getThreadNamePrefix();

        ThreadPoolExecutor executor = createRunExecutor(core, max, queueCap, prefix);
        long startMs = System.currentTimeMillis();

        try {
            // ---------- 4. 提交 totalTasks 个扣减任务 ----------
            for (int t = 0; t < threadCount; t++) {
                for (int r = 0; r < requestsPerThread; r++) {
                    executor.execute(() -> {
                        try {
                            // 底层：按 strategy 加锁（或不加锁）后读-改-写 stock
                            DeductResult result = stockDeductService.deductOnce(skuId, strategy, deductOptions);
                            resultCounter.record(result);
                        } catch (Exception ex) {
                            resultCounter.recordUncaughtException();
                        } finally {
                            latch.countDown();
                        }
                    });
                }
            }
            awaitLatch(latch);
        } finally {
            shutdownExecutor(executor);
        }

        long elapsedMs = System.currentTimeMillis() - startMs;

        // ---------- 5. 读取实验后库存，判定是否超卖 ----------
        LockDemoStock after = stockMapper.selectById(skuId);
        int finalStock = after != null && after.getStock() != null ? after.getStock() : -1;

        // 超卖：成功次数超过初始库存，或最终库存为负（无锁时常见）
        boolean anomaly = resultCounter.getSuccessCount() > recordedInitial || finalStock < 0;
        String anomalyReason = resolveAnomalyReason(
                resultCounter.getSuccessCount(), recordedInitial, finalStock, anomaly);

        // ---------- 6. 批次结果落库 lock_demo_run_log ----------
        LockDemoRunLog logRow = new LockDemoRunLog();
        logRow.setBatchId(batchId);
        logRow.setLockStrategy(strategy.name());
        logRow.setThreadCount(threadCount);
        logRow.setRequestsPerThread(requestsPerThread);
        logRow.setSuccessCount(resultCounter.getSuccessCount());
        logRow.setFailCount(resultCounter.getFailCount());
        logRow.setErrorCount(resultCounter.getErrorCount());
        logRow.setResultBreakdown(resultCounter.toBreakdownJson());
        logRow.setInitialStock(recordedInitial);
        logRow.setFinalStock(finalStock);
        logRow.setAnomaly(anomaly);
        logRow.setAnomalyReason(anomalyReason);
        logRow.setElapsedMs(elapsedMs);
        logRow.setInstanceId(instanceId);
        logRow.setBatchTag(request.getBatchTag());
        logRow.setCreatedAt(LocalDateTime.now());
        log.info("logRow={}", logRow);
        runLogMapper.insert(logRow);

        // ---------- 7. 组装 API 响应 ----------
        LockRunResponse response = new LockRunResponse();
        response.setBatchId(batchId);
        response.setLockStrategy(strategy.name());
        response.setInitialStock(recordedInitial);
        response.setFinalStock(finalStock);
        response.setSuccessCount(resultCounter.getSuccessCount());
        response.setFailCount(resultCounter.getFailCount());
        response.setErrorCount(resultCounter.getErrorCount());
        response.setResultBreakdown(resultCounter.toBreakdownMap());
        response.setTotalRequests(totalTasks);
        response.setAnomaly(anomaly);
        response.setAnomalyReason(anomalyReason);
        response.setElapsedMs(elapsedMs);
        response.setInstanceId(instanceId);
        return response;
    }

    /**
     * 从 lock_demo_run_log 还原批次结果（供 GET /run/{batchId}）。
     */
    public LockRunResponse getRun(String batchId) {
        LockDemoRunLog logRow = runLogMapper.selectOne(
                new LambdaQueryWrapper<LockDemoRunLog>().eq(LockDemoRunLog::getBatchId, batchId).last("LIMIT 1"));
        if (logRow == null) {
            return null;
        }
        LockRunResponse response = new LockRunResponse();
        response.setBatchId(logRow.getBatchId());
        response.setLockStrategy(logRow.getLockStrategy());
        response.setInitialStock(logRow.getInitialStock());
        response.setFinalStock(logRow.getFinalStock());
        response.setSuccessCount(logRow.getSuccessCount());
        response.setFailCount(logRow.getFailCount());
        response.setErrorCount(logRow.getErrorCount());
        response.setResultBreakdown(RunResultCounter.parseBreakdownJson(logRow.getResultBreakdown()));
        response.setTotalRequests(logRow.getThreadCount() * logRow.getRequestsPerThread());
        response.setAnomaly(logRow.getAnomaly());
        response.setAnomalyReason(logRow.getAnomalyReason());
        response.setElapsedMs(logRow.getElapsedMs());
        response.setInstanceId(logRow.getInstanceId());
        return response;
    }

    /**
     * 查询 lock_demo_stock 当前行（无锁读）。
     */
    public StockViewResponse getStock(String skuId) {
        String resolved = resolveSkuId(skuId);
        // 实验 3b：读路径走读锁，可与 READ_WRITE 写扣减并发
        LockDemoStock row = stockDeductService.loadStockUnderReadLock(resolved);
        if (row == null) {
            return null;
        }
        StockViewResponse view = new StockViewResponse();
        view.setSkuId(row.getSkuId());
        view.setStock(row.getStock());
        view.setVersion(row.getVersion());
        return view;
    }

    /**
     * 重置库存并返回最新快照（供 POST /stock/reset）。
     */
    public StockViewResponse resetStock(StockResetRequest request) {
        String skuId = resolveSkuId(request.getSkuId());
        int stock = request.getStock() != null ? request.getStock() : 100;
        resetStock(skuId, stock);
        return getStock(skuId);
    }

    /**
     * 插入或更新 lock_demo_stock；实验 4 之前 version 仅重置为 0。
     */
    private void resetStock(String skuId, int stock) {
        LockDemoStock row = stockMapper.selectById(skuId);
        LocalDateTime now = LocalDateTime.now();
        if (row == null) {
            row = new LockDemoStock();
            row.setSkuId(skuId);
            row.setStock(stock);
            row.setVersion(0);
            row.setUpdatedAt(now);
            stockMapper.insert(row);
        } else {
            row.setStock(stock);
            row.setVersion(0);
            row.setUpdatedAt(now);
            stockMapper.updateById(row);
        }
    }

    private void validateRunRequest(LockRunRequest request) {
        if (request.getThreadCount() == null || request.getThreadCount() < 1) {
            throw new IllegalArgumentException("threadCount 必须 >= 1");
        }
        if (request.getRequestsPerThread() == null || request.getRequestsPerThread() < 1) {
            throw new IllegalArgumentException("requestsPerThread 必须 >= 1");
        }
        if (request.getInitialStock() == null || request.getInitialStock() < 0) {
            throw new IllegalArgumentException("initialStock 必须 >= 0");
        }
        long total = (long) request.getThreadCount() * request.getRequestsPerThread();
        if (total > 10_000) {
            throw new IllegalArgumentException("threadCount * requestsPerThread 不得超过 10000");
        }
    }

    private String resolveSkuId(String skuId) {
        if (skuId != null && !skuId.isBlank()) {
            return skuId.trim();
        }
        return properties.getDefaultSkuId();
    }

    /**
     * 为本批次构造扣减参数（Semaphore 每批独立实例，避免多请求共享许可）。
     */
    private DeductOptions buildDeductOptions(LockRunRequest request, LockStrategy strategy) {
        int tryLockTimeoutMs = request.getTryLockTimeoutMs() != null && request.getTryLockTimeoutMs() > 0
                ? request.getTryLockTimeoutMs()
                : properties.getTryLockTimeoutMs();
        int optimisticMaxRetries = request.getOptimisticMaxRetries() != null && request.getOptimisticMaxRetries() > 0
                ? request.getOptimisticMaxRetries()
                : properties.getOptimisticMaxRetries();
        int simulateDelayMs = request.getSimulateDelayMs() != null ? Math.max(0, request.getSimulateDelayMs()) : 0;

        Semaphore semaphore = null;
        if (strategy == LockStrategy.SEMAPHORE) {
            int permits = request.getSemaphorePermits() != null && request.getSemaphorePermits() > 0
                    ? request.getSemaphorePermits()
                    : properties.getSemaphorePermits();
            semaphore = new Semaphore(permits);
        }

        return new DeductOptions(tryLockTimeoutMs, optimisticMaxRetries, semaphore, simulateDelayMs);
    }

    /**
     * 生成 anomaly 原因码，便于 API 与 SQL 筛选。
     */
    private static String resolveAnomalyReason(int success, int initial, int finalStock, boolean anomaly) {
        if (!anomaly) {
            return null;
        }
        if (success > initial) {
            return "SUCCESS_COUNT_EXCEEDS_INITIAL_STOCK";
        }
        if (finalStock < 0) {
            return "FINAL_STOCK_NEGATIVE";
        }
        return "UNKNOWN";
    }

    /**
     * 每批 /run 单独建池，避免与线程池实验、其它批次争用同一线程池。
     * CallerRunsPolicy：队列满时由调用线程执行，形成背压。
     */
    private static ThreadPoolExecutor createRunExecutor(int core, int max, int queueCap, String prefix) {
        AtomicLong threadSeq = new AtomicLong(1);
        ThreadFactory factory = runnable -> {
            Thread thread = new Thread(runnable, prefix + threadSeq.getAndIncrement());
            thread.setDaemon(false);
            return thread;
        };
        return new ThreadPoolExecutor(
                core,
                max,
                60L,
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(queueCap),
                factory,
                new ThreadPoolExecutor.CallerRunsPolicy()
        );
    }

    private static void awaitLatch(CountDownLatch latch) {
        try {
            if (!latch.await(5, TimeUnit.MINUTES)) {
                throw new IllegalStateException("并发扣减等待超时（5 分钟）");
            }
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("并发扣减被中断", ex);
        }
    }

    private static void shutdownExecutor(ThreadPoolExecutor executor) {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            executor.shutdownNow();
        }
    }
}
