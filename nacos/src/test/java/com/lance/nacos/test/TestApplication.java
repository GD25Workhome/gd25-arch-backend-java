package com.lance.nacos.test;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 测试应用主类
 * 用于测试 Spring Boot 上下文启动和 Nacos 集成
 */
@SpringBootApplication
public class TestApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(TestApplication.class, args);
    }
}

