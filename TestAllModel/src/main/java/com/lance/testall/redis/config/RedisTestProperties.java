package com.lance.testall.redis.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Redis 连接测试相关配置。
 */
@Data
@Component
@ConfigurationProperties(prefix = "redis.test")
public class RedisTestProperties {

    /** 读写测试键前缀，避免污染业务 key */
    private String keyPrefix = "testall:redis-test:";
}
