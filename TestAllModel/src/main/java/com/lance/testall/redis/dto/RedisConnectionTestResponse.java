package com.lance.testall.redis.dto;

import lombok.Data;

/**
 * Redis 连接测试结果。
 */
@Data
public class RedisConnectionTestResponse {

    /** 是否连通（PING 成功且读写测试通过） */
    private Boolean connected;

    /** PING 返回值，通常为 PONG */
    private String ping;

    /** INFO server 中的 redis_version */
    private String redisVersion;

    /** 当前配置的主机（不含密码） */
    private String host;

    /** 当前配置的端口 */
    private Integer port;

    /** 是否启用 SSL */
    private Boolean sslEnabled;

    /** 读写测试使用的 key */
    private String testKey;

    /** 读写测试写入并读回的值 */
    private String testValue;

    /** 端到端耗时（毫秒） */
    private Long elapsedMs;

    /** 失败时的异常摘要 */
    private String errorMessage;
}
