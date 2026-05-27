package com.lance.testall.redis.controller;

import com.lance.common.enums.CodeEnum;
import com.lance.common.model.ApiResult;
import com.lance.testall.redis.dto.RedisConnectionTestResponse;
import com.lance.testall.redis.service.RedisConnectionTestService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Redis 连接测试 HTTP 入口（云实例 / 本地均可）。
 * <p>
 * 启动云 Redis 示例：
 * <pre>
 *   export REDIS_PASSWORD='你的密码'
 *   mvn -pl TestAllModel spring-boot:run -Dspring-boot.run.profiles=redis-cloud
 *   curl http://localhost:8080/api/redis-test/ping
 * </pre>
 */
@RestController
@RequestMapping("/api/redis-test")
public class RedisConnectionTestController {

    private final RedisConnectionTestService redisConnectionTestService;

    public RedisConnectionTestController(RedisConnectionTestService redisConnectionTestService) {
        this.redisConnectionTestService = redisConnectionTestService;
    }

    /**
     * 探活：PING + 临时键 SET/GET，返回 host/port/version 等诊断字段。
     */
    @GetMapping("/ping")
    public ApiResult<RedisConnectionTestResponse> ping() {
        RedisConnectionTestResponse result = redisConnectionTestService.testConnection();
        if (Boolean.TRUE.equals(result.getConnected())) {
            return ApiResult.success(result);
        }
        String msg = result.getErrorMessage() != null ? result.getErrorMessage() : "Redis 连接失败";
        return ApiResult.fail(CodeEnum.ERROR.getCode(), msg, result);
    }
}
