package com.lance.testall.lock.service;

import com.lance.testall.lock.entity.DeductResult;
import com.lance.testall.lock.entity.LockDemoStock;
import com.lance.testall.lock.mapper.LockDemoStockMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 数据库悲观锁扣减（实验 5）。
 * <p>
 * 独立 Bean 以保证 {@link Transactional} 通过 Spring 代理生效（线程池内调用仍走代理）。
 */
@Service
public class DbStockDeductService {

    private final LockDemoStockMapper stockMapper;

    public DbStockDeductService(LockDemoStockMapper stockMapper) {
        this.stockMapper = stockMapper;
    }

    /**
     * 事务内 SELECT … FOR UPDATE 后扣减，依赖数据库行锁。
     *
     * @param simulateDelayMs 大于 0 时持锁休眠，用于观察其它线程阻塞（实验 5b，慎用）
     */
    @Transactional(rollbackFor = Exception.class)
    public DeductResult deductPessimistic(String skuId, int simulateDelayMs) {
        LockDemoStock row = stockMapper.selectForUpdate(skuId);
        if (row == null || row.getStock() == null || row.getStock() < 1) {
            return DeductResult.INSUFFICIENT;
        }
        if (simulateDelayMs > 0) {
            sleepQuietly(simulateDelayMs);
        }
        row.setStock(row.getStock() - 1);
        row.setVersion(row.getVersion() + 1);
        row.setUpdatedAt(LocalDateTime.now());
        stockMapper.updateById(row);
        return DeductResult.SUCCESS;
    }

    private static void sleepQuietly(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("悲观锁模拟延迟被中断", ex);
        }
    }
}
