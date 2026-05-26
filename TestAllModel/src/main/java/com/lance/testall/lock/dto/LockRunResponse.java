package com.lance.testall.lock.dto;

import lombok.Data;

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
    private Integer totalRequests;
    private Boolean anomaly;
    private String anomalyReason;
    private Long elapsedMs;
    private String instanceId;
}
