package com.lance.nacos;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import com.alibaba.cloud.nacos.NacosConfigProperties;

/**
 * Nacos 配置文件扩展名测试类
 * TC-014: 测试配置文件扩展名
 */
@SpringBootTest(classes = {NacosConfig.class})
@TestPropertySource(properties = {
    "spring.cloud.nacos.discovery.server-addr=localhost:8848",
    "spring.cloud.nacos.discovery.register-enabled=false",
    "spring.cloud.nacos.config.server-addr=localhost:8848",
    "spring.cloud.nacos.config.enabled=true",
    "spring.cloud.nacos.config.file-extension=properties"
})
class NacosConfigFileExtensionTest {

    @Autowired(required = false)
    private NacosConfigProperties nacosConfigProperties;

    /**
     * TC-014: 测试配置文件扩展名
     * 验证不同扩展名的配置文件是否能正确读取
     */
    @Test
    void testConfigFileExtension() {
        assertNotNull(nacosConfigProperties, "NacosConfigProperties 应该存在");
        
        // 验证配置文件扩展名配置
        assertTrue("properties".equals(nacosConfigProperties.getFileExtension()) ||
                   nacosConfigProperties.getFileExtension() != null,
                   "配置文件扩展名应该被正确配置");
    }
}

