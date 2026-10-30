---
id: ext-cache
type: module-design
title: ext-cache — 多级缓存抽象
status: draft
parent: ext-library-architecture
depends-on: [ext-core, ext-json, ext-redis, ext-postgres]
---

# ext-cache — 多级缓存抽象

**职责**：统一的缓存抽象与四套策略——`CaffeineStrategy`（本地）、`RedisStrategy`、`PostgresStrategy`、`L2Strategy`（Caffeine + 分布式两级）；`@Cache` 注解 + 切面提供声明式缓存；`CacheProperties` 承载 `ext.cache.*`（`key-prefix`、`expire-time`、`cache-storage`、`l2-backend`、`caffeine.*`）。`CacheStrategy` 除 `get` / `put` / `evict` / `clear` 外提供原子的 `putIfAbsent(cacheName, key, value, expireTime)`，供 [ext-idempotent](../../ext-enhance/ext-idempotent/SPEC.md) 这类「在时间窗内只放行一次」的场景复用同一套后端。

**边界**：`ext-redis`、`ext-postgres`、`caffeine` 全部是 `compileOnly`。由此推出两件事：

- 单独引入 `ext-cache` 不会把 Redis / PostgreSQL / Caffeine 拖进消费方；要用哪个后端就由消费方自己引入对应依赖，`AUTO` 场景按类路径选实现（优先 Redis）。
- 本模块**不保证** `L2` / `CAFFEINE` 存储可用——没有 `caffeine` 时应显式配置 `cache-storage` 为分布式后端，否则启动即失败。

不预建任何表：PostgreSQL 缓存表由 [ext-postgres](../ext-postgres/SPEC.md) 初始化；与它之间无环，依赖方向是单向可选。
