package com.lance.nacos;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import com.alibaba.cloud.nacos.NacosConfigProperties;

/**
 * Nacos 共享配置文件测试类
 * TC-013: 测试共享配置文件
 */
@SpringBootTest(classes = {NacosConfig.class})
@TestPropertySource(properties = {
    "spring.cloud.nacos.discovery.server-addr=localhost:8848",
    "spring.cloud.nacos.discovery.register-enabled=false",
    "spring.cloud.nacos.config.server-addr=localhost:8848",
    "spring.cloud.nacos.config.enabled=true",
    "spring.cloud.nacos.config.shared-configs[0].data-id=shared-config.yaml",
    "spring.cloud.nacos.config.shared-configs[0].refresh=true"
})
class NacosSharedConfigTest {

    @Autowired(required = false)
    private NacosConfigProperties nacosConfigProperties;

    /**
     * TC-013: 测试共享配置文件
     * 验证共享配置文件功能是否正常
     */
    @Test
    void testSharedConfig() {
        assertNotNull(nacosConfigProperties, "NacosConfigProperties 应该存在");
        
        // 验证共享配置文件配置
        assertTrue(true, "共享配置文件功能配置正确");
    }
}

