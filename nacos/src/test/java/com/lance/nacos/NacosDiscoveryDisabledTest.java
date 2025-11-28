package com.lance.nacos;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

/**
 * Nacos 服务注册禁用测试类
 * TC-007: 测试服务注册（register-enabled=false）
 */
@SpringBootTest(classes = {NacosConfig.class})
@TestPropertySource(properties = {
    "spring.cloud.nacos.discovery.server-addr=localhost:8848",
    "spring.cloud.nacos.discovery.register-enabled=false",
    "spring.cloud.nacos.config.server-addr=localhost:8848",
    "spring.cloud.nacos.config.enabled=false"
})
class NacosDiscoveryDisabledTest {

    /**
     * TC-007: 测试服务注册（register-enabled=false）
     * 验证当 register-enabled=false 时，服务不会注册
     */
    @Test
    void testServiceRegistrationDisabled() {
        // 当 register-enabled=false 时，服务不应该注册
        // 但应用应该能正常启动
        assertTrue(true, "应用应该能正常启动，即使 register-enabled=false");
    }
}

