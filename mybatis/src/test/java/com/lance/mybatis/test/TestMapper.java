package com.lance.mybatis.test;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 测试 Mapper 接口
 * 用于测试 MyBatis Mapper 功能
 */
@Mapper
public interface TestMapper extends BaseMapper<TestEntity> {
}

