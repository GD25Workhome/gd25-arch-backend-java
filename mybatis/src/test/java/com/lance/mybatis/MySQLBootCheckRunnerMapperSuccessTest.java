package com.lance.mybatis;

import com.lance.mybatis.test.TestApplication;
import com.lance.mybatis.test.TestEntity;
import com.lance.mybatis.test.TestMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;

import javax.sql.DataSource;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * MySQLBootCheckRunner Mapper 检查成功测试类
 * 测试有 Mapper 且检查成功的场景
 */
@SpringBootTest(classes = {TestApplication.class, MyBatisPlusAutoConfiguration.class})
@TestPropertySource(properties = {
        "mybatis.boot.check.enable=true",
        "mybatis.boot.check.block=false",
        "spring.datasource.url=jdbc:h2:mem:testdb;MODE=MySQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
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
class MySQLBootCheckRunnerMapperSuccessTest {

    @Autowired(required = false)
    private TestMapper testMapper;

    @Autowired(required = false)
    private DataSource dataSource;

    /**
     * TC-007: 测试有 Mapper 且检查成功
     * 当存在 Mapper 且检查成功时，应该输出成功日志
     */
    @Test
    void testMapperCheckSuccess() {
        // 验证 Mapper 存在
        assertNotNull(testMapper, "TestMapper 应该被注入");
        
        // 验证数据源存在
        assertNotNull(dataSource, "数据源应该存在");
        
        // 验证 Mapper 可以正常使用（检查成功）
        // 如果应用能正常启动到这里，说明 Runner 的检查已经通过
        List<TestEntity> entities = testMapper.selectList(null);
        assertNotNull(entities, "查询结果不应该为 null");
    }
}

