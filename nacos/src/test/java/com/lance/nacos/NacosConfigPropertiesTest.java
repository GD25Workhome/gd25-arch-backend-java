package com.lance.nacos;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import com.alibaba.cloud.nacos.NacosConfigProperties;

/**
 * Nacos 配置中心配置属性测试类
 * TC-022: 测试配置中心配置属性
 */
@SpringBootTest(classes = {NacosConfig.class})
@TestPropertySource(properties = {
    "spring.cloud.nacos.discovery.server-addr=localhost:8848",
    "spring.cloud.nacos.discovery.register-enabled=false",
    "spring.cloud.nacos.config.server-addr=localhost:8848",
    "spring.cloud.nacos.config.namespace=test-namespace",
    "spring.cloud.nacos.config.prefix=test-app",
    "spring.cloud.nacos.config.file-extension=yaml",
    "spring.cloud.nacos.config.enabled=true"
})
class NacosConfigPropertiesTest {

    @Autowired(required = false)
    private NacosConfigProperties nacosConfigProperties;

    /**
     * TC-022: 测试配置中心配置属性
     * 验证配置中心相关配置属性是否正确生效
     */
    @Test
    void testConfigProperties() {
        assertNotNull(nacosConfigProperties, "NacosConfigProperties 应该存在");
        
        // 验证服务器地址
        assertNotNull(nacosConfigProperties.getServerAddr(),
                "配置中心服务器地址应该被配置");
        assertTrue(nacosConfigProperties.getServerAddr().contains("8848"),
                "服务器地址应该包含端口 8848");
        
        // 验证命名空间
        assertNotNull(nacosConfigProperties.getNamespace(),
                "命名空间应该被配置");
        assertEquals("test-namespace", nacosConfigProperties.getNamespace(),
                "命名空间应该为 test-namespace");
        
        // 验证前缀
        assertNotNull(nacosConfigProperties.getPrefix(),
                "配置前缀应该被配置");
        assertEquals("test-app", nacosConfigProperties.getPrefix(),
                "配置前缀应该为 test-app");
        
        // 验证文件扩展名
        assertNotNull(nacosConfigProperties.getFileExtension(),
                "文件扩展名应该被配置");
        assertEquals("yaml", nacosConfigProperties.getFileExtension(),
                "文件扩展名应该为 yaml");
    }
}

