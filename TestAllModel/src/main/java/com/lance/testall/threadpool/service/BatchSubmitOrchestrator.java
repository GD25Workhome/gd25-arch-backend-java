package com.lance.testall.threadpool.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lance.testall.threadpool.dto.BatchSummaryResponse;
import com.lance.testall.threadpool.dto.PoolStatsResponse;
import com.lance.testall.threadpool.dto.SubmitRequest;
import com.lance.testall.threadpool.dto.SubmitResponse;
import com.lance.testall.threadpool.entity.TaskLogStatus;
import com.lance.testall.threadpool.entity.ThreadPoolTaskLog;
import com.lance.testall.threadpool.mapper.ThreadPoolTaskLogMapper;
import com.lance.testall.threadpool.support.DemoPoolTask;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * JDK / Spring 线程池共用的批量提交与查询编排。
 */
@Component
public class BatchSubmitOrchestrator {

    private static final int MAX_TASK_COUNT = 500;
    private static final int MAX_WORK_DELAY_MS = 60_000;
    private static final long LATCH_TIMEOUT_MINUTES = 30;

    private final ThreadPoolTaskRunner taskRunner;
    private final ThreadPoolTaskLogMapper taskLogMapper;

    public BatchSubmitOrchestrator(ThreadPoolTaskRunner taskRunner, ThreadPoolTaskLogMapper taskLogMapper) {
        this.taskRunner = taskRunner;
        this.taskLogMapper = taskLogMapper;
    }

    public SubmitResponse submit(Executor executor, String executorType, SubmitRequest request) {
        int taskCount = normalizeTaskCount(request.getTaskCount());
        int workDelayMs = normalizeWorkDelayMs(request.getWorkDelayMs());
        boolean waitForComplete = request.getWaitForComplete() == null || request.getWaitForComplete();
        String batchTag = request.getBatchTag();
        String batchId = UUID.randomUUID().toString();

        CountDownLatch latch = waitForComplete ? new CountDownLatch(taskCount) : null;
        long start = System.currentTimeMillis();

        int submitAttempts = 0;
        int submitExceptions = 0;
        for (int i = 0; i < taskCount; i++) {
            DemoPoolTask task = new DemoPoolTask(
                    executorType, batchId, i, batchTag, workDelayMs, taskRunner, latch);
            submitAttempts++;
            try {
                executor.execute(task);
            } catch (RejectedExecutionException e) {
                submitExceptions++;
            }
        }

        if (waitForComplete && latch != null) {
            awaitLatch(latch);
        }

        SubmitResponse response = new SubmitResponse();
        response.setBatchId(batchId);
        response.setExecutorType(executorType);
        response.setSubmitted(submitAttempts);
        response.setElapsedMs(System.currentTimeMillis() - start);

        if (waitForComplete) {
            fillCountsFromDb(batchId, response);
        } else {
            response.setSuccess(0);
            response.setFailed(0);
            response.setRejected(submitExceptions);
        }
        return response;
    }

    public BatchSummaryResponse getBatchSummary(String batchId) {
        List<ThreadPoolTaskLog> logs = taskLogMapper.selectList(
                new LambdaQueryWrapper<ThreadPoolTaskLog>()
                        .eq(ThreadPoolTaskLog::getBatchId, batchId)
        );
        BatchSummaryResponse summary = new BatchSummaryResponse();
        summary.setBatchId(batchId);
        summary.setTotal(logs.size());
        if (!logs.isEmpty()) {
            summary.setExecutorType(logs.get(0).getExecutorType());
        }

        Map<String, Long> byStatus = logs.stream()
                .collect(Collectors.groupingBy(ThreadPoolTaskLog::getStatus, Collectors.counting()));
        summary.setSuccess(byStatus.getOrDefault(TaskLogStatus.SUCCESS, 0L).intValue());
        summary.setFailed(byStatus.getOrDefault(TaskLogStatus.FAIL, 0L).intValue());
        summary.setRejected(byStatus.getOrDefault(TaskLogStatus.REJECTED, 0L).intValue());
        summary.setPending(byStatus.getOrDefault(TaskLogStatus.PENDING, 0L).intValue());

        summary.setByStatus(byStatus.entrySet().stream()
                .map(e -> new BatchSummaryResponse.StatusCount(e.getKey(), e.getValue()))
                .toList());

        Map<String, Long> byThread = logs.stream()
                .filter(l -> l.getThreadName() != null)
                .collect(Collectors.groupingBy(ThreadPoolTaskLog::getThreadName, Collectors.counting()));
        summary.setByThreadName(byThread.entrySet().stream()
                .map(e -> new BatchSummaryResponse.ThreadNameCount(e.getKey(), e.getValue()))
                .toList());

        return summary;
    }

    public PoolStatsResponse buildPoolStats(ThreadPoolExecutor executor, String executorType, String policyHint) {
        PoolStatsResponse stats = new PoolStatsResponse();
        stats.setExecutorType(executorType);
        stats.setCorePoolSize(executor.getCorePoolSize());
        stats.setMaximumPoolSize(executor.getMaximumPoolSize());
        stats.setPoolSize(executor.getPoolSize());
        stats.setActiveCount(executor.getActiveCount());
        if (executor.getQueue() != null) {
            stats.setQueueType(executor.getQueue().getClass().getSimpleName());
            stats.setQueueSize(executor.getQueue().size());
            stats.setQueueRemainingCapacity(executor.getQueue().remainingCapacity());
        }
        stats.setCompletedTaskCount(executor.getCompletedTaskCount());
        stats.setTaskCount(executor.getTaskCount());
        stats.setRejectionPolicy(policyHint);
        return stats;
    }

    private void fillCountsFromDb(String batchId, SubmitResponse response) {
        BatchSummaryResponse summary = getBatchSummary(batchId);
        response.setSuccess(summary.getSuccess());
        response.setFailed(summary.getFailed());
        response.setRejected(summary.getRejected());
    }

    private void awaitLatch(CountDownLatch latch) {
        try {
            boolean done = latch.await(LATCH_TIMEOUT_MINUTES, TimeUnit.MINUTES);
            if (!done) {
                throw new IllegalStateException("等待批次任务超时（" + LATCH_TIMEOUT_MINUTES + " 分钟）");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("等待批次任务被中断", e);
        }
    }

    private static int normalizeTaskCount(Integer taskCount) {
        if (taskCount == null || taskCount < 1) {
            return 10;
        }
        return Math.min(taskCount, MAX_TASK_COUNT);
    }

    private static int normalizeWorkDelayMs(Integer workDelayMs) {
        if (workDelayMs == null || workDelayMs < 0) {
            return 100;
        }
        return Math.min(workDelayMs, MAX_WORK_DELAY_MS);
    }
}
