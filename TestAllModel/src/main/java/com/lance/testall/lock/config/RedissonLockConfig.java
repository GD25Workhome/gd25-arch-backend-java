package com.lance.testall.lock.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

/**
 * 实验 6d：按 spring.data.redis 连接信息创建 {@link RedissonClient}，并设置看门狗超时。
 */
@Configuration
@ConditionalOnExpression("${lock.redis.enabled:true} && ${lock.redisson.enabled:true}")
public class RedissonLockConfig {

    @Bean(destroyMethod = "shutdown")
    public RedissonClient redissonClient(
            RedisProperties redisProperties,
            LockDemoRedissonProperties redissonProperties) {
        Config config = new Config();
        boolean ssl = redisProperties.getSsl() != null && redisProperties.getSsl().isEnabled();
        String scheme = ssl ? "rediss" : "redis";
        String address = scheme + "://" + redisProperties.getHost() + ":" + redisProperties.getPort();

        var server = config.useSingleServer().setAddress(address);
        if (StringUtils.hasText(redisProperties.getUsername())) {
            server.setUsername(redisProperties.getUsername());
        }
        if (redisProperties.getPassword() != null) {
            server.setPassword(String.valueOf(redisProperties.getPassword()));
        }

        long watchdogMs = redissonProperties.getWatchdogTimeoutSeconds() * 1000L;
        config.setLockWatchdogTimeout(watchdogMs);
        return Redisson.create(config);
    }
}
