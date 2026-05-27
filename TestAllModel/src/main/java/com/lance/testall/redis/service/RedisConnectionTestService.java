package com.lance.testall.redis.service;

import com.lance.testall.redis.config.RedisTestProperties;
import com.lance.testall.redis.dto.RedisConnectionTestResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Properties;
import java.util.UUID;

/**
 * Redis 连通性与读写探活。
 */
@Service
public class RedisConnectionTestService {

    private final StringRedisTemplate redisTemplate;
    private final RedisTestProperties redisTestProperties;

    @Value("${spring.data.redis.host:localhost}")
    private String host;

    @Value("${spring.data.redis.port:6379}")
    private int port;

    @Value("${spring.data.redis.ssl.enabled:false}")
    private boolean sslEnabled;

    public RedisConnectionTestService(
            StringRedisTemplate redisTemplate,
            RedisTestProperties redisTestProperties) {
        this.redisTemplate = redisTemplate;
        this.redisTestProperties = redisTestProperties;
    }

    /**
     * 执行 PING + 临时键读写，返回诊断信息（不向外抛出连接异常）。
     */
    public RedisConnectionTestResponse testConnection() {
        long start = System.currentTimeMillis();
        RedisConnectionTestResponse response = new RedisConnectionTestResponse();
        response.setHost(host);
        response.setPort(port);
        response.setSslEnabled(sslEnabled);
        response.setConnected(false);

        String testKey = redisTestProperties.getKeyPrefix() + UUID.randomUUID();
        String testValue = "ok-" + System.currentTimeMillis();
        response.setTestKey(testKey);
        response.setTestValue(testValue);

        try {
            String ping = redisTemplate.execute((RedisConnection connection) -> connection.ping());
            response.setPing(ping);

            Properties serverInfo = redisTemplate.execute((RedisConnection connection) ->
                    connection.serverCommands().info("server"));
            if (serverInfo != null) {
                response.setRedisVersion(serverInfo.getProperty("redis_version"));
            }

            redisTemplate.opsForValue().set(testKey, testValue, Duration.ofSeconds(60));
            String readBack = redisTemplate.opsForValue().get(testKey);
            if (!testValue.equals(readBack)) {
                response.setErrorMessage("读写不一致: expected=" + testValue + ", actual=" + readBack);
                return response;
            }

            response.setConnected(true);
            return response;
        } catch (Exception ex) {
            response.setErrorMessage(ex.getClass().getSimpleName() + ": " + ex.getMessage());
            return response;
        } finally {
            response.setElapsedMs(System.currentTimeMillis() - start);
            if (Boolean.TRUE.equals(response.getConnected())) {
                try {
                    redisTemplate.delete(testKey);
                } catch (Exception ignored) {
                    // 清理失败不影响连通性结论
                }
            }
        }
    }
}
