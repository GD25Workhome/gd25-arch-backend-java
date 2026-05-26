package com.lance.testall.lock.entity;

/**
 * 单次扣减结果（由 {@link com.lance.testall.lock.service.StockDeductService} 返回，
 * {@link com.lance.testall.lock.service.LockDemoService} 据此累加批次计数）。
 */
public enum DeductResult {

    /** 扣减成功，stock 已减 1 */
    SUCCESS,

    /** 库存不足（stock &lt; 1），业务失败 */
    INSUFFICIENT,

    /** ReentrantLock.tryLock 超时未获取锁 */
    LOCK_TIMEOUT,

    /** 乐观锁重试耗尽，版本持续冲突 */
    VERSION_CONFLICT,

    /** 中断或其它异常 */
    ERROR
}
