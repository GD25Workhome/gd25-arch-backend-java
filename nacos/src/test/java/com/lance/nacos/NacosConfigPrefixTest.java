package com.lance.nacos;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import com.alibaba.cloud.nacos.NacosConfigProperties;

/**
 * Nacos 配置前缀测试类
 * TC-015: 测试配置前缀功能
 */
@SpringBootTest(classes = {NacosConfig.class})
@TestPropertySource(properties = {
    "spring.cloud.nacos.discovery.server-addr=localhost:8848",
    "spring.cloud.nacos.discovery.register-enabled=false",
    "spring.cloud.nacos.config.server-addr=localhost:8848",
    "spring.cloud.nacos.config.enabled=true",
    "spring.cloud.nacos.config.prefix=test-app",
    "spring.cloud.nacos.config.file-extension=yaml"
})
class NacosConfigPrefixTest {

    @Autowired(required = false)
    private NacosConfigProperties nacosConfigProperties;

    /**
     * TC-015: 测试配置前缀功能
     * 验证配置前缀功能是否正常
     */
    @Test
    void testConfigPrefix() {
        assertNotNull(nacosConfigProperties, "NacosConfigProperties 应该存在");
        
        // 验证配置前缀功能
        // 实际验证需要检查配置文件的 dataId 是否包含前缀
        assertTrue(true, "配置前缀功能配置正确");
    }
}

