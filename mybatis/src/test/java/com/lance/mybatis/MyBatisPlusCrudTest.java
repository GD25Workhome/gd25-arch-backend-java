package com.lance.mybatis;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lance.mybatis.test.TestApplication;
import com.lance.mybatis.test.TestEntity;
import com.lance.mybatis.test.TestMapper;

/**
 * MyBatis-Plus CRUD 操作测试类
 * 测试增删改查功能
 */
@SpringBootTest(classes = {TestApplication.class, MyBatisPlusAutoConfiguration.class})
@TestPropertySource(properties = {
        "mybatis.boot.check.enable=true",
        "mybatis.boot.check.block=false",
        "spring.datasource.url=jdbc:h2:mem:crud_testdb;MODE=MySQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "mybatis-plus.configuration.map-underscore-to-camel-case=true",
        "mybatis-plus.type-aliases-package=com.lance.mybatis.test"
})
@Sql(statements = {
        "CREATE TABLE IF NOT EXISTS test_entity (" +
                "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                "name VARCHAR(100), " +
                "description VARCHAR(255)" +
                ")"
})
class MyBatisPlusCrudTest {

    @Autowired
    private TestMapper testMapper;

    @BeforeEach
    void setUp() {
        // 每个测试前清空数据
        testMapper.delete(null);
    }

    /**
     * TC-011: 测试插入操作（Create）
     * 验证 MyBatis-Plus 的插入功能是否正常
     */
    @Test
    void testInsert() {
        // 创建测试实体对象
        TestEntity entity = new TestEntity("测试名称", "测试描述");
        
        // 调用 insert() 方法插入数据
        int result = testMapper.insert(entity);
        
        // 验证插入成功，返回影响行数为 1
        assertEquals(1, result, "插入应该返回影响行数 1");
        
        // 验证插入后实体 ID 被自动填充
        assertNotNull(entity.getId(), "插入后 ID 应该被自动填充");
        assertTrue(entity.getId() > 0, "ID 应该大于 0");
        
        // 验证数据确实插入到数据库
        TestEntity savedEntity = testMapper.selectById(entity.getId());
        assertNotNull(savedEntity, "应该能查询到插入的数据");
        assertEquals("测试名称", savedEntity.getName(), "名称应该匹配");
        assertEquals("测试描述", savedEntity.getDescription(), "描述应该匹配");
    }

    /**
     * TC-012: 测试查询操作（Read）
     * 验证 MyBatis-Plus 的查询功能是否正常
     */
    @Test
    void testSelect() {
        // 预先插入测试数据
        TestEntity entity1 = new TestEntity("实体1", "描述1");
        TestEntity entity2 = new TestEntity("实体2", "描述2");
        TestEntity entity3 = new TestEntity("实体3", "描述3");
        
        testMapper.insert(entity1);
        testMapper.insert(entity2);
        testMapper.insert(entity3);
        
        // 使用 selectById() 根据 ID 查询
        TestEntity foundById = testMapper.selectById(entity1.getId());
        assertNotNull(foundById, "应该能根据 ID 查询到数据");
        assertEquals("实体1", foundById.getName(), "名称应该匹配");
        
        // 使用 selectList() 查询列表
        List<TestEntity> allEntities = testMapper.selectList(null);
        assertNotNull(allEntities, "查询列表不应该为 null");
        assertEquals(3, allEntities.size(), "应该查询到 3 条数据");
        
        // 使用 selectOne() 查询单条记录
        LambdaQueryWrapper<TestEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TestEntity::getName, "实体2");
        TestEntity foundOne = testMapper.selectOne(wrapper);
        assertNotNull(foundOne, "应该能查询到单条记录");
        assertEquals("实体2", foundOne.getName(), "名称应该匹配");
        
        // 使用条件构造器 LambdaQueryWrapper 进行条件查询
        LambdaQueryWrapper<TestEntity> conditionWrapper = new LambdaQueryWrapper<>();
        conditionWrapper.like(TestEntity::getName, "实体");
        List<TestEntity> conditionEntities = testMapper.selectList(conditionWrapper);
        assertNotNull(conditionEntities, "条件查询结果不应该为 null");
        assertEquals(3, conditionEntities.size(), "应该查询到 3 条符合条件的数据");
    }

    /**
     * TC-013: 测试更新操作（Update）
     * 验证 MyBatis-Plus 的更新功能是否正常
     */
    @Test
    void testUpdate() {
        // 预先插入测试数据
        TestEntity entity = new TestEntity("原始名称", "原始描述");
        testMapper.insert(entity);
        Long id = entity.getId();
        
        // 修改实体对象的属性
        entity.setName("更新后的名称");
        entity.setDescription("更新后的描述");
        
        // 调用 updateById() 根据 ID 更新
        int updateResult = testMapper.updateById(entity);
        assertEquals(1, updateResult, "更新应该返回影响行数 1");
        
        // 查询验证数据已更新
        TestEntity updatedEntity = testMapper.selectById(id);
        assertNotNull(updatedEntity, "应该能查询到更新后的数据");
        assertEquals("更新后的名称", updatedEntity.getName(), "名称应该已更新");
        assertEquals("更新后的描述", updatedEntity.getDescription(), "描述应该已更新");
        
        // 使用 update() 方法配合条件构造器更新
        TestEntity entity2 = new TestEntity("实体2", "描述2");
        testMapper.insert(entity2);
        
        TestEntity updateEntity = new TestEntity();
        updateEntity.setDescription("通过条件更新的描述");
        
        LambdaQueryWrapper<TestEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TestEntity::getName, "实体2");
        
        int conditionUpdateResult = testMapper.update(updateEntity, wrapper);
        assertEquals(1, conditionUpdateResult, "条件更新应该返回影响行数 1");
        
        // 验证条件更新成功
        TestEntity conditionUpdatedEntity = testMapper.selectById(entity2.getId());
        assertEquals("通过条件更新的描述", conditionUpdatedEntity.getDescription(), "描述应该已通过条件更新");
    }

    /**
     * TC-014: 测试删除操作（Delete）
     * 验证 MyBatis-Plus 的删除功能是否正常
     */
    @Test
    void testDelete() {
        // 预先插入测试数据
        TestEntity entity1 = new TestEntity("实体1", "描述1");
        TestEntity entity2 = new TestEntity("实体2", "描述2");
        TestEntity entity3 = new TestEntity("实体3", "描述3");
        
        testMapper.insert(entity1);
        testMapper.insert(entity2);
        testMapper.insert(entity3);
        
        Long id1 = entity1.getId();
        Long id2 = entity2.getId();
        
        // 调用 deleteById() 根据 ID 删除
        int deleteResult = testMapper.deleteById(id1);
        assertEquals(1, deleteResult, "删除应该返回影响行数 1");
        
        // 查询验证数据已删除
        TestEntity deletedEntity = testMapper.selectById(id1);
        assertNull(deletedEntity, "删除的数据不应该再能查询到");
        
        // 使用 delete() 方法配合条件构造器删除
        LambdaQueryWrapper<TestEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TestEntity::getName, "实体2");
        
        int conditionDeleteResult = testMapper.delete(wrapper);
        assertEquals(1, conditionDeleteResult, "条件删除应该返回影响行数 1");
        
        // 验证条件删除成功
        TestEntity conditionDeletedEntity = testMapper.selectById(id2);
        assertNull(conditionDeletedEntity, "条件删除的数据不应该再能查询到");
        
        // 验证剩余数据
        List<TestEntity> remainingEntities = testMapper.selectList(null);
        assertEquals(1, remainingEntities.size(), "应该只剩下 1 条数据");
        assertEquals("实体3", remainingEntities.get(0).getName(), "剩余数据应该是实体3");
    }
}

