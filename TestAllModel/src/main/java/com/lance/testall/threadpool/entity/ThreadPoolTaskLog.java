package com.lance.testall.threadpool.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 线程池实验任务日志，对应表 thread_pool_task_log。
 */
@Data
@TableName("thread_pool_task_log")
public class ThreadPoolTaskLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String batchId;

    private Integer taskIndex;

    /** JDK 或 SPRING，用于对照实验 */
    private String executorType;

    private String threadName;

    private String status;

    private String errorMessage;

    private String batchTag;

    private LocalDateTime createdAt;

    private LocalDateTime finishedAt;
}
