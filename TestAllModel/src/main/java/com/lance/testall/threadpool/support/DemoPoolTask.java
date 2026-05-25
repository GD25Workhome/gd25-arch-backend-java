package com.lance.testall.threadpool.support;

import com.lance.testall.threadpool.service.ThreadPoolTaskRunner;

import java.util.concurrent.CountDownLatch;

/**
 * 提交给线程池的可识别任务，便于拒绝策略与完成等待。
 */
public class DemoPoolTask implements Runnable {

    private final String executorType;
    private final String batchId;
    private final int taskIndex;
    private final String batchTag;
    private final int workDelayMs;
    private final ThreadPoolTaskRunner taskRunner;
    private final CountDownLatch latch;

    public DemoPoolTask(String executorType,
                        String batchId,
                        int taskIndex,
                        String batchTag,
                        int workDelayMs,
                        ThreadPoolTaskRunner taskRunner,
                        CountDownLatch latch) {
        this.executorType = executorType;
        this.batchId = batchId;
        this.taskIndex = taskIndex;
        this.batchTag = batchTag;
        this.workDelayMs = workDelayMs;
        this.taskRunner = taskRunner;
        this.latch = latch;
    }

    public String getBatchId() {
        return batchId;
    }

    public int getTaskIndex() {
        return taskIndex;
    }

    public String getBatchTag() {
        return batchTag;
    }

    @Override
    public void run() {
        try {
            taskRunner.runSuccessTask(executorType, batchId, taskIndex, batchTag, workDelayMs);
        } finally {
            if (latch != null) {
                latch.countDown();
            }
        }
    }

    /**
     * 任务被拒绝时由拒绝策略调用：登记 REJECTED 并释放 latch。
     */
    public void onRejected() {
        try {
            taskRunner.insertRejected(executorType, batchId, taskIndex, batchTag);
        } finally {
            if (latch != null) {
                latch.countDown();
            }
        }
    }
}
