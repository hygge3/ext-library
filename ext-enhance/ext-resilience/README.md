# ext-resilience

> 限流与降级模块 - 令牌桶限流 + 熔断降级

## 简介

`ext-resilience` 通过注解和 AOP 提供两条保护能力：`@RateLimit` 用令牌桶做平滑限流（允许受控突发），`@Degrade` 用进程内熔断器在下游持续失败时切到降级方法。限流令牌存哪由 ext-cache 决定，本模块没有自己的存储选型。

## 快速开始

### Maven

```xml
<dependency>
    <groupId>ext.library</groupId>
    <artifactId>ext-resilience</artifactId>
</dependency>
```

### Gradle

```groovy
implementation("ext.library:ext-resilience")
```

## 依赖说明

| 依赖 | 说明 |
|------|------|
| ext-cache | 令牌桶的实际存储（本模块必备） |
| ext-redis | 可选，让 ext-cache 走 Redis 后端（多实例限流必须） |
| ext-postgres | 可选，让 ext-cache 走 PostgreSQL 后端 |

## 功能特性

- `@RateLimit`：令牌桶限流，`limit` 是桶容量（瞬时突发量），`window` 是补满周期，平均速率 = `limit / window`
- `@Degrade`：熔断降级，失败率过阈值后熔断一段时间，期间直接调降级方法；还可指定隔离线程池与超时
- `@Retry`：失败重试，指数退避 + 抖动，只允许用在幂等接口上
- 后端跟随 `ext.cache.storage`，本模块无 `store-type` 配置
- 都是注解式，业务代码不写调用样板

## 限流

```java
// 平均 100 次/分钟，允许瞬时 100 个突发
@RateLimit(key = "#request.getRemoteAddr()", limit = 100, window = 1, timeUnit = TimeUnit.MINUTES)
public Result<Void> sendCode(String phone) { ... }

// 一次扣 5 个令牌（批量接口按实际工作量计费）
@RateLimit(key = "#userId", limit = 100, permits = 5, window = 1)
public Result<Void> batchImport(long userId) { ... }
```

`limit` / `permits` / `window` 三个值直接决定桶的形状，不做换算：桶容量就是 `limit`，补满速率是 `limit / window`。超限抛 `RateLimitException`，带 `RateLimitResult`（currentCount / remaining / limit / resetIn）。

存储位置取决于 `ext.cache.cache-storage`：

| 后端 | 实现 |
|------|------|
| `MEMORY`（默认） | Caffeine 容器 + `synchronized`，**只在单实例内生效** |
| `REDIS` | Lua 脚本，一次往返完成读改写 |
| `POSTGRES` | `pg_token_bucket` 表的条件 `UPDATE … RETURNING` |
| `L2` | 分布式那一级（Redis 优先，其次 PostgreSQL） |

多实例部署必须选 `REDIS` / `POSTGRES` / `L2`，否则各实例各算各的桶。

## 降级

```java
@Degrade(minCalls = 20, failureRate = 50, window = 10, openDuration = 30, fallback = "fallbackRecommend")
public List<Item> recommend(long userId) { ... }

// 降级方法：同 bean、参数表与原方法一致（或空参），返回类型须兼容
List<Item> fallbackRecommend(long userId) {
    return List.of();
}
```

| 参数 | 含义 | 默认 |
|------|------|------|
| `minCalls` | 统计窗口内最少调用数，不到不熔断 | 10 |
| `failureRate` | 失败率百分比阈值（含） | 50 |
| `window` | 统计窗口秒数，过期清零 | 10 |
| `openDuration` | 熔断持续秒数，到期进半开 | 30 |
| `fallback` | 降级方法名，留空则抛 `CircuitOpenException` | `""` |

状态机：`CLOSED` → 达阈值 → `OPEN`（直接降级）→ 到期 → `HALF_OPEN`（只放一个探测请求）→ 探测成功回 `CLOSED`，失败重新 `OPEN`。

两条边界：

- **熔断是进程内的**，不做分布式熔断。一次网络抖动本来就会打穿所有实例，全局一致只会让故障传播更快。
- **降级只对熔断态生效**。原方法自己抛的异常照原样上抛，不吞进降级方法——把业务异常降级会掩盖真实故障。

## 隔离与超时

```java
// 核心接口走独立线程池，200ms 不出结果就放弃；核心接口禁止配兜底数据
@Degrade(executor = "coreExecutor", timeoutMillis = 200, critical = true)
public String coreQuery(long id) { ... }

// 边缘接口 2s 超时，挂了就给兜底
@Degrade(timeoutMillis = 2000, fallback = "cacheOnly")
public String edgeQuery(long id) { ... }
```

`executor` 填容器里 `Executor` 的 bean 名。线程池大小与拒绝策略由业务自己定，本模块不代管——业务知道自己的并发量与可接受的排队深度。`timeoutMillis` 只有配了 `executor` 才有意义：调用线程上没法中断一个同步调用。超时抛 `IsolationTimeoutException`，对外是 `504`。

`critical = true` 表示核心接口：**禁止**同时配 `fallback`，同时在首次调用时报配置错。主流程拿到过期的兜底数据比直接报错更危险，所以熔断时只快速失败，不假装成功。

## 重试

```java
@Idempotent          // 或 @RepeatSubmit；没有幂等注解会直接报配置错
@Retry(maxAttempts = 3, initialDelayMillis = 100)
public void pushOrder(long orderId) { ... }
```

退避 = `initialDelay * multiplier^(第几次) * [1 - jitter, 1]`，默认 100ms → 200ms，带 50% 抖动。重试会放大流量，同一时刻失败的一批调用必须错开，否则就是自己造一个尖峰。

**只给幂等接口用**：方法或所在类必须带 `@Idempotent` / `@RepeatSubmit`，否则首次调用直接报配置错。实在没法标注解的场景（第三方接口本身幂等）才能用 `assumeIdempotent = true` 绕过。`retryOn` 默认只重试 `RuntimeException`——`Error` 与受检异常不在其列。

## 注意事项

`RateLimitException` / `CircuitOpenException` / `IsolationTimeoutException` 都实现了 `HttpAware`，引了 `ext-mvc` 时自动落成 `429` / `503` / `504`，限流还带 `Retry-After`。未引 `ext-mvc` 的纯 servlet 场景，它们只是普通 `ExtException`，需在使用方自己的异常处理器里映射状态码。
