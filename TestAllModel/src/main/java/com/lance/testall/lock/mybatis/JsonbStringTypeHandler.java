package com.lance.testall.lock.mybatis;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;
import org.postgresql.util.PGobject;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 将 Java {@link String}（JSON 文本）写入 PostgreSQL {@code JSONB} 列；
 * H2 / MySQL 等使用 {@code TEXT} 列时退化为普通 {@link String} 绑定。
 */
@MappedTypes(String.class)
@MappedJdbcTypes(JdbcType.VARCHAR)
public class JsonbStringTypeHandler extends BaseTypeHandler<String> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, String parameter, JdbcType jdbcType)
            throws SQLException {
        if (isPostgreSql(ps.getConnection())) {
            PGobject jsonb = new PGobject();
            jsonb.setType("jsonb");
            jsonb.setValue(parameter);
            ps.setObject(i, jsonb);
        } else {
            ps.setString(i, parameter);
        }
    }

    @Override
    public String getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return rs.getString(columnName);
    }

    @Override
    public String getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return rs.getString(columnIndex);
    }

    @Override
    public String getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return cs.getString(columnIndex);
    }

    private static boolean isPostgreSql(Connection connection) throws SQLException {
        return "PostgreSQL".equalsIgnoreCase(connection.getMetaData().getDatabaseProductName());
    }
}
