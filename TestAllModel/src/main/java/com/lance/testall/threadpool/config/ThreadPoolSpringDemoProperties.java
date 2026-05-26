package com.lance.testall.threadpool.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Spring {@link org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor} 实验配置。
 */
@Data
@ConfigurationProperties(prefix = "thread-pool.spring-demo")
public class ThreadPoolSpringDemoProperties {

    private int corePoolSize = 2;
    private int maxPoolSize = 4;
    private int queueCapacity = 10;
    private int keepAliveSeconds = 60;
    private String threadNamePrefix = "tp-spring-";
    private String rejectionPolicy = "abort";
    private int awaitTerminationSeconds = 30;
    private boolean waitForTasksToCompleteOnShutdown = true;
}
