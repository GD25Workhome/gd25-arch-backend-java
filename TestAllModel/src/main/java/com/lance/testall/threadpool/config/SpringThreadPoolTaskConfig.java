package com.lance.testall.threadpool.config;

import com.lance.testall.threadpool.support.DemoRejectedExecutionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Spring {@link ThreadPoolTaskExecutor} 实验池（总纲第 2 步）。
 */
@Configuration
@EnableConfigurationProperties(ThreadPoolSpringDemoProperties.class)
public class SpringThreadPoolTaskConfig {

    private static final Logger log = LoggerFactory.getLogger(SpringThreadPoolTaskConfig.class);

    public static final String SPRING_DEMO_EXECUTOR_BEAN = "springDemoThreadPoolTaskExecutor";

    @Bean(name = SPRING_DEMO_EXECUTOR_BEAN)
    public ThreadPoolTaskExecutor springDemoThreadPoolTaskExecutor(ThreadPoolSpringDemoProperties properties) {
        RejectedExecutionHandler base = ThreadPoolRejectionPolicyResolver.resolve(properties.getRejectionPolicy());
        RejectedExecutionHandler handler = new DemoRejectedExecutionHandler(base);

        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(properties.getCorePoolSize());
        executor.setMaxPoolSize(properties.getMaxPoolSize());
        executor.setQueueCapacity(properties.getQueueCapacity());
        executor.setKeepAliveSeconds(properties.getKeepAliveSeconds());
        executor.setThreadNamePrefix(properties.getThreadNamePrefix());
        executor.setRejectedExecutionHandler(handler);
        executor.setWaitForTasksToCompleteOnShutdown(properties.isWaitForTasksToCompleteOnShutdown());
        executor.setAwaitTerminationSeconds(properties.getAwaitTerminationSeconds());
        executor.setAllowCoreThreadTimeOut(true);
        executor.initialize();

        ThreadPoolExecutor underlying = executor.getThreadPoolExecutor();
        if (underlying != null) {
            log.info("Spring demo 线程池已初始化: core={}, max={}, queueType={}, queueCap={}, policy={}",
                    underlying.getCorePoolSize(),
                    underlying.getMaximumPoolSize(),
                    underlying.getQueue().getClass().getSimpleName(),
                    properties.getQueueCapacity(),
                    properties.getRejectionPolicy());
        }
        return executor;
    }
}
