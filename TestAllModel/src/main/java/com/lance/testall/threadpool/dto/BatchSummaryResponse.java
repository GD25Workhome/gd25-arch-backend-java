package com.lance.testall.threadpool.dto;

import java.util.List;

/**
 * 批次汇总查询响应。
 */
public class BatchSummaryResponse {

    private String batchId;
    private int total;
    private int success;
    private int failed;
    private int rejected;
    private int pending;
    private List<StatusCount> byStatus;
    private List<ThreadNameCount> byThreadName;

    public String getBatchId() {
        return batchId;
    }

    public void setBatchId(String batchId) {
        this.batchId = batchId;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public int getSuccess() {
        return success;
    }

    public void setSuccess(int success) {
        this.success = success;
    }

    public int getFailed() {
        return failed;
    }

    public void setFailed(int failed) {
        this.failed = failed;
    }

    public int getRejected() {
        return rejected;
    }

    public void setRejected(int rejected) {
        this.rejected = rejected;
    }

    public int getPending() {
        return pending;
    }

    public void setPending(int pending) {
        this.pending = pending;
    }

    public List<StatusCount> getByStatus() {
        return byStatus;
    }

    public void setByStatus(List<StatusCount> byStatus) {
        this.byStatus = byStatus;
    }

    public List<ThreadNameCount> getByThreadName() {
        return byThreadName;
    }

    public void setByThreadName(List<ThreadNameCount> byThreadName) {
        this.byThreadName = byThreadName;
    }

    public static class StatusCount {
        private String status;
        private long count;

        public StatusCount() {
        }

        public StatusCount(String status, long count) {
            this.status = status;
            this.count = count;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public long getCount() {
            return count;
        }

        public void setCount(long count) {
            this.count = count;
        }
    }

    public static class ThreadNameCount {
        private String threadName;
        private long count;

        public ThreadNameCount() {
        }

        public ThreadNameCount(String threadName, long count) {
            this.threadName = threadName;
            this.count = count;
        }

        public String getThreadName() {
            return threadName;
        }

        public void setThreadName(String threadName) {
            this.threadName = threadName;
        }

        public long getCount() {
            return count;
        }

        public void setCount(long count) {
            this.count = count;
        }
    }
}
