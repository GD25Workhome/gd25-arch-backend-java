# TestAllModel：Spring ThreadPoolTaskExecutor 封装实验总结

> **理论**：[26052501-Spring线程池使用与学习路径.md](./26052501-Spring线程池使用与学习路径.md) 第 2 步  
> **JDK 对照**：[26052502](./26052502-TestAllModel-JDK线程池批量写库实验设计.md) / [26052503 操作说明](./26052503-TestAllModel-JDK线程池实验操作说明.md)  
> **状态**：已实现，可与 JDK 版 A/B 对照

---

## 一、实现目标

在 **阶段 0（JDK `ThreadPoolExecutor`）** 基础上，用 **Spring `ThreadPoolTaskExecutor`** 复现同一套「API → 线程池 → 写库 → 查批次」实验，便于对照学习：

| 维度 | JDK 路径 | Spring 路径 |
|------|----------|-------------|
| 配置类 | `JdkThreadPoolConfig` | `SpringThreadPoolTaskConfig` |
| 执行器 Bean | `demoThreadPoolExecutor` | `springDemoThreadPoolTaskExecutor` |
| API 前缀 | `/api/thread-pool/demo` | `/api/thread-pool/spring` |
| 线程名前缀 | `tp-demo-*` | `tp-spring-*` |
| 日志字段 `executor_type` | `JDK` | `SPRING` |

业务写库逻辑 **共用** `ThreadPoolTaskRunner` 与 `BatchSubmitOrchestrator`，差异仅在「如何创建/提交到池」。

---

## 二、代码结构

```
TestAllModel/.../threadpool/
├── config/
│   ├── JdkThreadPoolConfig.java              # JDK 池
│   ├── SpringThreadPoolTaskConfig.java       # Spring 池 + initialize()
│   ├── ThreadPoolDemoProperties.java         # thread-pool.demo
│   ├── ThreadPoolSpringDemoProperties.java   # thread-pool.spring-demo
│   └── ThreadPoolRejectionPolicyResolver.java
├── service/
│   ├── BatchSubmitOrchestrator.java          # 共用提交/查询
│   ├── ThreadPoolDemoService.java            # JDK 门面
│   ├── SpringThreadPoolDemoService.java      # Spring 门面
│   └── ThreadPoolTaskRunner.java
├── controller/
│   ├── ThreadPoolDemoController.java
│   └── SpringThreadPoolDemoController.java
└── support/ DemoPoolTask, DemoRejectedExecutionHandler
```

---

## 三、Spring 封装要点（相对 JDK 多出来的）

| 能力 | 实现位置 |
|------|----------|
| `initialize()` | `SpringThreadPoolTaskConfig` 创建 Bean 后必须调用，否则池未启动 |
| 优雅停机 | `setWaitForTasksToCompleteOnShutdown(true)` + `setAwaitTerminationSeconds`（由 Spring 容器销毁时处理，无需手写 `@PreDestroy`） |
| 线程名 | `setThreadNamePrefix("tp-spring-")` |
| 拒绝策略 | `setRejectedExecutionHandler`，复用 `DemoRejectedExecutionHandler` |
| 观测底层池 | `springExecutor.getThreadPoolExecutor()` → `pool/stats` |

配置前缀：`application.yml` → `thread-pool.spring-demo`（参数与 `thread-pool.demo` 对称，便于公平对比）。

---

## 四、JDK vs Spring 的一个重要差异（对比时要知）

| 项目 | JDK 实验池 | Spring 实验池 |
|------|------------|---------------|
| 工作队列 | `ArrayBlockingQueue`（显式指定） | `LinkedBlockingQueue`（`ThreadPoolTaskExecutor` 默认，由 `queueCapacity` 设容量） |
| 优雅停机 | `JdkThreadPoolConfig.@PreDestroy` 手写 | `waitForTasksToCompleteOnShutdown` 交给 Spring |

**相同 core/max/queue 数字时**，吞吐与拒绝 **大体接近**，但队列实现不同，极端压测下可能有细微差别。对照时请看 `GET .../pool/stats` 返回的 **`queueType`**。

---

## 五、API 与三组对照实验

### 5.1 启动

```bash
mvn -pl TestAllModel -am spring-boot:run
```

### 5.2 实验 1：正常吞吐（A/B）

```bash
# JDK
curl -s -X POST http://localhost:8080/api/thread-pool/demo/submit \
  -H "Content-Type: application/json" \
  -d '{"taskCount":10,"workDelayMs":100,"waitForComplete":true,"batchTag":"exp1-jdk"}'

# Spring（请求体相同，只换 URL）
curl -s -X POST http://localhost:8080/api/thread-pool/spring/submit \
  -H "Content-Type: application/json" \
  -d '{"taskCount":10,"workDelayMs":100,"waitForComplete":true,"batchTag":"exp1-spring"}'
```

**观察**：`executorType`、`elapsedMs`、`byThreadName`（`tp-demo-*` vs `tp-spring-*`）。

### 5.3 实验 2：队列积压

```bash
# 同上，taskCount=50, workDelayMs=300
curl -s -X POST http://localhost:8080/api/thread-pool/spring/submit \
  -H "Content-Type: application/json" \
  -d '{"taskCount":50,"workDelayMs":300,"waitForComplete":true,"batchTag":"exp2-spring"}'
```

对比 `GET /api/thread-pool/spring/pool/stats` 与 JDK 的 `.../demo/pool/stats` 中 `queueSize`。

### 5.4 实验 3：触发拒绝

```bash
# JDK
mvn -pl TestAllModel -am spring-boot:run -Dspring-boot.run.profiles=exp3-reject

# Spring（专用 profile）
mvn -pl TestAllModel -am spring-boot:run -Dspring-boot.run.profiles=exp3-reject-spring
```

```bash
curl -s -X POST http://localhost:8080/api/thread-pool/spring/submit \
  -H "Content-Type: application/json" \
  -d '{"taskCount":30,"workDelayMs":200,"waitForComplete":true,"batchTag":"exp3-spring"}'
```

### 5.5 查询批次

```bash
curl -s "http://localhost:8080/api/thread-pool/spring/batch/<batchId>"
curl -s http://localhost:8080/api/thread-pool/spring/pool/stats
```

---

## 六、数据库字段

表 `thread_pool_task_log` 新增 **`executor_type`**（`JDK` / `SPRING`），便于 SQL 过滤：

```sql
SELECT executor_type, thread_name, status, COUNT(*)
FROM thread_pool_task_log
WHERE batch_tag LIKE 'exp1%'
GROUP BY executor_type, thread_name, status;
```

---

## 七、学习检查清单

- [ ] 能说出 `ThreadPoolTaskExecutor` 与底层 `ThreadPoolExecutor` 的关系
- [ ] 知道必须 `initialize()`，以及不设时会出现什么问题
- [ ] 会用 `pool/stats` 对比 `queueType`（Array vs Linked）
- [ ] 同一请求体分别打 `/demo` 与 `/spring`，对比 `elapsedMs` 与 `rejected`
- [ ] 理解 Spring 优雅停机配置与 JDK `@PreDestroy` 的等价目的

---

## 八、后续（总纲实验 B～D）

| 步骤 | 建议在 TestAllModel 的扩展 |
|------|---------------------------|
| B `@Async` | 新接口调用 `@Async` Service 写库；另做「同类自调用」反面示例 |
| C `TaskDecorator` | 在 `ThreadPoolTaskExecutor` 上传播 `traceId` / MDC |
| D `@Scheduled` | 自定义 `ThreadPoolTaskScheduler` vs 默认单线程 |

---

## 相关文档

- [26052501 第 2 步 ThreadPoolTaskExecutor](./26052501-Spring线程池使用与学习路径.md#第-2-步spring-的-threadpooltaskexecutor项目里最常用)
- [26052501 第六节 动手练习](./26052501-Spring线程池使用与学习路径.md#六在本仓库中的动手练习顺序)
- [26052503 JDK 实验操作](./26052503-TestAllModel-JDK线程池实验操作说明.md)
