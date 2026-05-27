CREATE TABLE IF NOT EXISTS test_entity (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100),
    description VARCHAR(255)
);

-- JDK 线程池实验：任务执行日志
CREATE TABLE IF NOT EXISTS thread_pool_task_log (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
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

-- 并发锁实验：库存与批次日志
CREATE TABLE IF NOT EXISTS lock_demo_stock (
    sku_id     VARCHAR(64)  PRIMARY KEY,
    stock      INT          NOT NULL,
    version    INT          NOT NULL DEFAULT 0,
    updated_at TIMESTAMP    DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS lock_demo_run_log (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    batch_id            VARCHAR(64)  NOT NULL,
    lock_strategy       VARCHAR(32)  NOT NULL,
    thread_count        INT          NOT NULL,
    requests_per_thread INT          NOT NULL,
    success_count       INT          NOT NULL,
    fail_count          INT          NOT NULL,
    error_count         INT          NOT NULL,
    result_breakdown    TEXT,
    initial_stock       INT          NOT NULL,
    final_stock         INT          NOT NULL,
    anomaly             BOOLEAN      NOT NULL,
    anomaly_reason      VARCHAR(64),
    elapsed_ms          BIGINT,
    instance_id         VARCHAR(64),
    batch_tag           VARCHAR(128),
    created_at          TIMESTAMP    DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_lock_run_batch ON lock_demo_run_log(batch_id);
