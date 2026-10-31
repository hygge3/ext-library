---
id: ext-ratelimit
type: module-design
title: ext-ratelimit — 接口限流
status: draft
parent: ext-library-architecture
depends-on: [ext-core, ext-redis, ext-postgres, ext-servlet]
---

# ext-ratelimit — 接口限流

**职责**：`@RateLimit`（`key` 支持 SpEL、`limit` 阈值、`window` 窗口）+ `RateLimitAspect`；`RateLimitKeyGenerator` 生成键；`RateLimitStore` 为 SPI，实现有 `MemoryRateLimitStore` / `RedisRateLimitStore` / `PostgresRateLimitStore`；触发限流抛 `RateLimitException`，携带限流结果供上层拼装 `Retry-After`。

**后端选择**：`ext.ratelimit.store-type` = `AUTO`（默认）/ `REDIS` / `POSTGRES` / `MEMORY`，`AUTO` 优先级 Redis > 内存。
