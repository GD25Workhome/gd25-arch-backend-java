package com.lance.testall.threadpool.service;

import com.lance.testall.threadpool.entity.TaskLogStatus;
import com.lance.testall.threadpool.entity.ThreadPoolTaskLog;
import com.lance.testall.threadpool.mapper.ThreadPoolTaskLogMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 单条任务的写库逻辑，供池线程与拒绝策略共用。
 */
@Component
public class ThreadPoolTaskRunner {

    private static final Logger log = LoggerFactory.getLogger(ThreadPoolTaskRunner.class);

    private final ThreadPoolTaskLogMapper taskLogMapper;

    public ThreadPoolTaskRunner(ThreadPoolTaskLogMapper taskLogMapper) {
        this.taskLogMapper = taskLogMapper;
    }

    /**
     * 模拟慢 IO 后写入 SUCCESS；异常时写入 FAIL。
     */
    public void runSuccessTask(String executorType, String batchId, int taskIndex, String batchTag, int workDelayMs) {
        LocalDateTime now = LocalDateTime.now();
        try {
            if (workDelayMs > 0) {
                Thread.sleep(workDelayMs);
            }
            ThreadPoolTaskLog row = newRow(executorType, batchId, taskIndex, batchTag, now);
            row.setThreadName(Thread.currentThread().getName());
            row.setStatus(TaskLogStatus.SUCCESS);
            row.setFinishedAt(LocalDateTime.now());
            taskLogMapper.insert(row);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            insertFail(executorType, batchId, taskIndex, batchTag, now, "线程被中断: " + e.getMessage());
        } catch (Exception e) {
            log.error("任务执行失败 batchId={}, taskIndex={}", batchId, taskIndex, e);
            insertFail(executorType, batchId, taskIndex, batchTag, now, e.getMessage());
        }
    }

    /**
     * 任务未进入执行队列时被拒绝，写入 REJECTED。
     */
    public void insertRejected(String executorType, String batchId, int taskIndex, String batchTag) {
        LocalDateTime now = LocalDateTime.now();
        ThreadPoolTaskLog row = newRow(executorType, batchId, taskIndex, batchTag, now);
        row.setThreadName(Thread.currentThread().getName());
        row.setStatus(TaskLogStatus.REJECTED);
        row.setErrorMessage("RejectedExecutionException: 线程池队列已满且已达最大线程数");
        row.setFinishedAt(now);
        taskLogMapper.insert(row);
    }

    private void insertFail(String executorType, String batchId, int taskIndex, String batchTag,
                            LocalDateTime createdAt, String message) {
        ThreadPoolTaskLog row = newRow(executorType, batchId, taskIndex, batchTag, createdAt);
        row.setThreadName(Thread.currentThread().getName());
        row.setStatus(TaskLogStatus.FAIL);
        row.setErrorMessage(truncate(message, 500));
        row.setFinishedAt(LocalDateTime.now());
        taskLogMapper.insert(row);
    }

    private static ThreadPoolTaskLog newRow(String executorType, String batchId, int taskIndex, String batchTag,
                                            LocalDateTime createdAt) {
        ThreadPoolTaskLog row = new ThreadPoolTaskLog();
        row.setExecutorType(executorType);
        row.setBatchId(batchId);
        row.setTaskIndex(taskIndex);
        row.setBatchTag(batchTag);
        row.setCreatedAt(createdAt);
        return row;
    }

    private static String truncate(String message, int maxLen) {
        if (message == null) {
            return null;
        }
        return message.length() <= maxLen ? message : message.substring(0, maxLen);
    }
}
