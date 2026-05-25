-- PostgreSQL 建表脚本（test_entity）
CREATE TABLE IF NOT EXISTS test_entity (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100),
    description VARCHAR(255)
);

-- JDK 线程池实验：任务执行日志
CREATE TABLE IF NOT EXISTS thread_pool_task_log (
    id             BIGSERIAL PRIMARY KEY,
    batch_id       VARCHAR(64)  NOT NULL,
    task_index     INT          NOT NULL,
    executor_type  VARCHAR(16)  NOT NULL DEFAULT 'JDK',
    thread_name    VARCHAR(128),
    status         VARCHAR(32)  NOT NULL,
    error_message  VARCHAR(512),
    batch_tag      VARCHAR(128),
    created_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    finished_at    TIMESTAMP
);
