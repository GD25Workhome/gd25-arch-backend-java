package com.lance.testall.threadpool.dto;

import lombok.Data;

/**
 * 批量提交响应。
 */
@Data
public class SubmitResponse {

    private String batchId;
    /** JDK 或 SPRING */
    private String executorType;
    private int submitted;
    private int success;
    private int failed;
    private int rejected;
    private long elapsedMs;
}
