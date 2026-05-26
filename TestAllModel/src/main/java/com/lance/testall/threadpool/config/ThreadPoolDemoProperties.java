package com.lance.testall.threadpool.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * JDK 线程池实验配置，前缀 thread-pool.demo。
 */
@Data
@ConfigurationProperties(prefix = "thread-pool.demo")
public class ThreadPoolDemoProperties {

    private int corePoolSize = 2;
    private int maxPoolSize = 4;
    private int queueCapacity = 10;
    private int keepAliveSeconds = 60;
    private String threadNamePrefix = "tp-demo-";
    private String rejectionPolicy = "abort";
    private int awaitTerminationSeconds = 30;
}
