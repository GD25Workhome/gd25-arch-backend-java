package com.lance.mybatis;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * MySQLBootCheckRunner 禁用测试类
 * 测试禁用检查时 Runner Bean 不被创建
 */
@SpringBootTest(classes = {MyBatisPlusAutoConfiguration.class})
@TestPropertySource(properties = {"mybatis.boot.check.enable=false"})
class MySQLBootCheckRunnerDisabledTest {

    /**
     * TC-005: 测试 MySQLBootCheckRunner Bean 不创建（禁用检查）
     */
    @Test
    void testMySQLBootCheckRunnerDisabled(@Autowired ApplicationContext context) {
        // 验证当配置禁用时，Runner Bean 不会被创建
        assertThrows(org.springframework.beans.factory.NoSuchBeanDefinitionException.class, 
                () -> context.getBean(MySQLBootCheckRunner.class),
                "配置禁用时，Runner Bean 不应该被创建");
    }
}

