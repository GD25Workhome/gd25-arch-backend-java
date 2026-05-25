package com.lance.testall.threadpool.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lance.testall.threadpool.entity.ThreadPoolTaskLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * thread_pool_task_log 表 Mapper。
 */
@Mapper
public interface ThreadPoolTaskLogMapper extends BaseMapper<ThreadPoolTaskLog> {
}
