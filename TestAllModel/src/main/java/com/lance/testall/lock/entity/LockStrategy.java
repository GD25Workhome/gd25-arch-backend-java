package com.lance.testall.lock.entity;

/**
 * 锁实验策略枚举，与 API 请求体 lockStrategy 字段一一对应。
 * <p>
 * 解析：{@link #fromApiValue(String)}，由 {@link com.lance.testall.lock.service.LockDemoService#run} 调用。
 * 实现：{@link com.lance.testall.lock.service.StockDeductService#deductOnce} 内 switch 分发。
 */
public enum LockStrategy {

    /** 实验 0：无锁，读-改-写分离 */
    NONE,
    /** 实验 1a：synchronized 实例方法 */
    SYNC_INSTANCE,
    /** 实验 1b：synchronized 静态方法 */
    SYNC_STATIC,
    /** 实验 1c：按 sku 分段 synchronized 代码块 */
    SYNC_BLOCK_SKU,
    /** 实验 1d：故意使用 Integer 缓存池对象作锁（反例） */
    SYNC_WRONG_INTEGER,
    /** 实验 2a：ReentrantLock 非公平 */
    REENTRANT,
    /** 实验 2c：ReentrantLock 公平锁 */
    REENTRANT_FAIR,
    /** 实验 2b：tryLock 超时 */
    REENTRANT_TRY,
    /** 实验 3a：Semaphore 限制同时进入临界区的线程数（内层仍 synchronized 保证正确） */
    SEMAPHORE,
    /** 实验 3b：ReentrantReadWriteLock 写锁扣减 */
    READ_WRITE,
    /** 实验 4a：version 乐观锁，冲突重试 */
    DB_OPTIMISTIC,
    /** 实验 4b：单条 UPDATE 原子扣减 */
    DB_ATOMIC_UPDATE,
    /** 实验 5：SELECT FOR UPDATE 悲观锁 */
    DB_PESSIMISTIC,
    /** 实验 6a/6c：Redis 分布式锁 + DB 扣减 */
    REDIS,
    /** 实验 6b：仅 JVM 本地锁（对照，不用 Redis） */
    REDIS_LOCAL_ONLY;

    public static LockStrategy fromApiValue(String value) {
        if (value == null || value.isBlank()) {
            return NONE;
        }
        try {
            return LockStrategy.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("不支持的 lockStrategy: " + value);
        }
    }
}
