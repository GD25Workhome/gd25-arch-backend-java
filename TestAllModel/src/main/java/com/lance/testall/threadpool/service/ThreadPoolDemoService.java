package com.lance.testall.threadpool.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lance.testall.threadpool.config.JdkThreadPoolConfig;
import com.lance.testall.threadpool.dto.BatchSummaryResponse;
import com.lance.testall.threadpool.dto.PoolStatsResponse;
import com.lance.testall.threadpool.dto.SubmitRequest;
import com.lance.testall.threadpool.dto.SubmitResponse;
import com.lance.testall.threadpool.entity.TaskLogStatus;
import com.lance.testall.threadpool.entity.ThreadPoolTaskLog;
import com.lance.testall.threadpool.mapper.ThreadPoolTaskLogMapper;
import com.lance.testall.threadpool.support.DemoPoolTask;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 线程池批量写库实验业务逻辑。
 */
@Service
public class ThreadPoolDemoService {

    private static final int MAX_TASK_COUNT = 500;
    private static final int MAX_WORK_DELAY_MS = 60_000;
    private static final long LATCH_TIMEOUT_MINUTES = 30;

    private final ThreadPoolExecutor demoExecutor;
    private final ThreadPoolTaskRunner taskRunner;
    private final ThreadPoolTaskLogMapper taskLogMapper;

    public ThreadPoolDemoService(
            @Qualifier(JdkThreadPoolConfig.DEMO_EXECUTOR_BEAN) ThreadPoolExecutor demoExecutor,
            ThreadPoolTaskRunner taskRunner,
            ThreadPoolTaskLogMapper taskLogMapper) {
        this.demoExecutor = demoExecutor;
        this.taskRunner = taskRunner;
        this.taskLogMapper = taskLogMapper;
    }

    public SubmitResponse submit(SubmitRequest request) {
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
            DemoPoolTask task = new DemoPoolTask(batchId, i, batchTag, workDelayMs, taskRunner, latch);
            submitAttempts++;
            try {
                demoExecutor.execute(task);
            } catch (RejectedExecutionException e) {
                // AbortPolicy：拒绝策略已登记 REJECTED 并 countDown，此处仅统计提交异常次数
                submitExceptions++;
            }
        }

        if (waitForComplete && latch != null) {
            awaitLatch(latch);
        }

        SubmitResponse response = new SubmitResponse();
        response.setBatchId(batchId);
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

    public PoolStatsResponse getPoolStats() {
        PoolStatsResponse stats = new PoolStatsResponse();
        stats.setCorePoolSize(demoExecutor.getCorePoolSize());
        stats.setMaximumPoolSize(demoExecutor.getMaximumPoolSize());
        stats.setPoolSize(demoExecutor.getPoolSize());
        stats.setActiveCount(demoExecutor.getActiveCount());
        if (demoExecutor.getQueue() != null) {
            stats.setQueueSize(demoExecutor.getQueue().size());
            stats.setQueueRemainingCapacity(demoExecutor.getQueue().remainingCapacity());
        }
        stats.setCompletedTaskCount(demoExecutor.getCompletedTaskCount());
        stats.setTaskCount(demoExecutor.getTaskCount());
        if (demoExecutor.getRejectedExecutionHandler() instanceof com.lance.testall.threadpool.support.DemoRejectedExecutionHandler) {
            stats.setRejectionPolicy("(wrapped) see application.yml thread-pool.demo.rejection-policy");
        } else {
            stats.setRejectionPolicy(demoExecutor.getRejectedExecutionHandler().getClass().getSimpleName());
        }
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
