---
id: ext-postgres
type: module-design
title: ext-postgres — PostgreSQL 原生化能力
status: draft
parent: ext-library-architecture
depends-on: [ext-core, ext-json]
---

# ext-postgres — PostgreSQL 原生化能力

**职责**：用 PostgreSQL 原生特性替代 Redis 的四类能力，使只需一个关系库的项目也能有分布式缓存 / 队列 / 广播 / 限流：

- **缓存**：UNLOGGED 表（无 WAL，性能接近内存）——`PostgresCacheManager`。
- **队列**：`FOR UPDATE SKIP LOCKED` 无锁出队——`PostgresQueue`。
- **发布订阅**：`LISTEN/NOTIFY`——`PostgresPubSub`（实现 [ext-core](../../ext-common/ext-core/SPEC.md) 的 `PubSub` SPI）。
- **限流**：计数表 + 固定窗口——`PostgresRateLimiter`。

**建表与配置**：每种能力有自己的开关与表名，互不影响——`ext.postgres.cache.*`（`auto-init-schema`、`table-name`）、`ext.postgres.queue.*`（`auto-init-schema`、`table-name`）、`ext.postgres.ratelimit.*`（`auto-init-schema`、`table-name`），`auto-init-schema` 默认均为 `true`。`PostgresSchemaInitializer` 只为开着的那些能力建表（`CREATE TABLE IF NOT EXISTS` + `CREATE INDEX IF NOT EXISTS`，幂等），全关时不连库、不建表；`pubsub` 不需要表。表名可改（用于与已有表/前缀共存），DDL 语句统一引用配置的表名，不写死 `pg_*`。其余参数同样归到各自能力下：`cache.default-expire-time` / `cache.cleanup-interval`、`queue.max-attempts`、`ratelimit.window` / `ratelimit.default-limit`。

**边界**：依赖 `ext-core`、`ext-json`、`postgresql` 驱动、`starter-jdbc`；不依赖 `ext-redis`，与 [ext-cache](../ext-cache/SPEC.md) 之间只有「cache 单向可选引用 postgres」的关系，不成环。队列能力被 [ext-job](../ext-job/SPEC.md) 直接复用，不在别处另造调度原语。

**调用方自建表**：把对应 `auto-init-schema` 关掉、自己用迁移工具（Flyway / Liquibase）建表即可——关掉就是完全不建表，不会、也不应被下游模块逼着建；[ext-job](../ext-job/SPEC.md) 的队列同样只看这个开关，自己不做 DDL。

**约定**：配置前缀 `ext.postgres`。
