package com.lance.testall.threadpool.service;

import com.lance.testall.threadpool.config.SpringThreadPoolTaskConfig;
import com.lance.testall.threadpool.config.ThreadPoolSpringDemoProperties;
import com.lance.testall.threadpool.dto.BatchSummaryResponse;
import com.lance.testall.threadpool.dto.PoolStatsResponse;
import com.lance.testall.threadpool.dto.SubmitRequest;
import com.lance.testall.threadpool.dto.SubmitResponse;
import com.lance.testall.threadpool.entity.ExecutorType;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * Spring {@link ThreadPoolTaskExecutor} 批量写库实验（总纲第 2 步）。
 */
@Service
public class SpringThreadPoolDemoService {

    private final ThreadPoolTaskExecutor springExecutor;
    private final BatchSubmitOrchestrator orchestrator;
    private final ThreadPoolSpringDemoProperties properties;

    public SpringThreadPoolDemoService(
            @Qualifier(SpringThreadPoolTaskConfig.SPRING_DEMO_EXECUTOR_BEAN) ThreadPoolTaskExecutor springExecutor,
            BatchSubmitOrchestrator orchestrator,
            ThreadPoolSpringDemoProperties properties) {
        this.springExecutor = springExecutor;
        this.orchestrator = orchestrator;
        this.properties = properties;
    }

    public SubmitResponse submit(SubmitRequest request) {
        return orchestrator.submit(springExecutor, ExecutorType.SPRING, request);
    }

    public BatchSummaryResponse getBatchSummary(String batchId) {
        return orchestrator.getBatchSummary(batchId);
    }

    public PoolStatsResponse getPoolStats() {
        ThreadPoolExecutor underlying = springExecutor.getThreadPoolExecutor();
        String policyHint = "ThreadPoolTaskExecutor; thread-pool.spring-demo.rejection-policy="
                + properties.getRejectionPolicy();
        return orchestrator.buildPoolStats(underlying, ExecutorType.SPRING, policyHint);
    }
}
