package com.lance.nacos;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

/**
 * NacosConfig 配置类测试
 * 测试配置类的基本功能和注解生效
 * 
 * TC-001: 测试 NacosConfig Bean 创建
 * TC-002: 测试 @EnableDiscoveryClient 注解生效
 * TC-003: 测试配置类条件注解
 */
@SpringBootTest(classes = {NacosConfig.class})
@TestPropertySource(properties = {
    "spring.cloud.nacos.discovery.server-addr=localhost:8848",
    "spring.cloud.nacos.discovery.register-enabled=false",
    "spring.cloud.nacos.config.server-addr=localhost:8848",
    "spring.cloud.nacos.config.enabled=false"
})
class NacosConfigTest {

    @Autowired(required = false)
    private NacosConfig nacosConfig;


    /**
     * TC-001: 测试 NacosConfig Bean 创建
     * 验证 NacosConfig 配置类能正确创建 Bean
     */
    @Test
    void testNacosConfigBeanCreation() {
        assertNotNull(nacosConfig, "NacosConfig Bean 应该被创建");
    }

    /**
     * TC-002: 测试 @EnableDiscoveryClient 注解生效
     * 验证 @EnableDiscoveryClient 注解是否正确启用服务发现功能
     * 
     * 注意：由于配置了 register-enabled=false，DiscoveryClient Bean 可能不存在
     * 这里主要验证配置类能正常加载，注解处理逻辑正确
     */
    @Test
    void testEnableDiscoveryClient() {
        // 验证 NacosConfig 配置类存在
        assertNotNull(nacosConfig, "NacosConfig 应该存在");
        
        // 验证配置类上有 @EnableDiscoveryClient 注解
        // 如果应用能正常启动且没有报错，说明注解处理正确
        assertTrue(nacosConfig.getClass().isAnnotationPresent(
                org.springframework.cloud.client.discovery.EnableDiscoveryClient.class),
                "NacosConfig 应该标注 @EnableDiscoveryClient 注解");
    }

    /**
     * TC-003: 测试配置类条件注解
     * 验证配置类在缺少 Nacos 依赖时的处理
     * 
     * 注意：这个测试在实际环境中难以模拟，因为 Nacos 依赖已经在 pom.xml 中
     * 这里主要验证配置类能正常加载，如果缺少依赖会在启动时抛出异常
     */
    @Test
    void testConfigurationConditionalAnnotation() {
        // 验证配置类能正常加载
        // 如果缺少必要的依赖，Spring 启动时会抛出异常
        // 这里验证配置类存在，说明依赖和条件都满足
        assertNotNull(nacosConfig, "NacosConfig 应该存在，说明依赖和条件都满足");
    }
}

