package com.lance.testall.threadpool.config;

import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 拒绝策略名称解析为 JDK {@link RejectedExecutionHandler}。
 */
public final class ThreadPoolRejectionPolicyResolver {

    private ThreadPoolRejectionPolicyResolver() {
    }

    public static RejectedExecutionHandler resolve(String policy) {
        if (policy == null) {
            return new ThreadPoolExecutor.AbortPolicy();
        }
        return switch (policy.trim().toLowerCase()) {
            case "caller-runs" -> new ThreadPoolExecutor.CallerRunsPolicy();
            case "discard" -> new ThreadPoolExecutor.DiscardPolicy();
            case "discard-oldest" -> new ThreadPoolExecutor.DiscardOldestPolicy();
            default -> new ThreadPoolExecutor.AbortPolicy();
        };
    }
}
