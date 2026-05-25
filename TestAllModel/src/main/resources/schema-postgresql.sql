-- PostgreSQL 建表脚本（test_entity）
CREATE TABLE IF NOT EXISTS test_entity (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100),
    description VARCHAR(255)
);
