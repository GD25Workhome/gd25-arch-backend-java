package com.lance.testall.threadpool.controller;

import com.lance.common.model.ApiResult;
import com.lance.testall.threadpool.dto.BatchSummaryResponse;
import com.lance.testall.threadpool.dto.PoolStatsResponse;
import com.lance.testall.threadpool.dto.SubmitRequest;
import com.lance.testall.threadpool.dto.SubmitResponse;
import com.lance.testall.threadpool.service.SpringThreadPoolDemoService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Spring ThreadPoolTaskExecutor 实验 API（与 /demo 路径对称，便于 A/B 对照）。
 */
@RestController
@RequestMapping("/api/thread-pool/spring")
public class SpringThreadPoolDemoController {

    private final SpringThreadPoolDemoService demoService;

    public SpringThreadPoolDemoController(SpringThreadPoolDemoService demoService) {
        this.demoService = demoService;
    }

    @PostMapping("/submit")
    public ApiResult<SubmitResponse> submit(@RequestBody(required = false) SubmitRequest request) {
        if (request == null) {
            request = new SubmitRequest();
        }
        return ApiResult.success(demoService.submit(request));
    }

    @GetMapping("/batch/{batchId}")
    public ApiResult<BatchSummaryResponse> batchSummary(@PathVariable String batchId) {
        return ApiResult.success(demoService.getBatchSummary(batchId));
    }

    @GetMapping("/pool/stats")
    public ApiResult<PoolStatsResponse> poolStats() {
        return ApiResult.success(demoService.getPoolStats());
    }
}
