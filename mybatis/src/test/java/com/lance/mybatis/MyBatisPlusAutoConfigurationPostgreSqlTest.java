package com.lance.mybatis;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;

@SpringBootTest(classes = {MyBatisPlusAutoConfiguration.class})
@TestPropertySource(properties = {"mybatis-plus.db-type=postgresql", "mybatis.boot.check.enable=false"})
class MyBatisPlusAutoConfigurationPostgreSqlTest {

    @Autowired
    private MybatisPlusInterceptor mybatisPlusInterceptor;

    @Test
    void paginationUsesPostgreSqlDbType() {
        assertNotNull(mybatisPlusInterceptor);
        PaginationInnerInterceptor pagination = (PaginationInnerInterceptor) mybatisPlusInterceptor.getInterceptors().stream()
                .filter(i -> i instanceof PaginationInnerInterceptor)
                .findFirst()
                .orElseThrow();
        assertEquals(DbType.POSTGRE_SQL, pagination.getDbType());
    }
}
