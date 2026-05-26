package com.lance.testall.lock.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lance.testall.lock.entity.LockDemoRunLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 表 lock_demo_run_log 的持久化接口。
 * <p>
 * 每批 {@link com.lance.testall.lock.service.LockDemoService#run} 结束时 insert 一行汇总；
 * {@link com.lance.testall.lock.service.LockDemoService#getRun} 按 batch_id 查询。
 */
@Mapper
public interface LockDemoRunLogMapper extends BaseMapper<LockDemoRunLog> {
}
