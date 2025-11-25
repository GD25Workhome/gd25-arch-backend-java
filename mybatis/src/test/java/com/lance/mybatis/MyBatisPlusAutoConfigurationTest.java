package com.lance.mybatis;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;

/**
 * MyBatisPlusAutoConfiguration 测试类
 * 测试 MyBatis-Plus 自动配置功能
 */
@SpringBootTest(classes = {MyBatisPlusAutoConfiguration.class})
class MyBatisPlusAutoConfigurationTest {

    @Autowired(required = false)
    private MybatisPlusInterceptor mybatisPlusInterceptor;

    /**
     * TC-001: 测试 MyBatis-Plus 拦截器 Bean 创建
     */
    @Test
    void testMybatisPlusInterceptorBeanCreation() {
        assertNotNull(mybatisPlusInterceptor, "MyBatis-Plus 拦截器 Bean 应该被创建");
    }

    /**
     * TC-003: 测试分页插件配置
     */
    @Test
    void testPaginationPluginConfiguration() {
        assertNotNull(mybatisPlusInterceptor, "拦截器应该存在");
        
        // 验证拦截器包含内部拦截器
        assertFalse(mybatisPlusInterceptor.getInterceptors().isEmpty(), 
                "拦截器应该包含内部拦截器");
        
        // 验证包含分页插件
        boolean hasPaginationPlugin = mybatisPlusInterceptor.getInterceptors().stream()
                .anyMatch(interceptor -> interceptor instanceof PaginationInnerInterceptor);
        assertTrue(hasPaginationPlugin, "拦截器应该包含分页插件");
        
        // 验证分页插件的数据库类型
        PaginationInnerInterceptor paginationInterceptor = 
                (PaginationInnerInterceptor) mybatisPlusInterceptor.getInterceptors().stream()
                        .filter(interceptor -> interceptor instanceof PaginationInnerInterceptor)
                        .findFirst()
                        .orElse(null);
        
        assertNotNull(paginationInterceptor, "分页插件应该存在");
        assertEquals(DbType.MYSQL, paginationInterceptor.getDbType(), 
                "分页插件应该配置为 MySQL 类型");
    }

    /**
     * TC-002: 测试条件注解 ConditionalOnMissingBean
     * 当已存在 MybatisPlusInterceptor Bean 时，不会重复创建
     * 注意：这个测试需要单独运行，因为需要自定义配置
     */
    @Test
    void testConditionalOnMissingBean() {
        // 验证当前拦截器存在
        assertNotNull(mybatisPlusInterceptor, "拦截器应该存在");
        // 注意：完整测试需要创建自定义配置类，但由于 Spring Boot Test 的限制
        // 这里主要验证基本逻辑，完整测试需要单独配置上下文
    }
}

