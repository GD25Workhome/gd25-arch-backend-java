package com.lance.testall.lock.dto;

import lombok.Data;

import java.util.Map;

/**
 * 锁实验运行结果。
 */
@Data
public class LockRunResponse {

    private String batchId;
    private String lockStrategy;
    private Integer initialStock;
    private Integer finalStock;
    private Integer successCount;
    private Integer failCount;
    private Integer errorCount;
    /** 扣减结果明细，键为 DeductResult 枚举名或 UNCAUGHT_EXCEPTION */
    private Map<String, Integer> resultBreakdown;
    private Integer totalRequests;
    private Boolean anomaly;
    private String anomalyReason;
    private Long elapsedMs;
    private String instanceId;
}
