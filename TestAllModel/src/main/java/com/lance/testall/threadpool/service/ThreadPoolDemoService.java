package com.lance.testall.threadpool.service;

import com.lance.testall.threadpool.config.JdkThreadPoolConfig;
import com.lance.testall.threadpool.config.ThreadPoolDemoProperties;
import com.lance.testall.threadpool.dto.BatchSummaryResponse;
import com.lance.testall.threadpool.dto.PoolStatsResponse;
import com.lance.testall.threadpool.dto.SubmitRequest;
import com.lance.testall.threadpool.dto.SubmitResponse;
import com.lance.testall.threadpool.entity.ExecutorType;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * JDK {@link ThreadPoolExecutor} 批量写库实验。
 */
@Service
public class ThreadPoolDemoService {

    private final ThreadPoolExecutor demoExecutor;
    private final BatchSubmitOrchestrator orchestrator;
    private final ThreadPoolDemoProperties properties;

    public ThreadPoolDemoService(
            @Qualifier(JdkThreadPoolConfig.DEMO_EXECUTOR_BEAN) ThreadPoolExecutor demoExecutor,
            BatchSubmitOrchestrator orchestrator,
            ThreadPoolDemoProperties properties) {
        this.demoExecutor = demoExecutor;
        this.orchestrator = orchestrator;
        this.properties = properties;
    }

    public SubmitResponse submit(SubmitRequest request) {
        return orchestrator.submit(demoExecutor, ExecutorType.JDK, request);
    }

    public BatchSummaryResponse getBatchSummary(String batchId) {
        return orchestrator.getBatchSummary(batchId);
    }

    public PoolStatsResponse getPoolStats() {
        String policyHint = "(wrapped) see thread-pool.demo.rejection-policy=" + properties.getRejectionPolicy();
        return orchestrator.buildPoolStats(demoExecutor, ExecutorType.JDK, policyHint);
    }
}
