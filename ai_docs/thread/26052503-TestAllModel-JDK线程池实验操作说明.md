# TestAllModel：JDK 线程池三组对照实验操作说明

> **设计文档**：[26052502-TestAllModel-JDK线程池批量写库实验设计.md](./26052502-TestAllModel-JDK线程池批量写库实验设计.md)  
> **理论总纲**：[26052501-Spring线程池使用与学习路径.md](./26052501-Spring线程池使用与学习路径.md)

## 启动应用

```bash
cd TestAllModel
mvn spring-boot:run
# 或根目录：
mvn -pl TestAllModel -am spring-boot:run
```

默认端口：`8080`，H2 内存库，表 `thread_pool_task_log` 随 `schema.sql` 自动初始化。

---

## API 速查

| 方法 | 路径 |
|------|------|
| POST | `http://localhost:8080/api/thread-pool/demo/submit` |
| GET | `http://localhost:8080/api/thread-pool/demo/batch/{batchId}` |
| GET | `http://localhost:8080/api/thread-pool/demo/pool/stats` |

---

## 实验 1：正常吞吐

**目的**：全部 SUCCESS；总耗时明显小于串行 `taskCount × workDelayMs`；`thread_name` 种类 ≤ `maxPoolSize`。

```bash
curl -s -X POST http://localhost:8080/api/thread-pool/demo/submit \
  -H "Content-Type: application/json" \
  -d '{"taskCount":10,"workDelayMs":100,"waitForComplete":true,"batchTag":"exp1"}' | jq .
```

记下响应中的 `batchId`，查询汇总：

```bash
BATCH_ID="<上一步的 batchId>"
curl -s "http://localhost:8080/api/thread-pool/demo/batch/${BATCH_ID}" | jq .
curl -s http://localhost:8080/api/thread-pool/demo/pool/stats | jq .
```

**预期**：

- `success: 10`，`rejected: 0`
- `elapsedMs` 远小于 `1000`（约几百毫秒量级，取决于池大小）
- `byThreadName` 仅少数条目（如 `tp-demo-1` … `tp-demo-4`）

---

## 实验 2：队列积压

**目的**：任务在队列中等待；`waitForComplete=true` 时 API 响应变慢。

```bash
curl -s -X POST http://localhost:8080/api/thread-pool/demo/submit \
  -H "Content-Type: application/json" \
  -d '{"taskCount":50,"workDelayMs":300,"waitForComplete":true,"batchTag":"exp2"}' | jq .
```

**观察**：

- `elapsedMs` 明显增大（仍应小于串行 `50×300=15000` ms）
- `pool/stats` 中 `queueSize` 在提交过程中可能 > 0
- H2 控制台或后续 SQL：`created_at` 与 `finished_at` 存在时间差

---

## 实验 3：触发拒绝

**目的**：部分任务 `REJECTED`；理解 `AbortPolicy` 与池参数关系。

先以 **小池 profile** 启动：

```bash
mvn -pl TestAllModel -am spring-boot:run -Dspring-boot.run.profiles=exp3-reject
```

或：

```bash
java -jar TestAllModel/target/testAllModel-*.jar --spring.profiles.active=exp3-reject
```

`exp3-reject` 配置：`core=1, max=2, queue=5`。

```bash
curl -s -X POST http://localhost:8080/api/thread-pool/demo/submit \
  -H "Content-Type: application/json" \
  -d '{"taskCount":30,"workDelayMs":200,"waitForComplete":true,"batchTag":"exp3"}' | jq .
```

**预期**：

- `rejected` > 0，`success` + `rejected` + `failed` = 30
- `byStatus` 含 `REJECTED`
- 被拒绝记录的 `thread_name` 多为 **Tomcat 请求线程**（拒绝策略在提交线程登记）

### 对比：CallerRunsPolicy（背压）

修改 `application-exp3-reject.yml`：

```yaml
thread-pool:
  demo:
    rejection-policy: caller-runs
```

重启后同样请求：`rejected` 应为 0，但 `elapsedMs` 显著变长（部分任务在 API 线程执行）。

---

## H2 查询示例

应用运行期间可访问 H2 Console（若已配置）或直接通过批次 API。逻辑等价 SQL：

```sql
SELECT batch_id, thread_name, status, COUNT(*) AS cnt
FROM thread_pool_task_log
WHERE batch_id = '<batchId>'
GROUP BY batch_id, thread_name, status
ORDER BY thread_name, status;
```

---

## 配置说明

默认 `application.yml`：

```yaml
thread-pool:
  demo:
    core-pool-size: 2
    max-pool-size: 4
    queue-capacity: 10
    rejection-policy: abort   # abort | caller-runs | discard | discard-oldest
```

---

## 代码位置

```
TestAllModel/src/main/java/com/lance/testall/threadpool/
├── config/JdkThreadPoolConfig.java
├── controller/ThreadPoolDemoController.java
├── service/ThreadPoolDemoService.java
├── service/ThreadPoolTaskRunner.java
└── ...
```
