package com.lance.testall.lock.service;

import com.lance.testall.lock.config.LockDemoProperties;
import com.lance.testall.lock.dto.DeductOptions;
import com.lance.testall.lock.entity.DeductResult;
import com.lance.testall.lock.entity.LockDemoStock;
import com.lance.testall.lock.entity.LockStrategy;
import com.lance.testall.lock.mapper.LockDemoStockMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * 库存扣减与各锁策略实现（实验 0～6）。
 */
@Service
public class StockDeductService {

    private static final Logger log = LoggerFactory.getLogger(StockDeductService.class);

    private static final Integer WRONG_LOCK = Integer.valueOf(1);

    private static final ConcurrentHashMap<String, Object> SKU_LOCKS = new ConcurrentHashMap<>();

    private final LockDemoStockMapper stockMapper;
    private final LockDemoProperties properties;
    private final DbStockDeductService dbStockDeductService;
    private final RedisStockLockService redisStockLockService;

    private final ReentrantLock reentrantLock = new ReentrantLock();
    private final ReentrantLock fairReentrantLock = new ReentrantLock(true);

    /** 实验 3b：读写锁，写扣减 / 读库存 */
    private final ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();

    /** 实验 3a：未传入 Semaphore 时使用的共享实例（许可数见 lock.demo.semaphore-permits） */
    private final Semaphore defaultSemaphore;

    public StockDeductService(
            LockDemoStockMapper stockMapper,
            LockDemoProperties properties,
            DbStockDeductService dbStockDeductService,
            RedisStockLockService redisStockLockService) {
        this.stockMapper = stockMapper;
        this.properties = properties;
        this.dbStockDeductService = dbStockDeductService;
        this.redisStockLockService = redisStockLockService;
        this.defaultSemaphore = new Semaphore(properties.getSemaphorePermits());
    }

    /**
     * 单次扣减入口：根据策略分发。
     */
    public DeductResult deductOnce(String skuId, LockStrategy strategy, DeductOptions options) {
        if (options == null) {
            options = defaultOptions();
        }
        return switch (strategy) {
            case NONE -> deductUnsafe(skuId);
            case SYNC_INSTANCE -> deductSyncInstance(skuId);
            case SYNC_STATIC -> deductSyncStatic(skuId);
            case SYNC_BLOCK_SKU -> deductSyncBlockSku(skuId);
            case SYNC_WRONG_INTEGER -> deductSyncWrongInteger(skuId);

            case REENTRANT -> deductReentrant(skuId, reentrantLock);
            case REENTRANT_FAIR -> deductReentrant(skuId, fairReentrantLock);
            case REENTRANT_TRY -> deductReentrantTry(skuId, options.getTryLockTimeoutMs());

            case SEMAPHORE -> deductWithSemaphore(skuId, options.getSemaphore());
            case READ_WRITE -> deductReadWrite(skuId);

            case DB_OPTIMISTIC -> deductOptimistic(skuId, options.getOptimisticMaxRetries(), options.getSimulateDelayMs());
            case DB_ATOMIC_UPDATE -> deductAtomicUpdate(skuId);
            case DB_PESSIMISTIC -> dbStockDeductService.deductPessimistic(skuId, options.getSimulateDelayMs());

            case REDIS -> deductWithRedis(skuId);
            case REDIS_LOCAL_ONLY -> deductRedisLocalOnly(skuId);
        };
    }

    /**
     * 带读锁查询库存（实验 3b：GET /stock 与写扣减可并发读）。
     */
    public LockDemoStock loadStockUnderReadLock(String skuId) {
        readWriteLock.readLock().lock();
        try {
            return stockMapper.selectById(skuId);
        } finally {
            readWriteLock.readLock().unlock();
        }
    }

    // ==================== 实验 0 ====================

    public DeductResult deductUnsafe(String skuId) {
        LockDemoStock row = stockMapper.selectById(skuId);
        if (row == null || row.getStock() == null || row.getStock() < 1) {
            return DeductResult.INSUFFICIENT;
        }
        row.setStock(row.getStock() - 1);
        row.setUpdatedAt(LocalDateTime.now());
        stockMapper.updateById(row);
        return DeductResult.SUCCESS;
    }

    // ==================== 实验 1 ====================

    public synchronized DeductResult deductSyncInstance(String skuId) {
        return deductWithReentrantTrace(skuId);
    }

    public DeductResult deductSyncStatic(String skuId) {
        synchronized (StockDeductService.class) {
            return deductWithReentrantTrace(skuId);
        }
    }

    public DeductResult deductSyncBlockSku(String skuId) {
        Object lock = SKU_LOCKS.computeIfAbsent(skuId, key -> new Object());
        synchronized (lock) {
            return deductWithReentrantTrace(skuId);
        }
    }

    public DeductResult deductSyncWrongInteger(String skuId) {
        synchronized (WRONG_LOCK) {
            return deductUnsafe(skuId);
        }
    }

    // ==================== 实验 2 ====================

    public DeductResult deductReentrant(String skuId, ReentrantLock lock) {
        lock.lock();
        try {
            return deductWithReentrantTrace(skuId);
        } finally {
            lock.unlock();
        }
    }

    public DeductResult deductReentrantTry(String skuId, int tryLockTimeoutMs) {
        boolean acquired;
        try {
            acquired = reentrantLock.tryLock(tryLockTimeoutMs, TimeUnit.MILLISECONDS);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            return DeductResult.ERROR;
        }
        if (!acquired) {
            return DeductResult.LOCK_TIMEOUT;
        }
        try {
            return deductWithReentrantTrace(skuId);
        } finally {
            reentrantLock.unlock();
        }
    }

    // ==================== 实验 3 ====================

    /**
     * Semaphore 做「准入控制」：限制同时进入的线程数；内层 synchronized 保证读-改-写正确。
     * <p>
     * {@code semaphore} 为 null 时使用本 Bean 内共享的 {@link #defaultSemaphore}，避免每次调用新建实例导致限流失效。
     * 批量实验仍应通过 {@link DeductOptions} 传入每批独立的 Semaphore（见 {@code LockDemoService#buildDeductOptions}）。
     */
    public DeductResult deductWithSemaphore(String skuId, Semaphore semaphore) {
        Semaphore effective = semaphore != null ? semaphore : defaultSemaphore;
        try {
            effective.acquire();
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            return DeductResult.ERROR;
        }
        try {
            Object lock = SKU_LOCKS.computeIfAbsent(skuId, key -> new Object());
            synchronized (lock) {
                return deductCore(skuId);
            }
        } finally {
            effective.release();
        }
    }

    /** 写锁保护下的扣减 */
    public DeductResult deductReadWrite(String skuId) {
        readWriteLock.writeLock().lock();
        try {
            return deductCore(skuId);
        } finally {
            readWriteLock.writeLock().unlock();
        }
    }

    // ==================== 实验 4 ====================

    /**
     * 乐观锁：读 version → UPDATE … WHERE version=? → 失败则重试。
     */
    public DeductResult deductOptimistic(String skuId, int maxRetries, int simulateDelayMs) {
        int attempts = Math.max(1, maxRetries);
        for (int i = 0; i < attempts; i++) {
            LockDemoStock row = stockMapper.selectById(skuId);
            if (row == null || row.getStock() == null || row.getStock() < 1) {
                return DeductResult.INSUFFICIENT;
            }
            if (simulateDelayMs > 0) {
                sleepQuietly(simulateDelayMs);
            }
            int updated = stockMapper.optimisticDecrementStock(skuId, row.getVersion());
            if (updated == 1) {
                return DeductResult.SUCCESS;
            }
        }
        return DeductResult.VERSION_CONFLICT;
    }

    /** 单条 SQL 原子扣减，无需应用层锁 */
    public DeductResult deductAtomicUpdate(String skuId) {
        int updated = stockMapper.atomicDecrementStock(skuId);
        return updated == 1 ? DeductResult.SUCCESS : DeductResult.INSUFFICIENT;
    }

    // ==================== 实验 6：Redis 分布式锁 ====================

    /**
     * 实验 6a/6c：先抢 Redis 锁，再在锁内执行 DB 读-改-写。
     */
    public DeductResult deductWithRedis(String skuId) {
        String token = redisStockLockService.tryLock(skuId);
        if (token == null) {
            return DeductResult.LOCK_TIMEOUT;
        }
        try {
            return deductCore(skuId);
        } finally {
            redisStockLockService.unlock(skuId, token);
        }
    }

    /**
     * 实验 6b：故意不使用 Redis，仅 {@link #deductSyncStatic} 本地互斥（双实例时会超卖）。
     */
    public DeductResult deductRedisLocalOnly(String skuId) {
        return deductSyncStatic(skuId);
    }

    // ==================== 公共 ====================

    private DeductResult deductWithReentrantTrace(String skuId) {
        traceReentrantEntry();
        return deductCore(skuId);
    }

    private synchronized void traceReentrantEntry() {
        if (log.isTraceEnabled()) {
            log.trace("可重入 trace, thread={}", Thread.currentThread().getName());
        }
    }

    private DeductResult deductCore(String skuId) {
        LockDemoStock row = stockMapper.selectById(skuId);
        if (row == null || row.getStock() == null || row.getStock() < 1) {
            return DeductResult.INSUFFICIENT;
        }
        row.setStock(row.getStock() - 1);
        row.setUpdatedAt(LocalDateTime.now());
        stockMapper.updateById(row);
        return DeductResult.SUCCESS;
    }

    private DeductOptions defaultOptions() {
        return new DeductOptions(
                properties.getTryLockTimeoutMs(),
                properties.getOptimisticMaxRetries(),
                null,
                0
        );
    }

    private static void sleepQuietly(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("模拟延迟被中断", ex);
        }
    }
}
