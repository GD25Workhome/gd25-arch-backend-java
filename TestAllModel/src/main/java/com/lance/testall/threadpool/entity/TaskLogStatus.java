package com.lance.testall.threadpool.entity;

/**
 * 线程池任务日志状态。
 */
public final class TaskLogStatus {

    public static final String PENDING = "PENDING";
    public static final String SUCCESS = "SUCCESS";
    public static final String FAIL = "FAIL";
    public static final String REJECTED = "REJECTED";

    private TaskLogStatus() {
    }
}
