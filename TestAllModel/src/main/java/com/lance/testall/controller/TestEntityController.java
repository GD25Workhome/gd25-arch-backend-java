package com.lance.testall.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lance.common.model.ApiResult;
import com.lance.testall.entity.TestEntity;
import com.lance.testall.mapper.TestEntityMapper;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 示例 CRUD REST 接口，供前端调用。
 */
@RestController
@RequestMapping("/api/test-entities")
public class TestEntityController {

    private final TestEntityMapper testEntityMapper;

    public TestEntityController(TestEntityMapper testEntityMapper) {
        this.testEntityMapper = testEntityMapper;
    }

    @GetMapping
    public ApiResult<List<TestEntity>> list() {
        return ApiResult.success(testEntityMapper.selectList(new LambdaQueryWrapper<>()));
    }

    @GetMapping("/{id}")
    public ApiResult<TestEntity> getById(@PathVariable Long id) {
        TestEntity entity = testEntityMapper.selectById(id);
        if (entity == null) {
            return ApiResult.fail("记录不存在: id=" + id);
        }
        return ApiResult.success(entity);
    }

    @PostMapping
    public ApiResult<TestEntity> create(@RequestBody TestEntity body) {
        body.setId(null);
        testEntityMapper.insert(body);
        return ApiResult.success(body);
    }

    @PutMapping("/{id}")
    public ApiResult<TestEntity> update(@PathVariable Long id, @RequestBody TestEntity body) {
        TestEntity existing = testEntityMapper.selectById(id);
        if (existing == null) {
            return ApiResult.fail("记录不存在: id=" + id);
        }
        body.setId(id);
        testEntityMapper.updateById(body);
        return ApiResult.success(testEntityMapper.selectById(id));
    }

    @DeleteMapping("/{id}")
    public ApiResult<Void> delete(@PathVariable Long id) {
        int rows = testEntityMapper.deleteById(id);
        if (rows == 0) {
            return ApiResult.fail("记录不存在: id=" + id);
        }
        return ApiResult.success();
    }
}
