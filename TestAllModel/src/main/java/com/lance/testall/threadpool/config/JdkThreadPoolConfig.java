package com.lance.testall.threadpool.config;

import com.lance.testall.threadpool.support.DemoRejectedExecutionHandler;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * JDK {@link ThreadPoolExecutor} 实验池配置。
 */
@Configuration
@EnableConfigurationProperties(ThreadPoolDemoProperties.class)
public class JdkThreadPoolConfig {

    private static final Logger log = LoggerFactory.getLogger(JdkThreadPoolConfig.class);

    public static final String DEMO_EXECUTOR_BEAN = "demoThreadPoolExecutor";

    private final ThreadPoolDemoProperties properties;
    private ThreadPoolExecutor executor;

    public JdkThreadPoolConfig(ThreadPoolDemoProperties properties) {
        this.properties = properties;
    }

    @Bean(name = DEMO_EXECUTOR_BEAN)
    public ThreadPoolExecutor demoThreadPoolExecutor() {
        ThreadFactory threadFactory = new NamedThreadFactory(properties.getThreadNamePrefix());
        RejectedExecutionHandler baseHandler = ThreadPoolRejectionPolicyResolver.resolve(properties.getRejectionPolicy());
        RejectedExecutionHandler handler = new DemoRejectedExecutionHandler(baseHandler);

        this.executor = new ThreadPoolExecutor(
                properties.getCorePoolSize(),
                properties.getMaxPoolSize(),
                properties.getKeepAliveSeconds(),
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(properties.getQueueCapacity()),
                threadFactory,
                handler
        );
        // core 超时回收。下次突发任务会重新建线程，有一点冷启动开销
        executor.allowCoreThreadTimeOut(true);
        log.info("demo 线程池已创建: core={}, max={}, queue={}, policy={}",
                properties.getCorePoolSize(),
                properties.getMaxPoolSize(),
                properties.getQueueCapacity(),
                properties.getRejectionPolicy());
        return executor;
    }

    @PreDestroy
    public void shutdownExecutor() {
        if (executor == null) {
            return;
        }
        log.info("正在关闭 demo 线程池...");
        executor.shutdown();
        try {
            if (!executor.awaitTermination(properties.getAwaitTerminationSeconds(), TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            executor.shutdownNow();
        }
    }

    private static final class NamedThreadFactory implements ThreadFactory {

        private final AtomicInteger counter = new AtomicInteger(1);
        private final String prefix;

        private NamedThreadFactory(String prefix) {
            this.prefix = prefix == null || prefix.isBlank() ? "tp-demo-" : prefix;
        }

        @Override
        public Thread newThread(Runnable r) {
            Thread thread = new Thread(r, prefix + counter.getAndIncrement());
            thread.setDaemon(false);
            return thread;
        }
    }
}
