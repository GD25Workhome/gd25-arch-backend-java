package com.lance.nacos;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import com.alibaba.cloud.nacos.NacosDiscoveryProperties;
import com.lance.nacos.test.TestApplication;

/**
 * Nacos 服务发现配置属性测试类
 * TC-021: 测试服务发现配置属性
 */
@SpringBootTest(classes = {TestApplication.class, NacosConfig.class})
@TestPropertySource(properties = {
    "spring.cloud.nacos.discovery.server-addr=localhost:8848",
    "spring.cloud.nacos.discovery.namespace=test-namespace",
    "spring.cloud.nacos.discovery.service=test-service",
    "spring.cloud.nacos.discovery.group=TEST_GROUP",
    "spring.cloud.nacos.discovery.register-enabled=false",
    "spring.cloud.nacos.config.server-addr=localhost:8848",
    "spring.cloud.nacos.config.enabled=false"
})
class NacosDiscoveryPropertiesTest {

    @Autowired(required = false)
    private NacosDiscoveryProperties nacosDiscoveryProperties;

    /**
     * TC-021: 测试服务发现配置属性
     * 验证服务发现相关配置属性是否正确生效
     */
    @Test
    void testDiscoveryProperties() {
        assertNotNull(nacosDiscoveryProperties, 
            """
            NacosDiscoveryProperties 应该存在。请确保：
            1. Spring Boot 应用上下文正确初始化
            2. Nacos 相关依赖已正确配置
            """);
        
        // 验证服务器地址
        assertNotNull(nacosDiscoveryProperties.getServerAddr(), 
                "服务发现服务器地址应该被配置");
        assertTrue(nacosDiscoveryProperties.getServerAddr().contains("8848"),
                "服务器地址应该包含端口 8848");
        
        // 验证命名空间
        assertNotNull(nacosDiscoveryProperties.getNamespace(),
                "命名空间应该被配置");
        assertEquals("test-namespace", nacosDiscoveryProperties.getNamespace(),
                "命名空间应该为 test-namespace");
        
        // 验证服务名称
        assertNotNull(nacosDiscoveryProperties.getService(),
                "服务名称应该被配置");
        assertEquals("test-service", nacosDiscoveryProperties.getService(),
                "服务名称应该为 test-service");
        
        // 验证分组
        assertNotNull(nacosDiscoveryProperties.getGroup(),
                "分组应该被配置");
        assertEquals("TEST_GROUP", nacosDiscoveryProperties.getGroup(),
                "分组应该为 TEST_GROUP");
    }
}

