package com.lance.mybatis;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

import com.baomidou.mybatisplus.annotation.DbType;

class MybatisPlusDbTypeResolverTest {

    @Test
    void resolvePostgreSqlAliases() {
        assertEquals(DbType.POSTGRE_SQL, MybatisPlusDbTypeResolver.resolve("postgresql"));
        assertEquals(DbType.POSTGRE_SQL, MybatisPlusDbTypeResolver.resolve("postgres"));
        assertEquals(DbType.POSTGRE_SQL, MybatisPlusDbTypeResolver.resolve("pg"));
    }

    @Test
    void resolveMysqlAndH2() {
        assertEquals(DbType.MYSQL, MybatisPlusDbTypeResolver.resolve("mysql"));
        assertEquals(DbType.H2, MybatisPlusDbTypeResolver.resolve("h2"));
        assertEquals(DbType.MYSQL, MybatisPlusDbTypeResolver.resolve(null));
        assertEquals(DbType.MYSQL, MybatisPlusDbTypeResolver.resolve(""));
    }
}
