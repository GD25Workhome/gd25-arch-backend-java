package com.lance.testall.lock.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 锁实验批次汇总日志，对应表 lock_demo_run_log。
 */
@Data
@TableName("lock_demo_run_log")
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

    private Integer initialStock;

    private Integer finalStock;

    private Boolean anomaly;

    private String anomalyReason;

    private Long elapsedMs;

    private String instanceId;

    private String batchTag;

    private LocalDateTime createdAt;
}
