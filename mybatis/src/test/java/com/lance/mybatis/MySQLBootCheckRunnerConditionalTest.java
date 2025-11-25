package com.lance.mybatis;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * MySQLBootCheckRunner 条件测试类
 * 测试 Runner Bean 的条件创建
 */
@SpringBootTest(classes = {MyBatisPlusAutoConfiguration.class})
@TestPropertySource(properties = {"mybatis.boot.check.enable=true"})
class MySQLBootCheckRunnerConditionalTest {

    /**
     * TC-004: 测试 MySQLBootCheckRunner Bean 创建（启用检查）
     */
    @Test
    void testMySQLBootCheckRunnerEnabled(@Autowired ApplicationContext context) {
        // 验证当配置启用时，Runner Bean 能被创建
        MySQLBootCheckRunner runner = context.getBean(MySQLBootCheckRunner.class);
        assertNotNull(runner, "配置启用时，Runner Bean 应该被创建");
    }
}

