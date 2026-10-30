---
id: ext-idempotent
type: module-design
title: ext-idempotent — 接口幂等控制
status: draft
parent: ext-library-architecture
depends-on: [ext-core, ext-json, ext-cache]
---

# ext-idempotent — 接口幂等控制

**职责**：`@Idempotent` + `IdempotentAspect`：按幂等键在时间窗内只放行一次，后到请求被拒。`KeyGenerator` / `DefaultKeyGenerator` 生成键（可替换）。

**存储**：幂等键经 [ext-cache](../../ext-infra/ext-cache/SPEC.md) 的 `CacheStrategy.putIfAbsent(cacheName, key, value, expireTime)` 写入，因此自动继承缓存后端选择（Caffeine / Redis / PostgreSQL）与 `ext.cache.key-prefix`，本模块不再自带 `KeyStore` 与后端探测。

**约束**：`putIfAbsent` 必须是后端的原子操作（Caffeine `asMap().putIfAbsent`、Redis `SET NX PX`、PostgreSQL `INSERT … ON CONFLICT DO NOTHING`），不能用 `get` + `put` 拼出来——否则并发重复请求会双双放行，而这正是本模块唯一要保证的事。因此 `cache-storage` 不能配 `L2`：两级缓存的本地层与分布式层各自 putIfAbsent 会出现「本地没命中、远端已存在」的分裂判定，配成 `L2` 时启动即报错。

**边界**：依赖 `ext-cache` 承担存储；不引任何后端时只剩内存实现，多实例部署下幂等失效。不实现业务侧幂等语义（如订单去重、状态机校验），只负责“同一个键在时间窗内只放行一次”。缓存名由注解的 `cacheName` 决定（默认 `idempotent`）。
