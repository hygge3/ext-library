[根目录](../AGENTS.md) > **ext-infra**

# ext-infra - 基础设施层

> 提供 Redis、PostgreSQL、缓存、邮件等基础设施集成

## 层级职责

ext-infra 负责与外部基础设施的集成，为上层业务模块提供数据缓存、消息通知等能力。

## 子模块列表

| 模块 | 描述 | 主要依赖 |
|------|------|----------|
| ext-redis | Redis 操作封装，提供统一的 Redis 访问接口 | ext-core, ext-json, spring-data-redis |
| ext-lock | 分布式锁，注解式加锁（Redis / PostgreSQL 咨询锁） | ext-core；可选 ext-redis、ext-postgres |
| ext-postgres | PostgreSQL 功能模块（缓存、队列、发布订阅、限流） | ext-core, ext-json, postgresql |
| ext-cache | 多级缓存抽象 (Caffeine + Redis/PostgreSQL) | ext-redis(可选), ext-postgres(可选), caffeine |
| ext-mail | 邮件发送服务封装 | spring-boot-starter-mail |
| ext-job | 基于 PostgreSQL 队列的声明式任务投递与执行 | ext-postgres |
| ext-i18n | 国际化，资源文件与数据库双源翻译 | ext-core, ext-json, spring-boot-starter-jdbc(可选) |
| ext-oss | 对象存储，支持本地文件系统与 S3 协议 | ext-core；可选 AWS SDK v2（s3 + apache-client） |
| ext-metrics | 运行时指标采集、即时告警与 Web 大屏 | ext-core, ext-json；可选 spring-jdbc、HikariCP |

## 模块详情

### ext-redis

Redis 操作模块，提供：

- RedisTemplate 增强配置
- JSON 序列化支持
- 连接池管理 (commons-pool2)

**包结构**: `ext.library.redis`

**配置前缀**: `spring.data.redis`

#### 设计约束

**职责**：Redis 接入层——`RedisTemplate` 增强（JSON 序列化值）、连接池配置（commons-pool2）；并向全库提供 `DistributedLock` 原语（Lua 脚本保证释放的原子性，线程内可重入）与发布订阅封装 `RedisPubSub`（`publish` / `subscribe` / `subscribePattern`，实现 [ext-core](../ext-common/AGENTS.md#ext-core) 的 `PubSub` SPI，把频道消息反序列化成指定类型，订阅句柄可取消）。

**边界**：依赖 `ext-core`、`ext-json`、`starter-data-redis`。不实现声明式锁语义（那是 [ext-lock](#ext-lock)），不承担缓存抽象（那是 [ext-cache](#ext-cache)）。

##### 与 ext-postgres 的能力对比

同样是“分布式缓存 / 队列 / 广播 / 限流”，两边都能做，但成本不同。选型先看运维现实：本项目已经有 Redis 就用这里，只有一个库就用 [ext-postgres](#ext-postgres)。

| 能力 | ext-redis | ext-postgres | 怎么选 |
| --- | --- | --- | --- |
| 缓存 | `RedisTemplate` / `RedisUtil`（TTL 原生、内存级延迟） | `pg_cache` UNLOGGED 表 + 定时清理 | 能引入 Redis 就用 Redis；否则 PG 的 UNLOGGED 表也能用，但读会打在库上 |
| 队列 | `QueueUtil`——list 阻塞出队 + zset 延迟队列 | `PostgresQueue`——`FOR UPDATE SKIP LOCKED`，带重试/统计/stuck 恢复 | 高吞吐、无事务参与选 Redis；需要与业务写入同一事务选 PG |
| 发布订阅 | `RedisPubSub`——`RedisUtil` 之上的封装，JSON 反序列化到指定类型 | `PostgresPubSub`——`LISTEN/NOTIFY` | 已有 Redis 就用它；只有 PG 时才需要广播，两边语义一致，换后端只换依赖 |
| 限流 | `CacheStrategy#tryAcquire` 走 Lua 脚本 | 同一个 `tryAcquire` 走 `pg_token_bucket` 的条件 `UPDATE … RETURNING` | 跟缓存同一选型，不要一边 Redis 一边 PG |
| 分布式锁 | `DistributedLock`（Lua 释放、线程内可重入、看过期） | 无锁原语（`pg_try_advisory_lock` 属 [ext-lock](#ext-lock) 的 `PostgresLockStore`） | 需要可重入/锁续约选 Redis；只要互斥用 PG 咨询锁 |
| 运维成本 | 多一个中间件、多一套监控/备份 | 零新增组件，复用现有库 | 这是 PG 方案存在的主要理由 |

不要两套同时做同一件事：缓存、限流、队列都只选一个后端，混用只会把“数据在哪”变成排障谜题。

**约定**：连接配置沿用 Spring 前缀 `spring.data.redis`，不新增 `ext.redis.*`。

### ext-postgres

PostgreSQL 功能模块，使用 PostgreSQL 原生特性实现：

- **缓存**: UNLOGGED 表实现高性能缓存（无 WAL，性能接近内存）
- **队列**: `FOR UPDATE SKIP LOCKED` 实现无锁任务队列
- **发布订阅**: `LISTEN/NOTIFY` 原生消息机制
- **限流**: 令牌桶表（GCRA，单行存理论到达时间）
- **表结构初始化**: 各能力独立开关，按开关建表（`ext.postgres.cache.auto-init-schema` / `ext.postgres.queue.auto-init-schema` / `ext.postgres.bucket.auto-init-schema`，全关则不建表）

**包结构**: `ext.library.postgres`

**配置前缀**: `ext.postgres`

**核心类**:
- `PostgresCacheManager` - 缓存管理器
- `PostgresQueue` - 任务队列
- `PostgresPubSub` - 发布订阅管理器
- `PostgresSchemaInitializer` - 表结构自动初始化

**表结构**:
- `pg_cache` - 缓存表（UNLOGGED）
- `pg_jobs` - 任务队列表
- `pg_token_bucket` - 令牌桶表

#### 设计约束

**职责**：用 PostgreSQL 原生特性替代 Redis 的四类能力，使只需一个关系库的项目也能有分布式缓存 / 队列 / 广播 / 限流：

- **缓存**：UNLOGGED 表（无 WAL，性能接近内存）——`PostgresCacheManager`。
- **队列**：`FOR UPDATE SKIP LOCKED` 无锁出队——`PostgresQueue`。
- **发布订阅**：`LISTEN/NOTIFY`——`PostgresPubSub`（实现 [ext-core](../ext-common/AGENTS.md#ext-core) 的 `PubSub` SPI）。
- **限流**：令牌桶表，GCRA 等价实现——单行存「理论到达时间」，`UPDATE … WHERE bucket_key = ? AND tat <= ?` 条件更新，`RETURNING` 判断是否放行（供 [ext-cache](#ext-cache) 的 `tryAcquire`）。

**建表与配置**：每种能力有自己的开关与表名，互不影响——`ext.postgres.cache.*`（`auto-init-schema`、`table-name`）、`ext.postgres.queue.*`（`auto-init-schema`、`table-name`）、`ext.postgres.bucket.*`（`auto-init-schema`、`table-name`），`auto-init-schema` 默认均为 `true`。`PostgresSchemaInitializer` 只为开着的那些能力建表（`CREATE TABLE IF NOT EXISTS` + `CREATE INDEX IF NOT EXISTS`，幂等），全关时不连库、不建表；`pubsub` 不需要表。表名可改（用于与已有表/前缀共存），DDL 语句统一引用配置的表名，不写死 `pg_*`。`ext.postgres.schema` 指定三种能力共用的 schema（留空沿用连接的 `search_path`），带 schema 时 DDL 与运行时 SQL 一律走 `PostgresProperties#qualify(表名)`，各能力的 `table-name` 仍写裸表名；该值只接受普通标识符（`[A-Za-z_][A-Za-z0-9_]*`），因为它会被直接拼进 SQL。配了 schema 时初始化器会先 `CREATE SCHEMA IF NOT EXISTS`（需要建 schema 的权限，没有就自己先建好）。数据库本身不单独配置，由 `ext.postgres.datasource.url`（或应用主数据源）决定。其余参数同样归到各自能力下：`cache.default-expire-time` / `cache.cleanup-interval`、`queue.max-attempts`。

**边界**：依赖 `ext-core`、`ext-json`、`postgresql` 驱动、`starter-jdbc`；不依赖 `ext-redis`，与 [ext-cache](#ext-cache) 之间只有「cache 单向可选引用 postgres」的关系，不成环。队列能力被 [ext-job](#ext-job) 直接复用，不在别处另造调度原语。

**调用方自建表**：把对应 `auto-init-schema` 关掉、自己用迁移工具（Flyway / Liquibase）建表即可——关掉就是完全不建表，不会、也不应被下游模块逼着建；[ext-job](#ext-job) 的队列同样只看这个开关，自己不做 DDL。

**约定**：配置前缀 `ext.postgres`。

### ext-lock

分布式锁模块，在锁存储 SPI 之上包一层声明式入口：

- `@RedisLock` - 锁注解（`key` 支持 SpEL、`timeout` 持有时间、`waitTime` 等待时间）；注解名沿用旧契约，内部不绑定后端
- `RedisLockAspect` - 锁切面，正常返回与抛异常都自动释放
- `LockKeyGenerator` - 锁键生成器（SpEL），可替换为自定义实现
- `LockStore` - 锁存储 SPI；实现 `MemoryLockStore`（单实例，进程内）、`RedisLockStore`（ext-redis 的 `DistributedLock`，自带 TTL）与 `PostgresLockStore`（`pg_try_advisory_lock` 会话级锁）
- `LockException` - 未抢到锁时抛出，携带锁 key

**包结构**: `ext.library.lock`

后端跟随 `CacheStrategy#backendType()`（即 `ext.cache.storage` 解析结果），本模块没有自己的选型键；`ext-redis` / `ext-postgres` 为 `compileOnly`。

#### 设计约束

**职责**：在锁存储 SPI 之上包一层声明式入口：`@RedisLock`（`key` 支持 SpEL、`timeout` 持有时长、`waitTime` 等待时长）、`RedisLockAspect`（方法返回或抛异常都自动释放）、可替换的 `LockKeyGenerator`、未抢到锁时抛 `LockException`（携带锁 key，便于上层转成 409/重试提示）。

**后端**：锁原语由 `LockStore` SPI 提供，实现三种——`MemoryLockStore`（单实例，进程内互斥，`backendType()=MEMORY` 时装配）、`RedisLockStore`（Lua 释放 + 线程内可重入，原语来自 [ext-redis](#ext-redis)）与 `PostgresLockStore`（`pg_try_advisory_lock` / `pg_advisory_unlock`，会话级锁随连接释放，不依赖 [ext-redis](#ext-redis)）。选型不单独配置：`LockAutoConfiguration` 读 `CacheStrategy#backendType()`，与缓存共用 `ext.cache.storage` 一处口径。注解名继续叫 `@RedisLock`（对外契约不改），内部不绑定后端。`MEMORY` 只对单实例有效，多实例必须选 `REDIS` 或 `POSTGRES`。`ext-redis` / `ext-postgres` 均为 `compileOnly`，缺依赖时按类加载失败报错，不静默降级——锁失效比启动失败危险得多。

**边界**：本模块不实现锁协议、不做锁续约之外的调度；Web / servlet 能力经 [ext-servlet](../ext-common/AGENTS.md#ext-servlet)（`compileOnly`）取得——`LockKeyGenerator` 用 `ServletUtil.getRequestOrNull()` 拿当前请求，不自己写 `RequestContextHolder` 样板；`jakarta.servlet-api` 仅因 `LockKeyGenerator` 签名里出现 `HttpServletRequest` 而保留为 `compileOnly`（由消费方容器提供），非 Web 场景不引入。

### ext-cache

多级缓存抽象模块，支持：

- **本地缓存**: Caffeine 高性能本地缓存
- **分布式缓存**: Redis 或 PostgreSQL（按类路径自动选择）
- **二级缓存**: Caffeine + 分布式缓存
- **缓存策略**: `CaffeineStrategy`, `RedisStrategy`, `PostgresStrategy`, `L2Strategy`
- **注解支持**: `@Cache` 缓存注解

**包结构**: `ext.library.cache`

**配置前缀**: `ext.cache`

**核心类**:
- `CacheAutoConfiguration` - 自动配置类
- `CacheProperties` - 缓存配置属性
- `Cache` - 缓存注解
- `CacheStorage` - 缓存存储方式枚举，即 `ext.cache.cache-storage` 的取值

**配置示例**:
```yaml
ext:
  cache:
    cache-storage: L2        # AUTO/REDIS/POSTGRES/CAFFEINE/L2，唯一的选型入口
    expire-jitter: 0.1       # 过期抖动比例，0 关闭
    load-timeout: 3s         # 单飞等待同 key 首次回源的上限，超时自己回源
```

#### 设计约束

**职责**：统一的缓存抽象与四套策略——`CaffeineStrategy`（本地）、`RedisStrategy`、`PostgresStrategy`、`L2Strategy`（Caffeine + 分布式两级）；`@Cache` 注解 + 切面提供声明式缓存；`CacheProperties` 承载 `ext.cache.*`（`key-prefix`、`expire-time`、`expire-jitter`、`load-timeout`、`cache-storage`、`caffeine.*`）。`CacheStrategy` 除 `get` / `put` / `evict` / `clear` 外提供原子的 `putIfAbsent(cacheName, key, value, expireTime)` 、`increment(cacheName, key, delta, window)` 与 `tryAcquire(cacheName, key, permits, limit, window)`，分别供 [ext-idempotent](../ext-enhance/AGENTS.md#ext-idempotent)（幂等键）、[ext-lock](AGENTS.md#ext-lock)（计数器）与 [ext-resilience](../ext-enhance/AGENTS.md#ext-resilience)（令牌桶限流）复用同一套后端。

**穿透 / 击穿 / 雪崩**（三个问题各有对应开关，都不用业务代码自己写）：

- **穿透**（查不到还反复查）：`@Cache(cacheNull = true, nullTimeout = 30)` 把空结果写成短 TTL 的负缓存。负缓存存在 `key + ":null"` 这个独立键上，不与被缓存的值混用同一个 key——混用会让返回 `Boolean` 的方法把负缓存读成 `false`。`CacheType.DELETE` 会连它一起删。只开在「查不到是常态」的方法上（按不存在的 ID 查详情、按手机号查用户），负缓存期间真实数据写进来了也读不到，只能靠 `nullTimeout` 自然过期。
- **击穿**（热点 key 过期瞬间全量回源）：`CacheAspect` 对同一个 `cacheName:key` 做单飞（single-flight），并发的第一个请求回源，其余等它的结果，回源异常原样抛给等待方。只合并同一实例内的请求，多实例要靠分布式锁；同一线程重入同一个 key 时不做合并（等自己就是死锁）。
- **雪崩**（一批 key 同时过期）：所有策略的 `put` 写 TTL 前先过 `CacheStrategy#withJitter`，按 `ext.cache.expire-jitter`（默认 `0.1`）在 `[ttl × 0.9, ttl]` 内随机，只缩短不延长。`L2` 不自己抖，两级各抖一次，因此本地副本与分布式层的 TTL 最大偏差为 jitter 的两倍；回填路径按 `remainingTtl` 对齐。

**选型只有一个键**：`ext.cache.cache-storage`。`L2` 的第二级后端按类路径自动选择（Redis 优先，其次 PostgreSQL），不另设 `l2-backend`；其他模块（[ext-lock](#ext-lock)、[ext-resilience](../ext-enhance/AGENTS.md#ext-resilience)）不设自己的 `store-type`，一律读 `CacheStrategy#backendType()`。同一个中间件配两遍，部署时必然漂移。

**持久化口径**：凡是需要跨重启 / 跨实例存活的键值数据，一律用 `CacheStrategy`，不在业务模块里直接注入 `RedisUtil` / `PostgresUtil`，也不自建 `*-storage` 选型开关——后端选型只有 `ext.cache.cache-storage` 一处。已遵循：[ext-idempotent](../ext-enhance/AGENTS.md#ext-idempotent)（幂等键）、[ext-security](../ext-security/AGENTS.md)（会话/凭据）、ext-api-crypto（nonce）、[ext-resilience](../ext-enhance/AGENTS.md#ext-resilience)（限流令牌）。

例外只有 [ext-lock](#ext-lock)：它要的不是 KV 原语，而是 `SET NX PX` + Lua 校验值后删除 + 续期（ext-redis 的 `DistributedLock`），`CacheStrategy` 给不了。它的后端仍从 `CacheStrategy#backendType()` 派生，只是不把锁写入缓存。

**边界**：`ext-redis`、`ext-postgres`、`caffeine` 全部是 `compileOnly`。由此推出两件事：

- 单独引入 `ext-cache` 不会把 Redis / PostgreSQL / Caffeine 拖进消费方；要用哪个后端就由消费方自己引入对应依赖，`AUTO` 场景按类路径选实现，优先级 Redis > PostgreSQL > 本地内存。
- 本模块**不保证** `L2` / `CAFFEINE` 存储可用——没有 `caffeine` 时应显式配置 `cache-storage` 为分布式后端，否则启动即失败。

不预建任何表：PostgreSQL 缓存表由 [ext-postgres](#ext-postgres) 初始化；与它之间无环，依赖方向是单向可选。

### ext-mail

邮件发送模块，提供：

- **邮件发送**: `MailSender`, `MailSenderImpl`
- **邮件模型**: `MailDetails`
- **事件支持**: `MailSendEvent` 邮件发送事件

**包结构**: `ext.library.mail`

**配置前缀**: `spring.mail`

#### 设计约束

**职责**：对 Spring Mail 的收敛封装——`MailSender` / `MailSenderImpl` 发送、`MailDetails` 描述邮件、`MailSendResult` 返回结果、`MailSendEvent` 发布发送事件（便于监听失败/重试）。`MailProperties` 承载 `ext.mail.*`：`retry-count`（失败重试次数，默认 `3`，即首次之外最多再试 3 次）、`retry-interval`（重试间隔，默认 `1s`），重试复用 `ext-tool` 的 `RetryUtils.doWithRetry`；最终的成败结果都通过 `MailSendEvent` 发出。

**边界**：只依赖 `ext-tool` 与 `starter-mail`；账号与服务器配置完全沿用 Spring 前缀 `spring.mail`，除重试参数外不新增配置项，也不管理发送队列或模板。

### ext-job

任务调度模块，直接复用 ext-postgres 的 `PostgresQueue`，不引入独立调度框架：

- **处理器**: `JobHandler<T>` 声明负载类型、队列名与处理逻辑，`handle` 抛异常即视为本次尝试失败；一个队列只能有一个处理器，重名在 `JobDispatcher` 构造时直接抛异常（否则后注册者会顶掉前者、负载被静默吞掉）
- **投递器**: `JobDispatcher` 按队列投递任务，支持延迟投递、重试次数上限与队列统计
- **执行器**: `JobExecutor` 每轮按队列出队处理，由 `@Scheduled` 轮询触发
- **定时任务**: `@CronJob` 标注在 `JobHandler` 实现类或 `@Bean` 工厂方法上，`CronJobScheduler` 按 cron 表达式到点投递；只负责触发投递，执行仍走轮询执行器
- **运维接口**: `countPending`/`stats`/`cleanup`/`cleanupFailed`/`resetStuck`/`queues`，用于积压观测、死信清理与卡死任务回收

**包结构**: `ext.library.job`

**配置前缀**: `ext.job`

**核心类**:
- `JobHandler` - 处理器接口（含 `of(Class, Consumer)` 便捷工厂）
- `JobDispatcher` - 任务投递器
- `JobExecutor` - 轮询执行器
- `CronJob` - 定时任务注解（`cron` / `zone`）
- `CronJobScheduler` - 定时任务注册器
- `JobProperties` - 任务执行配置
- `JobAutoConfiguration` - 自动配置类

**使用示例**:
```java
@Component
public class OrderJobHandler implements JobHandler<Order> {
    @Override
    public Class<Order> payloadType() { return Order.class; }

    @Override
    public void handle(Order order) { orderService.process(order); }
}

// 投递
long jobId = jobDispatcher.dispatch("default", new Order(1L));
```

**定时任务**:
```java
@CronJob(cron = "0 0 2 * * *")
@Component
public class ReportJobHandler implements JobHandler<ReportPayload> {
    // handle 里不做业务，只调 jobDispatcher.dispatch(...) 带着真实参数投递
}
```

> `@CronJob` 不自己执行业务：到点只往该处理器的队列投一个**默认构造**的负载（`payloadType()` 必须能无参构造），真实参数在 `handle` 里补齐后再投一次。这样定时触发与手动触发、失败重试走的是同一条队列与同一套重试口径。

**配置示例**:
```yaml
ext:
  job:
    enabled: true                 # 是否启用任务轮询
    batch-size: 10                # 每轮每队列最大出队条数
    poll-interval-millis: 1000    # 轮询间隔
    stuck-timeout: 10m            # 卡死任务回收阈值
    completed-retention: 1d       # 已完成任务保留时长
    recover-on-startup: true      # 启动时回收卡死任务
    cleanup-on-startup: true      # 启动时清理过期完成任务
```

> 未注册任何 `JobHandler` 时只装配投递器与执行器，不启动轮询。

#### 设计约束

**职责**：基于 [ext-postgres](#ext-postgres) 的 `PostgresQueue` 提供声明式任务投递与执行，不引入独立调度框架：`JobHandler<T>`（声明负载类型、队列名与处理逻辑，`handle` 抛异常即视为本次尝试失败）、`JobDispatcher`（投递、延迟投递、重试次数上限、队列统计）、`JobExecutor`（`@Scheduled` 轮询出队）、运维能力 `countPending` / `stats` / `cleanup` / `resetStuck` / `queues`。

**定时触发**：`@CronJob(cron, zone)` + `CronJobScheduler` 复用 Spring 的 `TaskScheduler` 与 `CronTrigger`，不引入 Quartz。到点只做投递，执行、重试、卡死回收全部复用轮询执行器——两套触发共享一条队列，失败处理口径只有一份。负载类型必须能无参构造（启动时就实例化一次，类型不可实例化在启动阶段就报错）；`zone` 留空则用服务器默认时区。

**决策**：队列直接用 PostgreSQL 的 `FOR UPDATE SKIP LOCKED`，因此不需要额外的消息中间件——这也是本库"用 PG 替代 Redis"路线的一部分。

**调度器**：`@EnableScheduling` 声明在本模块的自动配置上，Spring Boot 的 `TaskSchedulingAutoConfiguration` 判断时机在其后、拿不到调度注解处理器，因此 `TaskScheduler` 由本模块自建（`@ConditionalOnMissingBean`，2 线程，前缀 `ext-job-`），消费方自备调度器时退让；轮询间隔读 `${ext.job.poll-interval-millis:500}`，不引用 `JobProperties` Bean（`@EnableConfigurationProperties` 注册的 Bean 名不是 `jobProperties`）。

**边界与装配**：未注册任何 `JobHandler` 时只装配投递器与执行器，不启动轮询；配置前缀 `ext.job`（`enabled`、`batch-size`、`poll-interval-millis`、`stuck-timeout`、`completed-retention`、`failed-retention`、`cleanup-interval-millis`、`recover-on-startup`、`cleanup-on-startup`）。运行期由 `@Scheduled(cleanup-interval-millis)` 周期清理：COMPLETED 按 `completed_at`、FAILED（重试耗尽的死信）按 `created_at` 各自套保留时长，不清理就会无限堆积。

**建表**：队列表由 [ext-postgres](#ext-postgres) 的 `ext.postgres.queue.auto-init-schema` 决定是否自动创建（默认 `true`，表名 `ext.postgres.queue.table-name`，默认 `pg_jobs`）。关掉它就真的不建表——`ext-job` 自己不做任何 DDL，也不会在启动时偷偷把表补上；这时消费方必须先用迁移工具建好同名表（列结构见 [ext-postgres](#ext-postgres)），否则 `ext-job` 不可用。

### ext-i18n

国际化模块，把翻译文案拆成多个来源，查不到就回退到下一个：

- **管理器**: `I18nMessageManager` 按 `Locale` 顺序查询各翻译源，支持占位符参数、`find` 返回 `Optional`、`contains` 判定与 `clear` 清缓存
- **来源 SPI**: `TranslationSource` 接口（`load(Locale)` / `name()` / `order()`），`order()` 越小越先查询
- **资源文件源**: `ResourceBundleTranslationSource` 读 classpath 下的 `ResourceBundle`
- **数据库源**: `JdbcTranslationSource` 读翻译表，查询走语言降级链 `zh-CN` → `zh` → `ROOT`，按降级顺序合并
- **缓存**: 按语言缓存翻译表，`ext.i18n.cache-ttl` 到期重载，便于翻译变更后自动生效
- **Spring 消息源**: `I18nMessageSource` 实现 Spring 的 `MessageSource`，容器里没有 `messageSource` bean 时自动注册为它。模板的 `#{...}`、Bean Validation 消息因此能用同一套翻译（含数据库覆盖）；指定语言缺键回落 `default-locale`，都没有时返回键本身，而不是 `??code_locale??`。消费方配了 `spring.messages.basename` 时以消费方为准

**包结构**: `ext.library.i18n`

**配置前缀**: `ext.i18n`

**核心类**:
- `I18nMessageManager` - 翻译管理器
- `TranslationSource` - 翻译来源 SPI
- `ResourceBundleTranslationSource` / `JdbcTranslationSource` - 两个翻译源
- `I18nMessageSource` - Spring `MessageSource` 适配（模板与校验消息用）
- `I18nProperties` / `I18nAutoConfiguration` - 配置属性与自动配置

**使用示例**:
```java
String msg = i18nMessageManager.get("user.notfound");                 // 默认语言
String en = i18nMessageManager.get("user.notfound", Locale.US);
String hi = i18nMessageManager.get("order.count", Locale.US, 3);       // 带参数
```

**配置示例**:
```yaml
ext:
  i18n:
    enabled: true
    default-locale: zh-CN
    resource-bundle-base-name: i18n/messages
    jdbc-enabled: true            # 需要容器中存在 JdbcClient，未引入时静默关闭
    table-name: ext_i18n_message
    schema-init: true             # 启动时自动建表
    cache-ttl: 5m
```

> 翻译表结构：`ext_i18n_message(id, message_key, locale, message)`，`(message_key, locale)` 唯一。

#### 设计约束

**职责**：把翻译文案拆成多个来源，查不到就按 `order()` 回退到下一个：`I18nMessageManager`（`get` / `find` 返回 `Optional` / `contains` / `clear`，支持占位符参数）、`TranslationSource` SPI、`ResourceBundleTranslationSource`（classpath 资源文件）、`JdbcTranslationSource`（翻译表，语言降级链 `zh-CN` → `zh` → `ROOT`，按降级顺序合并）。

**缓存**：按语言缓存翻译表，`ext.i18n.cache-ttl`（默认 5m）到期重载——这是翻译改库后自动生效的机制，不要改成只在启动时加载。

**边界**：`starter-jdbc` 为 `compileOnly`，容器里没有 `JdbcClient` 时数据库源静默关闭（`jdbc-enabled` 自动失效），只有资源文件源生效。表结构 `ext_i18n_message(id, message_key, locale, message)`，`(message_key, locale)` 唯一，可由 `schema-init` 自动建表。

### ext-oss

对象存储模块，本地文件系统与 S3 协议各一份实现，业务方只依赖门面类：

- **门面**: `OssTemplate` - put/putBytes/putFile/copy/move/get/getString/getBytes/stat/delete/exists/list/presign/presignDownload/presignPut/thumbnail/generateKey
- **SPI**: `ObjectStorage` 接口，两种后端各实现一份；`copy` / `move` 带跨桶重载，无桶概念的实现抛 `UnsupportedOperationException`
- **本地实现**: `LocalObjectStorage` 写本地目录，可配置对外访问前缀
- **S3 实现**: `S3ObjectStorage` 基于 AWS SDK for Java v2（`S3Client` + `S3Presigner`），超过 `part-size` 走分片上传，复制走服务端 `CopyObject`
- **返回值**: `OssObject`（bucket/key/contentType/size/content 流，实现 `Closeable`）、`ObjectMeta`（stat 结果，不含内容）、`ObjectItem`（列表项）
- **实现选择**: 按 `ext.oss.type` 选 `local` / `s3`；厂商类型只出现在自动配置类方法体内部，且各方 SDK 都是 `compileOnly`，容器里没有对应 SDK 时其他模式照样能启动；S3 模式还要额外引 `software.amazon.awssdk:apache-client`（或 `url-connection-client`），`s3` 本身不带 HTTP 客户端实现
- **不在范围**: 跨实现复制（local → s3 属于数据迁移）、CDN 回源、服务端图片处理（缩略图在应用进程内算）

**预签名**：`presign` 给下载直链，`presignPut` 给上传直链（客户端直接 PUT，数据不经过应用），`presignDownload(key, filename)` 在响应上带 `Content-Disposition: attachment; filename`（非 ASCII 走 RFC 5987 的 `filename*`）。本地实现没有签名机制，`presignDownload` 退化为 `presign`（忽略文件名），`presignPut` 直接拒绝。

**缩略图**：`thumbnail(key)` / `thumbnail(key, maxEdge)` / `thumbnail(key, maxEdge, format)` 从对象读图、等比缩到长边 `maxEdge`、按格式编码，整条管线在 `ImageUtil.thumbnail(byte[], int, String, float)` 里。解码走 ImageIO（jpg/png/gif/bmp/tiff/webp 皆有插件），编码按格式查 `ImageUtil` 的编码器注册表：内置 `webp`（`com.github.usefulness:webp-imageio`，带各平台原生库，JDK 自身没有 webp 编码器），其余格式按 ImageIO 可写能力自动注册。业务方注册一个 `ThumbnailEncoder` Bean 就覆盖同格式内置实现（如自研 avif 编码），启动时由 `OssAutoConfiguration` 装入注册表。尺寸默认长边 320px、质量 0.8，只缩不放。

**图片能力**：分两层。底层读写与格式探测在 `ext-common/ext-tool` 的 `ext.library.tool.util.ImageIOUtil`——读（File/URL/InputStream/byte[]/String）、写（File/OutputStream/字节数组）、`isReadable` / `isWritable` / `readableFormats` / `writableFormats`，外加两处守卫（认不出格式返回 null、找不到 writer 返回 false 都转成带可用格式列表的异常）。上层的 `ext.library.oss.util.ImageUtil` 只管策略：缩略图、等比缩放、质量控制写出、编码器注册表，不重复实现读写。

**格式边界**：透明通道走 ImageIO 原样保留（含 webp 的 alpha，缩略图降采样后仍保留）。动图只能拿到首帧——ImageIO 没有逐帧 API，`webp-imageio` 只暴露静态编解码，animated webp 与 gif 动画都只解首帧，写出的永远是静态图。

**包结构**: `ext.library.oss`（`config` / `util`）

**配置前缀**: `ext.oss`

**核心类**:
- `OssTemplate` - 对象存储门面
- `ObjectStorage` - 存储 SPI
- `LocalObjectStorage` / `S3ObjectStorage` - 两种实现
- `OssObject` / `ObjectMeta` / `ObjectItem` - 返回模型
- `OssProperties` / `OssAutoConfiguration` - 配置属性与自动配置
- `util.ImageUtil` - 图片读写、缩放、格式探测、缩略图编码器注册表
- `util.ThumbnailEncoder` / `util.ImageIoThumbnailEncoder` - 缩略图编码 SPI 与 ImageIO 实现

**使用示例**:
```java
String key = ossTemplate.generateKey("avatar", "logo.png");
String url = ossTemplate.put(key, Files.newInputStream(file), size, "image/png");

ossTemplate.copy(key, "backup/" + key);                  // 服务端复制，数据不过进程
ossTemplate.move(key, "archive/" + key);                 // copy + delete
String url2 = ossTemplate.presign(key);                  // 预签名下载链接
String download = ossTemplate.presignDownload(key, "报表.xlsx");  // 带另存为文件名
String upload = ossTemplate.presignPut("upload/a.png");  // 客户端直接 PUT
byte[] cover = ossTemplate.thumbnail(key);               // 长边 320 的 webp
byte[] png = ossTemplate.thumbnail(key, 160, "png");      // 指定尺寸与格式
ObjectMeta meta = ossTemplate.stat(key);
byte[] bytes = ossTemplate.getBytes(key);
```

**配置示例**:
```yaml
ext:
  oss:
    enabled: true
    type: LOCAL                    # LOCAL / S3
    path: ./data/oss               # 本地存储根目录
    url-prefix: ""                 # 本地存储对外访问前缀
    bucket: ext-library
    # 以下为 type=s3 时必填
    endpoint: https://s3.example.com
    access-key: ${OSS_ACCESS_KEY}
    secret-key: ${OSS_SECRET_KEY}
    region: us-east-1
    auto-create-bucket: true
    part-size: 10485760            # 分片大小，默认 10MB
    presign-expiry: 1h             # 预签名链接有效期
    path-style-access: true        # 自建端点几乎都要；AWS 官方端点关掉
    thumbnail-max-edge: 320        # 缩略图长边上限，只缩不放
    thumbnail-format: webp         # 缩略图输出格式
    thumbnail-quality: 0.8         # 缩略图质量，仅对有损格式生效
    chunked-encoding: true         # 旧网关不认 aws-chunked 时关掉
```

#### 设计约束

**职责**：`OssTemplate` 作为唯一门面（`put` / `putBytes` / `putFile` / `copy` / `move` / `get` / `getString` / `getBytes` / `stat` / `delete` / `exists` / `list` / `presign` / `presignPut` / `generateKey`），背后是可替换的 `ObjectStorage` SPI，两份实现：`LocalObjectStorage`（写本地目录，可配对外访问前缀）与 `S3ObjectStorage`（AWS SDK for Java v2，分片上传 + 预签名链接 + 服务端复制）。返回模型 `OssObject`（`Closeable`，含 bucket/key/contentType/size/流）、`ObjectMeta`（`stat` 结果：size/contentType/lastModified，不含内容）与 `ObjectItem`（列表项）。

**装配约束**：按 `ext.oss.type` 选 `local` / `s3`。各实现的厂商类型（AWS SDK 的 `S3Client` / `S3Presigner`）只出现在自动配置类的方法体内部，且各自是 `compileOnly`，因此**容器里没有对应 SDK 时其他模式仍能启动**——新增存储实现必须沿用这个约束，不能把厂商类型泄进门面或 SPI 签名。S3 模式除 `software.amazon.awssdk:s3` 外还必须引一个 HTTP 客户端实现（`apache-client` 或 `url-connection-client`）——`s3` 只带 `http-client-spi` 接口，SDK 找不到实现会在建 `S3Client` 时抛 `SdkClientException`。

**S3 模式**：`ext.oss.type=s3`，配置 `ext.oss.*` 的 `endpoint` / `access-key` / `secret-key` / `region` / `bucket` / `auto-create-bucket` / `part-size`（默认 10MB）/ `presign-expiry`，外加两个 S3 兼容性开关：`path-style-access`（默认 `true`——MinIO、Ceph、R2 这类自建端点几乎都要求 path-style，AWS 官方端点要关掉）与 `chunked-encoding`（默认 `true`，个别旧网关不认 `aws-chunked` 时要关）。

**复制语义**：`copy(sourceKey, targetKey)` 与跨桶重载 `copy(sourceBucket, sourceKey, targetBucket, targetKey)` 走服务端复制（`CopyObject`），数据不经过应用进程；`move` 是 `copy` + `delete` 的组合，因为 S3 协议里没有原子 rename。本地实现用 `Files.copy` / `Files.move`。跨**实现**复制（如 local → s3）不做：那是数据迁移工具的事，门面不为它引入双客户端装配。

**边界**：不实现 CDN 回源、图片处理、配额管理，也不做跨实现复制；对象生命周期（过期清理）由消费方负责。

### ext-metrics

运行时指标模块，**纯自建**，不依赖 Micrometer / Actuator / Prometheus 客户端。提供：

- `MetricsFilter`：Servlet 过滤器，统计 QPS、耗时分桶与 P99、错误率、错误码分布、端点明细
- `MetricsRegistry`：指标容器，固定窗口计数 + 延迟直方图 + 慢 SQL/Pool 快照
- `AlertEvaluator` + `AlertNotifier`：定时评估，超阈值即时通知，同对象同指标冷却期内只报一次
- `SqlTimingBeanPostProcessor`：包住容器里的 `JdbcTemplate`，采慢 SQL（`JdbcClient` 由它构建，一并覆盖）
- `PoolSampler`：反射读 `ThreadPoolTaskExecutor` / `ThreadPoolExecutor` 与 `DataSource`（Hikari / Druid / Tomcat JDBC）水位
- `DashboardServlet`：单文件 HTML 大屏，轮询 `/snapshot` 的 JSON

#### 设计约束

**职责**：采集口径只有一个出口——`MetricsRecorder`（定义在 `ext-common/ext-core` 的 `ext/library/core/metrics/MetricsRecorder.java`）。ext-metrics 实现它并注册为 Bean；限流、幂等、异常处理、SQL 采集都通过它上报，模块之间不互相依赖。

**埋点方式**：引入依赖即生效，业务代码不改。`MetricsFilter` 由自动配置注册；限流拒绝、幂等命中、业务错误码、慢 SQL 分别由 `RateLimitAspect` / `IdempotentAspect` / `GlobalExceptionHandler` / `SqlTimingBeanPostProcessor` 上报。未引入 ext-metrics 时 `MetricsRecorder` 没有实现 Bean，各模块用 `ObjectProvider` 取到 null 就跳过，不加载类也不报错。

**窗口与分位**：计数是**固定窗口**（`ext.metrics.window`，默认 60s），窗口切换时计数清零；P99 用 20 个延迟桶的近似值（桶上界），不是精确分位。这是有意的取舍——精确分位需要保存全部样本，长跑进程会吃内存。

**告警**：默认错误率 > 1%、P99 > 500ms 触发，且样本数低于 `alert.min-requests`（默认 20）不评估——低流量接口否则天天误报。评估间隔 10s、冷却 5min。通知渠道由 `AlertNotifier` SPI 决定，默认只打 WARN 日志，接入钉钉/企业微信/邮件只需提供自己的 Bean。

**大屏**：`DashboardServlet` 默认注册在 `/metrics`（页面）与 `/metrics/snapshot`（JSON），符合「引入依赖即接入」。页面**没有鉴权**，对外暴露前必须自己套 Spring Security 或网关规则，或者置 `ext.metrics.dashboard.enabled=false` 关掉。

**边界**：不做分布式聚合（每个实例各看各的，多实例需在网关或 Prometheus 侧汇总）、不做持久化（重启清零）、不做链路追踪（那是 ext-trace 的事）、不覆盖 MyBatis（`SqlSessionTemplate` 不在 `JdbcTemplate` 代理内）。

## 依赖关系

```
ext-common (ext-core, ext-json, ext-servlet)
    |
    +---> ext-redis (独立)
    |         |
    |         +---> ext-lock
    |
    +---> ext-postgres (独立)
    |         |
    |         +---> ext-job
    |
    |
    +---> ext-cache
              |
              +---> ext-redis (可选，用于 Redis 缓存)
              +---> ext-postgres (可选，用于 PostgreSQL 缓存)
              +---> caffeine (必需，用于本地缓存)

ext-mail (独立，仅依赖 spring-boot-starter-mail)

ext-core + ext-json
    |
    +---> ext-i18n (+ spring-boot-starter-jdbc 可选)

ext-core (+ software.amazon.awssdk:s3，按需 compileOnly)
    |
    +---> ext-oss

ext-core + ext-json (+ spring-jdbc、HikariCP 按需 compileOnly)
    |
    +---> ext-metrics
```

**注意**: ext-postgres 和 ext-cache 之间没有循环依赖。ext-cache 通过可选依赖引用 ext-postgres。

## 相关文件

- `ext-redis/src/main/java/ext/library/redis/` - Redis 相关源码
- `ext-lock/src/main/java/ext/library/lock/` - 分布式锁源码
- `ext-postgres/src/main/java/ext/library/postgres/` - PostgreSQL 相关源码
- `ext-cache/src/main/java/ext/library/cache/` - 缓存相关源码
- `ext-mail/src/main/java/ext/library/mail/` - 邮件相关源码
- `ext-job/src/main/java/ext/library/job/` - 任务调度源码
- `ext-i18n/src/main/java/ext/library/i18n/` - 国际化源码
- `ext-oss/src/main/java/ext/library/oss/` - 对象存储源码
- `ext-metrics/src/main/java/ext/library/metrics/` - 指标采集、告警与大屏源码

## 变更记录

| 日期 | 变更内容 |
|------|----------|
| 2026-01-19 | 初始化 AGENTS.md 文档 |
| 2026-01-22 | 添加 ext-postgres 模块 |
| 2026-01-26 | ext-cache 支持 PostgreSQL 缓存后端，解除循环依赖 |
| 2026-10-10 | 新增 ext-job 模块（基于 PostgreSQL 队列的任务调度）；新增 ext-jdbc 模块（JdbcClient 增强与轻量 DAO） |
| 2026-10-11 | 收编原 ext-misc 层的 ext-i18n / ext-oss，ext-misc 层解散 |
| 2026-11-01 | ext-oss 移除 SFTP 后端（`presign` / `presignPut` 无法实现，属接口违约），只留 local + S3；`sshd` 依赖与 BOM 约束一并删除 |
| 2026-11-11 | ext-job 新增 `@CronJob` + `CronJobScheduler`（cron 到点投递，执行与重试仍走轮询）；ext-jdbc 的 `selectList` / `selectCount` 自动追加数据权限条件 |
| 2026-11-13 | 新增 ext-metrics：自建指标采集（QPS / P99 / 错误率 / 错误码 / 限流拒绝 / 幂等命中 / 慢 SQL / 池水位）、即时告警与 Web 大屏；埋点走 `ext-core` 的 `MetricsRecorder` |
