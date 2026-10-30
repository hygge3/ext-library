---
id: ext-redis
type: module-design
title: ext-redis — Redis 接入与分布式锁原语
status: draft
parent: ext-library-architecture
depends-on: [ext-core, ext-json]
---

# ext-redis — Redis 接入与分布式锁原语

**职责**：Redis 接入层——`RedisTemplate` 增强（JSON 序列化值）、连接池配置（commons-pool2）；并向全库提供 `DistributedLock` 原语（Lua 脚本保证释放的原子性，线程内可重入）与发布订阅封装 `RedisPubSub`（`publish` / `subscribe` / `subscribePattern`，实现 [ext-core](../ext-core/SPEC.md) 的 `PubSub` SPI，把频道消息反序列化成指定类型，订阅句柄可取消）。

**边界**：依赖 `ext-core`、`ext-json`、`starter-data-redis`。不实现声明式锁语义（那是 [ext-lock](../ext-lock/SPEC.md)），不承担缓存抽象（那是 [ext-cache](../ext-cache/SPEC.md)）。

## 与 ext-postgres 的能力对比

同样是“分布式缓存 / 队列 / 广播 / 限流”，两边都能做，但成本不同。选型先看运维现实：本项目已经有 Redis 就用这里，只有一个库就用 [ext-postgres](../ext-postgres/SPEC.md)。

| 能力 | ext-redis | ext-postgres | 怎么选 |
| --- | --- | --- | --- |
| 缓存 | `RedisTemplate` / `RedisUtil`（TTL 原生、内存级延迟） | `pg_cache` UNLOGGED 表 + 定时清理 | 能引入 Redis 就用 Redis；否则 PG 的 UNLOGGED 表也能用，但读会打在库上 |
| 队列 | `QueueUtil`——list 阻塞出队 + zset 延迟队列 | `PostgresQueue`——`FOR UPDATE SKIP LOCKED`，带重试/统计/stuck 恢复 | 高吞吐、无事务参与选 Redis；需要与业务写入同一事务选 PG |
| 发布订阅 | `RedisPubSub`——`RedisUtil` 之上的封装，JSON 反序列化到指定类型 | `PostgresPubSub`——`LISTEN/NOTIFY` | 已有 Redis 就用它；只有 PG 时才需要广播，两边语义一致，换后端只换依赖 |
| 限流 | 由 [ext-ratelimit](../../ext-enhance/ext-ratelimit/SPEC.md) 的 `RedisRateLimitStore` 用 | 同上的 `PostgresRateLimitStore` | 跟缓存同一选型，不要一边 Redis 一边 PG |
| 分布式锁 | `DistributedLock`（Lua 释放、线程内可重入、看过期） | 无锁原语（`pg_try_advisory_lock` 属 [ext-lock](../ext-lock/SPEC.md) 的 `PostgresLockStore`） | 需要可重入/锁续约选 Redis；只要互斥用 PG 咨询锁 |
| 运维成本 | 多一个中间件、多一套监控/备份 | 零新增组件，复用现有库 | 这是 PG 方案存在的主要理由 |

不要两套同时做同一件事：缓存、限流、队列都只选一个后端，混用只会把“数据在哪”变成排障谜题。

**约定**：连接配置沿用 Spring 前缀 `spring.data.redis`，不新增 `ext.redis.*`。
