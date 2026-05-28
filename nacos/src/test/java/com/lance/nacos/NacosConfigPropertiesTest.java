package com.lance.nacos;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.test.context.TestPropertySource;

import com.alibaba.cloud.nacos.NacosConfigProperties;
import com.lance.nacos.test.TestApplication;

/**
 * Nacos 配置中心配置属性测试类
 * TC-022: 测试配置中心配置属性
 */
@SpringBootTest(classes = {TestApplication.class, NacosConfig.class})
@TestPropertySource(properties = {
    "spring.profiles.active=",
    "spring.cloud.bootstrap.enabled=false",
    "spring.cloud.nacos.discovery.server-addr=localhost:8848",
    "spring.cloud.nacos.discovery.register-enabled=false",
    "spring.cloud.nacos.config.enabled=true",
    "spring.nacos.config.server-addr=localhost:8848",
    "spring.nacos.config.prefix=test-app",
    "spring.nacos.config.file-extension=yaml"
})
class NacosConfigPropertiesTest {

    @Autowired(required = false)
    private NacosConfigProperties nacosConfigProperties;

    @Autowired
    private Environment environment;

    /**
     * TC-022: 测试配置中心配置属性
     * SCA 2023.0.3.x 使用 spring.nacos.config.* 前缀；部分字段不再通过旧 Bean 绑定暴露。
     */
    @Test
    void testConfigProperties() {
        assertNotNull(nacosConfigProperties, "NacosConfigProperties 应该存在");

        assertEquals("localhost:8848", environment.getProperty("spring.nacos.config.server-addr"),
                "配置中心 server-addr 应来自测试属性");
        assertEquals("test-app", environment.getProperty("spring.nacos.config.prefix"),
                "配置前缀应来自测试属性");
        assertEquals("yaml", environment.getProperty("spring.nacos.config.file-extension"),
                "文件扩展名应来自测试属性");

        String serverAddr = nacosConfigProperties.getServerAddr();
        if (serverAddr != null) {
            assertTrue(serverAddr.contains("8848"), "Bean 中 server-addr 应包含端口 8848");
        }
    }
}
