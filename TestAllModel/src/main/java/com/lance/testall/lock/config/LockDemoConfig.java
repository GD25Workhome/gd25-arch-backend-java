package com.lance.testall.lock.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * 锁实验配置入口：绑定 application.yml 中 {@code lock.demo.*} 到 {@link LockDemoProperties}。
 */
@Configuration
@EnableConfigurationProperties(LockDemoProperties.class)
@EnableTransactionManagement
public class LockDemoConfig {
}
