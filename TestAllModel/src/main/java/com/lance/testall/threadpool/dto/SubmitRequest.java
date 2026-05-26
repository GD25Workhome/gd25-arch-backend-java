package com.lance.testall.threadpool.dto;

import lombok.Data;

/**
 * 批量提交线程池任务请求体。
 */
@Data
public class SubmitRequest {

    /** 本批子任务数量，默认 10 */
    private Integer taskCount = 10;

    /** 写库前模拟 IO 延迟（毫秒），默认 100 */
    private Integer workDelayMs = 100;

    /** 是否阻塞等待本批任务全部结束（含被拒绝登记） */
    private Boolean waitForComplete = true;

    /** 可选批次备注 */
    private String batchTag;
}
