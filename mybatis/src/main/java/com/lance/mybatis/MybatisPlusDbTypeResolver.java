package com.lance.mybatis;

import com.baomidou.mybatisplus.annotation.DbType;

/**
 * 将配置项 mybatis-plus.db-type 解析为 MyBatis-Plus {@link DbType}。
 */
public final class MybatisPlusDbTypeResolver {

    private MybatisPlusDbTypeResolver() {
    }

    /**
     * @param dbType 配置值，如 mysql、postgresql、h2；空则默认 MYSQL
     */
    public static DbType resolve(String dbType) {
        if (dbType == null || dbType.isBlank()) {
            return DbType.MYSQL;
        }
        return switch (dbType.trim().toLowerCase()) {
            case "postgresql", "postgres", "pg" -> DbType.POSTGRE_SQL;
            case "h2" -> DbType.H2;
            case "mysql" -> DbType.MYSQL;
            default -> DbType.MYSQL;
        };
    }
}
