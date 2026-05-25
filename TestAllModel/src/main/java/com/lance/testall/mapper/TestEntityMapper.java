package com.lance.testall.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lance.testall.entity.TestEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * test_entity 表 Mapper。
 */
@Mapper
public interface TestEntityMapper extends BaseMapper<TestEntity> {
}
