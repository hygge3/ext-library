---
id: ext-lock
type: module-design
title: ext-lock — 声明式分布式锁
status: draft
parent: ext-library-architecture
depends-on: [ext-core, ext-redis, ext-postgres, ext-servlet]
---

# ext-lock — 声明式分布式锁

**职责**：在锁存储 SPI 之上包一层声明式入口：`@RedisLock`（`key` 支持 SpEL、`timeout` 持有时长、`waitTime` 等待时长）、`RedisLockAspect`（方法返回或抛异常都自动释放）、可替换的 `LockKeyGenerator`、未抢到锁时抛 `LockException`（携带锁 key，便于上层转成 409/重试提示）。

**后端**：锁原语由 `LockStore` SPI 提供，实现两种——`RedisLockStore`（默认，Lua 释放 + 线程内可重入，原语来自 [ext-redis](../ext-redis/SPEC.md)）与 `PostgresLockStore`（`pg_try_advisory_lock` / `pg_advisory_unlock`，会话级锁随连接释放，不依赖 [ext-redis](../ext-redis/SPEC.md)）。选型由 `ext.lock.store-type` 控制：`AUTO`（默认，按类路径优先 Redis）/ `REDIS` / `POSTGRES`。注解名继续叫 `@RedisLock`（对外契约不改），内部不绑定后端。`ext-redis` / `ext-postgres` 均为 `compileOnly`；一个后端都没引时装配阶段直接报错，不静默降级——锁失效比启动失败危险得多。

**边界**：本模块不实现锁协议、不做锁续约之外的调度；Web / servlet 能力经 [ext-servlet](../../ext-common/ext-servlet/SPEC.md)（`compileOnly`）取得——`LockKeyGenerator` 用 `ServletUtil.getRequestOrNull()` 拿当前请求，不自己写 `RequestContextHolder` 样板；`jakarta.servlet-api` 仅因 `LockKeyGenerator` 签名里出现 `HttpServletRequest` 而保留为 `compileOnly`（由消费方容器提供），非 Web 场景不引入。
