package com.lance.testall.lock.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.lance.testall.lock.mybatis.JsonbStringTypeHandler;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 锁实验批次汇总日志，对应表 lock_demo_run_log。
 */
@Data
@TableName(value = "lock_demo_run_log", autoResultMap = true)
public class LockDemoRunLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String batchId;

    private String lockStrategy;

    private Integer threadCount;

    private Integer requestsPerThread;

    private Integer successCount;

    private Integer failCount;

    private Integer errorCount;

    /** 各 {@link DeductResult} 及 UNCAUGHT_EXCEPTION 的 JSON 明细（PG 为 JSONB） */
    @TableField(typeHandler = JsonbStringTypeHandler.class)
    private String resultBreakdown;

    private Integer initialStock;

    private Integer finalStock;

    private Boolean anomaly;

    private String anomalyReason;

    private Long elapsedMs;

    private String instanceId;

    private String batchTag;

    private LocalDateTime createdAt;
}
