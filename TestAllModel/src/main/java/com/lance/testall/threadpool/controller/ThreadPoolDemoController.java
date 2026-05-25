package com.lance.testall.threadpool.controller;

import com.lance.common.model.ApiResult;
import com.lance.testall.threadpool.dto.BatchSummaryResponse;
import com.lance.testall.threadpool.dto.PoolStatsResponse;
import com.lance.testall.threadpool.dto.SubmitRequest;
import com.lance.testall.threadpool.dto.SubmitResponse;
import com.lance.testall.threadpool.service.ThreadPoolDemoService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * JDK 线程池批量写库实验 API。
 */
@RestController
@RequestMapping("/api/thread-pool/demo")
public class ThreadPoolDemoController {

    private final ThreadPoolDemoService demoService;

    public ThreadPoolDemoController(ThreadPoolDemoService demoService) {
        this.demoService = demoService;
    }

    /**
     * 提交一批写库任务到 JDK 线程池。
     */
    @PostMapping("/submit")
    public ApiResult<SubmitResponse> submit(@RequestBody(required = false) SubmitRequest request) {
        if (request == null) {
            request = new SubmitRequest();
        }
        return ApiResult.success(demoService.submit(request));
    }

    /**
     * 按批次查询任务汇总（成功/失败/拒绝、按线程名分布）。
     */
    @GetMapping("/batch/{batchId}")
    public ApiResult<BatchSummaryResponse> batchSummary(@PathVariable String batchId) {
        return ApiResult.success(demoService.getBatchSummary(batchId));
    }

    /**
     * 当前线程池运行时指标。
     */
    @GetMapping("/pool/stats")
    public ApiResult<PoolStatsResponse> poolStats() {
        return ApiResult.success(demoService.getPoolStats());
    }
}
