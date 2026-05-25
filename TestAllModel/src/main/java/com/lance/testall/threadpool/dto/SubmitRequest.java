package com.lance.testall.threadpool.dto;

/**
 * 批量提交线程池任务请求体。
 */
public class SubmitRequest {

    /** 本批子任务数量，默认 10 */
    private Integer taskCount = 10;

    /** 写库前模拟 IO 延迟（毫秒），默认 100 */
    private Integer workDelayMs = 100;

    /** 是否阻塞等待本批任务全部结束（含被拒绝登记） */
    private Boolean waitForComplete = true;

    /** 可选批次备注 */
    private String batchTag;

    public Integer getTaskCount() {
        return taskCount;
    }

    public void setTaskCount(Integer taskCount) {
        this.taskCount = taskCount;
    }

    public Integer getWorkDelayMs() {
        return workDelayMs;
    }

    public void setWorkDelayMs(Integer workDelayMs) {
        this.workDelayMs = workDelayMs;
    }

    public Boolean getWaitForComplete() {
        return waitForComplete;
    }

    public void setWaitForComplete(Boolean waitForComplete) {
        this.waitForComplete = waitForComplete;
    }

    public String getBatchTag() {
        return batchTag;
    }

    public void setBatchTag(String batchTag) {
        this.batchTag = batchTag;
    }
}
