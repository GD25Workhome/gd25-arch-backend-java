package com.lance.testall;

import com.lance.mybatis.MyBatisPlusAutoConfiguration;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

/**
 * 聚合启动类：整合 web（REST）与 mybatis（持久层）。
 */
@SpringBootApplication(scanBasePackages = "com.lance.testall")
@MapperScan({"com.lance.testall.testentity.mapper", "com.lance.testall.threadpool.mapper"})
@Import(MyBatisPlusAutoConfiguration.class)
public class TestAllModelApplication {

    public static void main(String[] args) {
        SpringApplication.run(TestAllModelApplication.class, args);
    }
}
