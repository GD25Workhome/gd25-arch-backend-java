-- PostgreSQL：为已有 lock_demo_run_log 表增加 result_breakdown（思路 B 扩展字段）
-- 在目标库单独执行本脚本即可；新环境请直接用 schema-postgresql.sql 建表。

ALTER TABLE lock_demo_run_log
    ADD COLUMN IF NOT EXISTS result_breakdown JSONB;

COMMENT ON COLUMN lock_demo_run_log.result_breakdown IS
    '扣减结果明细 JSON：DeductResult 各枚举计数 + UNCAUGHT_EXCEPTION';
