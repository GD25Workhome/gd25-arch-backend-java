package com.lance.mybatis;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lance.mybatis.test.TestApplication;
import com.lance.mybatis.test.TestEntity;
import com.lance.mybatis.test.TestMapper;

/**
 * MyBatis-Plus 分页功能测试类
 * 测试分页和翻页功能
 */
@SpringBootTest(classes = {TestApplication.class, MyBatisPlusAutoConfiguration.class})
@TestPropertySource(properties = {
        "mybatis.boot.check.enable=true",
        "mybatis.boot.check.block=false",
        "spring.datasource.url=jdbc:h2:mem:pagination_testdb;MODE=MySQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
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
class MyBatisPlusPaginationTest {

    @Autowired
    private TestMapper testMapper;
    
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        // 每个测试前清空数据
        testMapper.delete(null);
        // 重置 AUTO_INCREMENT，确保 ID 从 1 开始
        // H2 数据库语法：ALTER TABLE table_name ALTER COLUMN column_name RESTART WITH value
        jdbcTemplate.execute("ALTER TABLE test_entity ALTER COLUMN id RESTART WITH 1");
    }

    /**
     * 准备测试数据
     * @param count 数据条数
     */
    private void prepareTestData(int count) {
        for (int i = 1; i <= count; i++) {
            TestEntity entity = new TestEntity("实体" + i, "描述" + i);
            testMapper.insert(entity);
        }
    }

    /**
     * TC-015: 测试分页查询（第一页）
     * 验证分页插件的第一页查询功能
     */
    @Test
    void testPaginationFirstPage() {
        // 预先插入多条测试数据（至少 10 条）
        prepareTestData(10);
        
        // 创建 Page 对象，设置当前页为 1，每页大小为 5
        Page<TestEntity> page = new Page<>(1, 5);
        
        // 调用 selectPage() 方法进行分页查询
        IPage<TestEntity> result = testMapper.selectPage(page, null);
        
        // 验证返回的 Page 对象包含正确的数据
        assertNotNull(result, "分页结果不应该为 null");
        assertNotNull(result.getRecords(), "分页记录不应该为 null");
        assertEquals(5, result.getRecords().size(), "第一页应该返回 5 条数据");
        
        // 验证分页信息：总记录数、总页数、当前页、每页大小
        assertEquals(10, result.getTotal(), "总记录数应该是 10");
        assertEquals(2, result.getPages(), "总页数应该是 2");
        assertEquals(1, result.getCurrent(), "当前页应该是 1");
        assertEquals(5, result.getSize(), "每页大小应该是 5");
    }

    /**
     * TC-016: 测试分页查询（中间页）
     * 验证分页插件的中间页查询功能
     */
    @Test
    void testPaginationMiddlePage() {
        // 预先插入多条测试数据（至少 15 条）
        prepareTestData(15);
        
        // 创建 Page 对象，设置当前页为 2，每页大小为 5
        Page<TestEntity> page = new Page<>(2, 5);
        
        // 创建查询条件，按 ID 升序排序，确保数据顺序可预测
        LambdaQueryWrapper<TestEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(TestEntity::getId);
        
        // 调用 selectPage() 方法进行分页查询
        IPage<TestEntity> result = testMapper.selectPage(page, wrapper);
        
        // 验证返回的数据是第 6-10 条记录
        assertNotNull(result, "分页结果不应该为 null");
        assertEquals(5, result.getRecords().size(), "第二页应该返回 5 条数据");
        
        // 验证分页信息正确
        assertEquals(15, result.getTotal(), "总记录数应该是 15");
        assertEquals(3, result.getPages(), "总页数应该是 3");
        assertEquals(2, result.getCurrent(), "当前页应该是 2");
        
        // 验证返回的数据确实是第二页的数据（ID 应该是 6-10，因为已重置 AUTO_INCREMENT 并从 1 开始）
        List<TestEntity> records = result.getRecords();
        assertEquals(6L, records.get(0).getId(), "第一条记录的 ID 应该是 6");
        assertEquals(10L, records.get(4).getId(), "最后一条记录的 ID 应该是 10");
    }

    /**
     * TC-017: 测试分页查询（最后一页）
     * 验证分页插件的最后一页查询功能
     */
    @Test
    void testPaginationLastPage() {
        // 预先插入多条测试数据（例如 13 条）
        prepareTestData(13);
        
        // 创建 Page 对象，设置当前页为最后一页，每页大小为 5
        Page<TestEntity> page = new Page<>(3, 5); // 13 条数据，每页 5 条，共 3 页
        
        // 调用 selectPage() 方法进行分页查询
        IPage<TestEntity> result = testMapper.selectPage(page, null);
        
        // 验证返回的数据是最后 3 条记录（13 % 5 = 3）
        assertNotNull(result, "分页结果不应该为 null");
        assertEquals(3, result.getRecords().size(), "最后一页应该返回 3 条数据");
        
        // 验证分页信息：总页数、当前页等
        assertEquals(13, result.getTotal(), "总记录数应该是 13");
        assertEquals(3, result.getPages(), "总页数应该是 3");
        assertEquals(3, result.getCurrent(), "当前页应该是 3");
    }

    /**
     * TC-018: 测试分页查询（空结果）
     * 验证分页查询在无数据时的处理
     */
    @Test
    void testPaginationEmptyResult() {
        // 确保数据库中没有测试数据（setUp 已经清空）
        
        // 创建 Page 对象，设置当前页为 1，每页大小为 5
        Page<TestEntity> page = new Page<>(1, 5);
        
        // 调用 selectPage() 方法进行分页查询
        IPage<TestEntity> result = testMapper.selectPage(page, null);
        
        // 验证返回的 Page 对象数据列表为空
        assertNotNull(result, "分页结果不应该为 null");
        assertNotNull(result.getRecords(), "分页记录不应该为 null");
        assertTrue(result.getRecords().isEmpty(), "数据列表应该为空");
        
        // 验证分页信息：总记录数为 0，总页数为 0
        assertEquals(0, result.getTotal(), "总记录数应该是 0");
        assertEquals(0, result.getPages(), "总页数应该是 0");
        assertEquals(1, result.getCurrent(), "当前页应该是 1");
    }

    /**
     * TC-019: 测试分页查询（带条件查询）
     * 验证分页查询配合条件构造器的功能
     */
    @Test
    void testPaginationWithCondition() {
        // 预先插入多条测试数据，包含不同的 name 值
        prepareTestData(10);
        // 插入一些包含 "test" 的数据
        for (int i = 1; i <= 5; i++) {
            TestEntity entity = new TestEntity("test" + i, "测试描述" + i);
            testMapper.insert(entity);
        }
        
        // 创建 Page 对象和 LambdaQueryWrapper 条件构造器
        Page<TestEntity> page = new Page<>(1, 5);
        LambdaQueryWrapper<TestEntity> wrapper = new LambdaQueryWrapper<>();
        
        // 设置查询条件（如 name like '%test%'）
        wrapper.like(TestEntity::getName, "test");
        
        // 调用 selectPage() 方法进行条件分页查询
        IPage<TestEntity> result = testMapper.selectPage(page, wrapper);
        
        // 验证返回的数据符合查询条件
        assertNotNull(result, "分页结果不应该为 null");
        assertTrue(result.getRecords().size() > 0, "应该返回符合条件的数据");
        
        // 验证所有返回的数据都符合条件
        result.getRecords().forEach(entity -> {
            assertTrue(entity.getName().contains("test"), "所有数据都应该包含 'test'");
        });
        
        // 验证分页信息正确
        assertEquals(5, result.getTotal(), "符合条件的总记录数应该是 5");
        assertEquals(1, result.getPages(), "总页数应该是 1");
    }

    /**
     * TC-020: 测试分页查询（排序功能）
     * 验证分页查询配合排序功能
     */
    @Test
    void testPaginationWithOrder() {
        // 预先插入多条测试数据
        prepareTestData(10);
        
        // 创建 Page 对象和 LambdaQueryWrapper 条件构造器
        Page<TestEntity> page = new Page<>(1, 5);
        LambdaQueryWrapper<TestEntity> wrapper = new LambdaQueryWrapper<>();
        
        // 设置排序条件（如按 id 降序、按 name 升序）
        wrapper.orderByDesc(TestEntity::getId);
        
        // 调用 selectPage() 方法进行排序分页查询
        IPage<TestEntity> result = testMapper.selectPage(page, wrapper);
        
        // 验证返回的数据按指定顺序排列
        assertNotNull(result, "分页结果不应该为 null");
        assertEquals(5, result.getRecords().size(), "应该返回 5 条数据");
        
        // 验证数据按 ID 降序排列
        List<TestEntity> records = result.getRecords();
        for (int i = 0; i < records.size() - 1; i++) {
            assertTrue(records.get(i).getId() > records.get(i + 1).getId(), 
                    "数据应该按 ID 降序排列");
        }
        
        // 验证分页信息正确
        assertEquals(10, result.getTotal(), "总记录数应该是 10");
        assertEquals(2, result.getPages(), "总页数应该是 2");
    }

    /**
     * TC-021: 测试翻页到下一页
     * 验证从第一页翻到第二页的功能
     */
    @Test
    void testPaginationNextPage() {
        // 预先插入多条测试数据（至少 10 条）
        prepareTestData(10);
        
        // 查询第一页数据，验证数据正确
        Page<TestEntity> page1 = new Page<>(1, 5);
        IPage<TestEntity> result1 = testMapper.selectPage(page1, null);
        assertEquals(1, result1.getCurrent(), "当前页应该是 1");
        assertEquals(5, result1.getRecords().size(), "第一页应该返回 5 条数据");
        
        // 使用第一页的 Page 对象，设置 current 为 2
        Page<TestEntity> page2 = new Page<>(2, 5);
        
        // 再次调用 selectPage() 查询第二页
        IPage<TestEntity> result2 = testMapper.selectPage(page2, null);
        
        // 验证返回的数据是第二页的数据
        assertNotNull(result2, "第二页结果不应该为 null");
        assertEquals(5, result2.getRecords().size(), "第二页应该返回 5 条数据");
        
        // 验证分页信息更新正确
        assertEquals(2, result2.getCurrent(), "当前页应该是 2");
        assertEquals(10, result2.getTotal(), "总记录数应该是 10");
        assertEquals(2, result2.getPages(), "总页数应该是 2");
        
        // 验证第二页的数据与第一页不同
        assertNotEquals(result1.getRecords().get(0).getId(), 
                result2.getRecords().get(0).getId(), "第二页的第一条数据应该与第一页不同");
    }

    /**
     * TC-022: 测试翻页到上一页
     * 验证从第二页翻到第一页的功能
     */
    @Test
    void testPaginationPreviousPage() {
        // 预先插入多条测试数据（至少 10 条）
        prepareTestData(10);
        
        // 查询第二页数据，验证数据正确
        Page<TestEntity> page2 = new Page<>(2, 5);
        IPage<TestEntity> result2 = testMapper.selectPage(page2, null);
        assertEquals(2, result2.getCurrent(), "当前页应该是 2");
        assertEquals(5, result2.getRecords().size(), "第二页应该返回 5 条数据");
        
        // 使用第二页的 Page 对象，设置 current 为 1
        Page<TestEntity> page1 = new Page<>(1, 5);
        
        // 再次调用 selectPage() 查询第一页
        IPage<TestEntity> result1 = testMapper.selectPage(page1, null);
        
        // 验证返回的数据是第一页的数据
        assertNotNull(result1, "第一页结果不应该为 null");
        assertEquals(5, result1.getRecords().size(), "第一页应该返回 5 条数据");
        
        // 验证分页信息更新正确
        assertEquals(1, result1.getCurrent(), "当前页应该是 1");
        assertEquals(10, result1.getTotal(), "总记录数应该是 10");
        assertEquals(2, result1.getPages(), "总页数应该是 2");
    }

    /**
     * TC-023: 测试翻页边界处理（首页）
     * 验证在首页时不能向前翻页
     */
    @Test
    void testPaginationBoundaryFirstPage() {
        // 预先插入多条测试数据
        prepareTestData(10);
        
        // 查询第一页数据
        Page<TestEntity> page1 = new Page<>(1, 5);
        IPage<TestEntity> result1 = testMapper.selectPage(page1, null);
        assertEquals(1, result1.getCurrent(), "当前页应该是 1");
        
        // 尝试翻到上一页（current = 0）
        Page<TestEntity> page0 = new Page<>(0, 5);
        IPage<TestEntity> result0 = testMapper.selectPage(page0, null);
        
        // 验证处理逻辑（应该能正常处理，但可能返回空数据或第一页数据）
        // MyBatis-Plus 会将 0 或负数转换为 1
        assertNotNull(result0, "结果不应该为 null");
        assertTrue(result0.getCurrent() >= 1, "当前页应该 >= 1");
    }

    /**
     * TC-024: 测试翻页边界处理（末页）
     * 验证在末页时不能向后翻页
     */
    @Test
    void testPaginationBoundaryLastPage() {
        // 预先插入多条测试数据（例如 13 条，每页 5 条，共 3 页）
        prepareTestData(13);
        
        // 查询最后一页（第 3 页）数据
        Page<TestEntity> page3 = new Page<>(3, 5);
        IPage<TestEntity> result3 = testMapper.selectPage(page3, null);
        assertEquals(3, result3.getCurrent(), "当前页应该是 3");
        assertEquals(3, result3.getRecords().size(), "最后一页应该返回 3 条数据");
        
        // 尝试翻到下一页（current = 4）
        Page<TestEntity> page4 = new Page<>(4, 5);
        IPage<TestEntity> result4 = testMapper.selectPage(page4, null);
        
        // 验证处理逻辑（应该返回空数据或最后一页数据）
        assertNotNull(result4, "结果不应该为 null");
        assertTrue(result4.getRecords().isEmpty() || result4.getCurrent() <= 3, 
                "应该返回空数据或保持在最后一页");
    }

    /**
     * TC-025: 测试跳转到指定页
     * 验证直接跳转到指定页面的功能
     */
    @Test
    void testPaginationJumpToPage() {
        // 预先插入多条测试数据（至少 15 条）
        prepareTestData(15);
        
        // 创建 Page 对象，直接设置 current 为 3
        Page<TestEntity> page = new Page<>(3, 5);
        
        // 创建查询条件，按 ID 升序排序，确保数据顺序可预测
        LambdaQueryWrapper<TestEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(TestEntity::getId);
        
        // 调用 selectPage() 查询第三页
        IPage<TestEntity> result = testMapper.selectPage(page, wrapper);
        
        // 验证返回的数据是第三页的数据
        assertNotNull(result, "分页结果不应该为 null");
        assertEquals(5, result.getRecords().size(), "第三页应该返回 5 条数据");
        
        // 验证分页信息正确
        assertEquals(3, result.getCurrent(), "当前页应该是 3");
        assertEquals(15, result.getTotal(), "总记录数应该是 15");
        assertEquals(3, result.getPages(), "总页数应该是 3");
        
        // 验证返回的数据确实是第三页的数据（ID 应该是 11-15，因为已重置 AUTO_INCREMENT 并从 1 开始）
        List<TestEntity> records = result.getRecords();
        assertEquals(11L, records.get(0).getId(), "第一条记录的 ID 应该是 11");
        assertEquals(15L, records.get(4).getId(), "最后一条记录的 ID 应该是 15");
    }
}

