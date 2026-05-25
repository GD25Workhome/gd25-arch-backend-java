package com.lance.testall.threadpool.support;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 拒绝时写入 REJECTED 日志（通过 {@link DemoPoolTask#onRejected()}）。
 */
public class DemoRejectedExecutionHandler implements RejectedExecutionHandler {

    private static final Logger log = LoggerFactory.getLogger(DemoRejectedExecutionHandler.class);

    private final RejectedExecutionHandler delegate;

    public DemoRejectedExecutionHandler(RejectedExecutionHandler delegate) {
        this.delegate = delegate;
    }

    @Override
    public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
        // CallerRuns 会在调用方线程执行任务，不应登记 REJECTED
        if (delegate instanceof ThreadPoolExecutor.CallerRunsPolicy) {
            delegate.rejectedExecution(r, executor);
            return;
        }
        if (r instanceof DemoPoolTask demoTask) {
            log.warn("任务被拒绝: batchId={}, taskIndex={}", demoTask.getBatchId(), demoTask.getTaskIndex());
            demoTask.onRejected();
        }
        if (delegate != null) {
            delegate.rejectedExecution(r, executor);
        }
    }
}
