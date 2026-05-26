package com.lance.testall.threadpool.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 批次汇总查询响应。
 */
@Data
public class BatchSummaryResponse {

    private String batchId;
    private String executorType;
    private int total;
    private int success;
    private int failed;
    private int rejected;
    private int pending;
    private List<StatusCount> byStatus;
    private List<ThreadNameCount> byThreadName;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StatusCount {
        private String status;
        private long count;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ThreadNameCount {
        private String threadName;
        private long count;
    }
}
