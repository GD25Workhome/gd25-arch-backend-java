# TestAllModel PostgreSQL 接入改造说明

> 文档日期：2026-05-25  
> 适用模块：`TestAllModel`（启动）、`mybatis`（持久层）

---

## 1. 改造目标

在保留默认 **H2 内存库** 的前提下，增加 **PostgreSQL** 连接能力，使 `TestAllModel` 可通过 Spring Profile 连接本地 PG，并正确执行 MyBatis-Plus 分页与 CRUD REST 接口。

---

## 2. 改造内容一览

| 位置 | 改动 |
|------|------|
| `mybatis/pom.xml` | 新增依赖 `org.postgresql:postgresql` |
| `mybatis/.../MybatisPlusDbTypeResolver.java` | **新增**：将配置 `mybatis-plus.db-type` 解析为 `DbType` |
| `mybatis/.../MyBatisPlusAutoConfiguration.java` | 分页插件由写死 `MYSQL` 改为读取 `mybatis-plus.db-type` |
| `mybatis/.../bootstrap-mybatis.yml` | 补充 `db-type` 说明与默认值 |
| `TestAllModel/.../application-postgresql.yml` | **新增**：PG 数据源与环境变量占位 |
| `TestAllModel/.../schema-postgresql.sql` | **新增**：PG 方言建表（`BIGSERIAL`） |
| `TestAllModel/.../application.yml` | 增加 `mybatis-plus.db-type: h2` 与 Profile 切换注释 |
| `TestAllModel/.../application-mysql.yml` | 增加 `mybatis-plus.db-type: mysql` |

业务代码（`TestEntityController`、`TestEntityMapper` 等）**无需修改**。

---

## 3. 多环境 Profile 对照

| Profile | 激活方式 | 数据源 | `mybatis-plus.db-type` | 建表脚本 |
|---------|----------|--------|------------------------|----------|
| 默认（无） | 直接启动 | H2 内存 `jdbc:h2:mem:testall` | `h2` | `schema.sql` |
| `mysql` | `--spring.profiles.active=mysql` | MySQL | `mysql` | `schema.sql` |
| `postgresql` | `--spring.profiles.active=postgresql` | PostgreSQL | `postgresql` | `schema-postgresql.sql` |

---

## 4. 连接本地 PostgreSQL 的前置条件

### 4.1 数据库侧

1. 本机已安装并启动 PostgreSQL（默认端口 **5432**）。
2. 创建数据库（若尚未存在）：

```sql
CREATE DATABASE testall;
```

3. 确认连接账号有该库的权限（默认示例用户 `postgres`）。

> 若库名、用户、密码与下文默认值不同，请通过环境变量覆盖（见第 5 节）。

### 4.2 应用侧

- 使用 Profile **`postgresql`** 启动（见第 5 节）。
- 首次启动且 `spring.sql.init.mode=always` 时，会自动执行 `schema-postgresql.sql` 创建表 `test_entity`。
- 若你已在 PG 中手工建表，可将 `application-postgresql.yml` 中 `spring.sql.init.mode` 改为 `never`，避免重复执行。

---

## 5. 启动方式

### 5.1 IDEA

1. 运行主类：`com.lance.testall.TestAllModelApplication`
2. **Active profiles** 填写：`postgresql`
3. 可选：在 Environment variables 中设置：

| 变量 | 含义 | 默认值（未设置时） |
|------|------|-------------------|
| `PG_URL` | JDBC URL | `jdbc:postgresql://localhost:5432/testall` |
| `PG_USER` | 用户名 | `postgres` |
| `PG_PASSWORD` | 密码 | 空 |

### 5.2 Maven 命令行

在仓库根目录或 `TestAllModel` 目录执行：

```bash
cd /path/to/gd25-arch-backend-java
mvn -pl TestAllModel -am spring-boot:run -Dspring-boot.run.profiles=postgresql
```

带环境变量示例（macOS / Linux）：

```bash
export PG_URL=jdbc:postgresql://localhost:5432/testall
export PG_USER=postgres
export PG_PASSWORD=你的密码
mvn -pl TestAllModel -am spring-boot:run -Dspring-boot.run.profiles=postgresql
```

### 5.3 仍使用 H2（默认，无需 PG）

```bash
mvn -pl TestAllModel -am spring-boot:run
```

不指定 Profile 即可，无需安装 PostgreSQL。

---

## 6. REST API 验证（与数据库无关，三种 Profile 通用）

服务默认端口：**8080**

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/test-entities` | 列表 |
| GET | `/api/test-entities/{id}` | 按 ID 查询 |
| POST | `/api/test-entities` | 新增，Body 示例：`{"name":"a","description":"b"}` |
| PUT | `/api/test-entities/{id}` | 更新 |
| DELETE | `/api/test-entities/{id}` | 删除 |

响应体为统一结构 `ApiResult`（`code` / `msg` / `data`）。

**curl 示例（PostgreSQL 启动后）：**

```bash
curl -s -X POST http://localhost:8080/api/test-entities \
  -H 'Content-Type: application/json' \
  -d '{"name":"pg-demo","description":"from postgresql"}'

curl -s http://localhost:8080/api/test-entities
```

---

## 7. 用客户端工具查看 PG 数据

使用 DBeaver、DataGrip、psql 等，连接类型选 **PostgreSQL**：

- Host：`localhost`
- Port：`5432`
- Database：`testall`（与 `PG_URL` 一致）
- User / Password：与 `PG_USER`、`PG_PASSWORD` 一致

确认存在表 **`test_entity`**（未加引号时 PG 存为小写表名，与 `@TableName("test_entity")` 一致）。

---

## 8. 配置项说明

### 8.1 `application-postgresql.yml` 核心片段

```yaml
spring:
  datasource:
    url: ${PG_URL:jdbc:postgresql://localhost:5432/testall}
    driver-class-name: org.postgresql.Driver
    username: ${PG_USER:postgres}
    password: ${PG_PASSWORD:}
  sql:
    init:
      mode: always
      schema-locations: classpath:schema-postgresql.sql

mybatis-plus:
  db-type: postgresql
```

### 8.2 `mybatis-plus.db-type` 可选值

| 配置值 | MyBatis-Plus 方言 |
|--------|-------------------|
| `mysql` | MySQL |
| `postgresql` / `postgres` / `pg` | PostgreSQL |
| `h2` | H2 |

**必须与当前 `spring.datasource` 实际数据库一致**，否则分页 SQL 可能错误。

---

## 9. 常见问题

### Q1：启动报错 `Connection refused` 或 `password authentication failed`

- 确认 PostgreSQL 服务已启动。
- 核对 `PG_URL`、库名、`PG_USER`、`PG_PASSWORD`。
- 用 psql 或图形工具先连通再启应用。

### Q2：报错 `relation "test_entity" does not exist`

- 确认 Profile 为 `postgresql` 且 `spring.sql.init` 已执行；或手工执行 `schema-postgresql.sql`。
- 检查是否连错库（URL 中的库名）。

### Q3：启动时 `MySQLBootCheckRunner` WARN

- PG Profile 下已开启 `mybatis.boot.check.enable=true`，要求表已存在。
- 若先启应用、后建表，会出现探活 WARN；建好表后重启即可。

### Q4：能否同时连 MySQL 和 PostgreSQL？

- 当前为 **单数据源 + Profile 切换**，一次启动只连一种库。
- 多数据源需另行配置 `@Primary` + 多 `DataSource`（未在本次改造范围）。

### Q5：密码能否写在配置文件里？

- 建议仅用环境变量或 IDEA Run Configuration，**勿将真实密码提交 Git**。

---

## 10. 架构关系（改造后）

```text
前端 / curl
    → TestAllModel (8080, Profile: postgresql)
        → web (Spring MVC)
        → mybatis (MyBatis-Plus + postgresql 驱动)
        → 本地 PostgreSQL (testall.test_entity)
```

---

## 11. 相关测试

| 测试类 | 说明 |
|--------|------|
| `MybatisPlusDbTypeResolverTest` | 方言解析单元测试 |
| `MyBatisPlusAutoConfigurationPostgreSqlTest` | PG 分页方言 Bean 测试 |
| `TestAllModelApplicationTest` | 默认 H2 上下文加载 |

执行：

```bash
mvn -pl mybatis,TestAllModel -am test -Dsurefire.failIfNoSpecifiedTests=false
```

> 连接真实 PG 的集成测试需本机数据库，日常 CI 可仅跑 H2 / 单元测试。

---

## 12. 变更文件清单（便于 Code Review）

```
mybatis/pom.xml
mybatis/src/main/java/com/lance/mybatis/MybatisPlusDbTypeResolver.java
mybatis/src/main/java/com/lance/mybatis/MyBatisPlusAutoConfiguration.java
mybatis/src/main/resources/bootstrap-mybatis.yml
mybatis/src/test/java/com/lance/mybatis/MybatisPlusDbTypeResolverTest.java
mybatis/src/test/java/com/lance/mybatis/MyBatisPlusAutoConfigurationPostgreSqlTest.java
TestAllModel/src/main/resources/application.yml
TestAllModel/src/main/resources/application-mysql.yml
TestAllModel/src/main/resources/application-postgresql.yml
TestAllModel/src/main/resources/schema-postgresql.sql
ai_docs/26052501-TestAllModel-PostgreSQL接入与使用说明.md
```
