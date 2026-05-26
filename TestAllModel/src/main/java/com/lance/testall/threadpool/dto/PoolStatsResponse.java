package com.lance.testall.threadpool.dto;

import lombok.Data;

/**
 * 线程池运行时指标。
 */
@Data
public class PoolStatsResponse {

    private String executorType;
    private int corePoolSize;
    private int maximumPoolSize;
    /** 底层队列实现类简单名，如 ArrayBlockingQueue、LinkedBlockingQueue */
    private String queueType;
    private int poolSize;
    private int activeCount;
    private int queueSize;
    private int queueRemainingCapacity;
    private long completedTaskCount;
    private long taskCount;
    private String rejectionPolicy;
}
