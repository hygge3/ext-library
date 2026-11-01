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
| ext-jdbc | 基于 JdbcClient 的通用 DAO 与链式条件构造 | ext-core, spring-boot-starter-jdbc |
| ext-job | 基于 PostgreSQL 队列的声明式任务投递与执行 | ext-postgres |
| ext-i18n | 国际化，资源文件与数据库双源翻译 | ext-core, ext-json, spring-boot-starter-jdbc(可选) |
| ext-oss | 对象存储，支持本地文件系统与 S3 协议 | ext-core；可选 AWS SDK v2（s3 + apache-client） |

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
| 限流 | 由 [ext-ratelimit](../ext-enhance/AGENTS.md#ext-ratelimit) 的 `RedisRateLimitStore` 用 | 同上的 `PostgresRateLimitStore` | 跟缓存同一选型，不要一边 Redis 一边 PG |
| 分布式锁 | `DistributedLock`（Lua 释放、线程内可重入、看过期） | 无锁原语（`pg_try_advisory_lock` 属 [ext-lock](#ext-lock) 的 `PostgresLockStore`） | 需要可重入/锁续约选 Redis；只要互斥用 PG 咨询锁 |
| 运维成本 | 多一个中间件、多一套监控/备份 | 零新增组件，复用现有库 | 这是 PG 方案存在的主要理由 |

不要两套同时做同一件事：缓存、限流、队列都只选一个后端，混用只会把“数据在哪”变成排障谜题。

**约定**：连接配置沿用 Spring 前缀 `spring.data.redis`，不新增 `ext.redis.*`。

### ext-postgres

PostgreSQL 功能模块，使用 PostgreSQL 原生特性实现：

- **缓存**: UNLOGGED 表实现高性能缓存（无 WAL，性能接近内存）
- **队列**: `FOR UPDATE SKIP LOCKED` 实现无锁任务队列
- **发布订阅**: `LISTEN/NOTIFY` 原生消息机制
- **限流**: 计数表 + 固定窗口算法
- **表结构初始化**: 各能力独立开关，按开关建表（`ext.postgres.cache.auto-init-schema` / `ext.postgres.queue.auto-init-schema` / `ext.postgres.ratelimit.auto-init-schema`，全关则不建表）

**包结构**: `ext.library.postgres`

**配置前缀**: `ext.postgres`

**核心类**:
- `PostgresCacheManager` - 缓存管理器
- `PostgresQueue` - 任务队列
- `PostgresPubSub` - 发布订阅管理器
- `PostgresRateLimiter` - 限流器
- `PostgresSchemaInitializer` - 表结构自动初始化

**表结构**:
- `pg_cache` - 缓存表（UNLOGGED）
- `pg_jobs` - 任务队列表
- `pg_rate_limits` - 限流记录表

#### 设计约束

**职责**：用 PostgreSQL 原生特性替代 Redis 的四类能力，使只需一个关系库的项目也能有分布式缓存 / 队列 / 广播 / 限流：

- **缓存**：UNLOGGED 表（无 WAL，性能接近内存）——`PostgresCacheManager`。
- **队列**：`FOR UPDATE SKIP LOCKED` 无锁出队——`PostgresQueue`。
- **发布订阅**：`LISTEN/NOTIFY`——`PostgresPubSub`（实现 [ext-core](../ext-common/AGENTS.md#ext-core) 的 `PubSub` SPI）。
- **限流**：计数表 + 固定窗口——`PostgresRateLimiter`。

**建表与配置**：每种能力有自己的开关与表名，互不影响——`ext.postgres.cache.*`（`auto-init-schema`、`table-name`）、`ext.postgres.queue.*`（`auto-init-schema`、`table-name`）、`ext.postgres.ratelimit.*`（`auto-init-schema`、`table-name`），`auto-init-schema` 默认均为 `true`。`PostgresSchemaInitializer` 只为开着的那些能力建表（`CREATE TABLE IF NOT EXISTS` + `CREATE INDEX IF NOT EXISTS`，幂等），全关时不连库、不建表；`pubsub` 不需要表。表名可改（用于与已有表/前缀共存），DDL 语句统一引用配置的表名，不写死 `pg_*`。其余参数同样归到各自能力下：`cache.default-expire-time` / `cache.cleanup-interval`、`queue.max-attempts`、`ratelimit.window` / `ratelimit.default-limit`。

**边界**：依赖 `ext-core`、`ext-json`、`postgresql` 驱动、`starter-jdbc`；不依赖 `ext-redis`，与 [ext-cache](#ext-cache) 之间只有「cache 单向可选引用 postgres」的关系，不成环。队列能力被 [ext-job](#ext-job) 直接复用，不在别处另造调度原语。

**调用方自建表**：把对应 `auto-init-schema` 关掉、自己用迁移工具（Flyway / Liquibase）建表即可——关掉就是完全不建表，不会、也不应被下游模块逼着建；[ext-job](#ext-job) 的队列同样只看这个开关，自己不做 DDL。

**约定**：配置前缀 `ext.postgres`。

### ext-lock

分布式锁模块，在锁存储 SPI 之上包一层声明式入口：

- `@RedisLock` - 锁注解（`key` 支持 SpEL、`timeout` 持有时间、`waitTime` 等待时间）；注解名沿用旧契约，内部不绑定后端
- `RedisLockAspect` - 锁切面，正常返回与抛异常都自动释放
- `LockKeyGenerator` - 锁键生成器（SpEL），可替换为自定义实现
- `LockStore` - 锁存储 SPI；实现 `RedisLockStore`（ext-redis 的 `DistributedLock`，自带 TTL）与 `PostgresLockStore`（`pg_try_advisory_lock` 会话级锁）
- `LockException` - 未抢到锁时抛出，携带锁 key

**包结构**: `ext.library.lock`

后端由 `ext.lock.store-type` 选择（`AUTO` / `REDIS` / `POSTGRES`），两个后端模块均为 `compileOnly`，一个都没引时装配阶段直接报错。

#### 设计约束

**职责**：在锁存储 SPI 之上包一层声明式入口：`@RedisLock`（`key` 支持 SpEL、`timeout` 持有时长、`waitTime` 等待时长）、`RedisLockAspect`（方法返回或抛异常都自动释放）、可替换的 `LockKeyGenerator`、未抢到锁时抛 `LockException`（携带锁 key，便于上层转成 409/重试提示）。

**后端**：锁原语由 `LockStore` SPI 提供，实现两种——`RedisLockStore`（默认，Lua 释放 + 线程内可重入，原语来自 [ext-redis](#ext-redis)）与 `PostgresLockStore`（`pg_try_advisory_lock` / `pg_advisory_unlock`，会话级锁随连接释放，不依赖 [ext-redis](#ext-redis)）。选型由 `ext.lock.store-type` 控制：`AUTO`（默认，按类路径优先 Redis）/ `REDIS` / `POSTGRES`。注解名继续叫 `@RedisLock`（对外契约不改），内部不绑定后端。`ext-redis` / `ext-postgres` 均为 `compileOnly`；一个后端都没引时装配阶段直接报错，不静默降级——锁失效比启动失败危险得多。

**边界**：本模块不实现锁协议、不做锁续约之外的调度；Web / servlet 能力经 [ext-servlet](../ext-common/AGENTS.md#ext-servlet)（`compileOnly`）取得——`LockKeyGenerator` 用 `ServletUtil.getRequestOrNull()` 拿当前请求，不自己写 `RequestContextHolder` 样板；`jakarta.servlet-api` 仅因 `LockKeyGenerator` 签名里出现 `HttpServletRequest` 而保留为 `compileOnly`（由消费方容器提供），非 Web 场景不引入。

### ext-cache

多级缓存抽象模块，支持：

- **本地缓存**: Caffeine 高性能本地缓存
- **分布式缓存**: Redis 或 PostgreSQL（可配置）
- **二级缓存**: Caffeine + 分布式缓存（后端可切换）
- **缓存策略**: `CaffeineStrategy`, `RedisStrategy`, `PostgresStrategy`, `L2Strategy`
- **注解支持**: `@Cache` 缓存注解

**包结构**: `ext.library.cache`

**配置前缀**: `ext.cache`

**核心类**:
- `CacheAutoConfiguration` - 自动配置类
- `CacheProperties` - 缓存配置属性
- `Cache` - 缓存注解
- `CacheStorage` - 缓存存储方式枚举
- `L2Backend` - 二级缓存后端枚举

**配置示例**:
```yaml
ext:
  cache:
    cache-storage: L2        # REDIS/POSTGRES/CAFFEINE/L2
    l2-backend: POSTGRES     # L2 模式下的分布式后端：REDIS/POSTGRES
```

#### 设计约束

**职责**：统一的缓存抽象与四套策略——`CaffeineStrategy`（本地）、`RedisStrategy`、`PostgresStrategy`、`L2Strategy`（Caffeine + 分布式两级）；`@Cache` 注解 + 切面提供声明式缓存；`CacheProperties` 承载 `ext.cache.*`（`key-prefix`、`expire-time`、`cache-storage`、`l2-backend`、`caffeine.*`）。`CacheStrategy` 除 `get` / `put` / `evict` / `clear` 外提供原子的 `putIfAbsent(cacheName, key, value, expireTime)`，供 [ext-idempotent](../ext-enhance/AGENTS.md#ext-idempotent) 这类「在时间窗内只放行一次」的场景复用同一套后端。

**边界**：`ext-redis`、`ext-postgres`、`caffeine` 全部是 `compileOnly`。由此推出两件事：

- 单独引入 `ext-cache` 不会把 Redis / PostgreSQL / Caffeine 拖进消费方；要用哪个后端就由消费方自己引入对应依赖，`AUTO` 场景按类路径选实现（优先 Redis）。
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

### ext-jdbc

JdbcClient 增强模块，在 Spring `JdbcClient` 之上补一层注解驱动的轻量 DAO，不引入 ORM：

- **注解**: `@Table`（表名）、`@Id`（主键字段）
- **DAO 工厂**: `JdbcDaoFactory#of(Class)` 按实体类型产出 `JdbcDao<T>`
- **元数据**: `EntityMetaData` 在首次使用时反射解析表名、主键与列名
- **行映射**: `EntityRowMapper` 按列名（驼峰↔下划线）映射实体字段
- **链式条件**: `QueryWrapper<T>` 提供 eq/ne/lt/le/gt/ge/in/notIn/between/isNull/like/groupBy/having/orderBy/limit/last
- **分页**: `PageQuery`（页码/页大小）与 `PageResult<T>`（记录、总条数、总页数）
- **防护**: 无条件 `update`/`delete` 直接拒绝执行，必须带 `QueryWrapper` 或主键

**包结构**: `ext.library.jdbc`

所有值均以 `?` 占位符绑定，不会拼接进 SQL 文本。

**核心类**:
- `JdbcDao` - 通用 DAO（`selectById`/`selectList`/`selectCount`/`selectPage`/`insert`/`update`/`deleteById`/`delete`）
- `JdbcDaoFactory` - DAO 工厂
- `QueryWrapper` - 链式条件构造
- `EntityMetaData` / `EntityRowMapper` - 元数据解析与行映射
- `PageQuery` / `PageResult` - 分页入参与结果
- `JdbcAutoConfiguration` - 自动装配 `JdbcDaoFactory`

**使用示例**:
```java
@Table("sys_user")
public class User {
    @Id
    private Long id;
    private String name;
}

JdbcDao<User> userDao = jdbcDaoFactory.of(User.class);
Optional<User> user = userDao.selectById(1L);

PageQuery page = new PageQuery();
page.setPage(1);
page.setSize(20);
PageResult<User> result = userDao.selectPage(page,
        QueryWrapper.<User>of().like("name", "张").orderByAsc("id"));

// 防护：无条件全表操作会被拒绝
userDao.delete(QueryWrapper.of());   // 抛 ExtException
```

#### 设计约束

**职责**：在 Spring `JdbcClient` 之上补一层注解驱动的轻量 DAO，**不引入 ORM**：`@Table` / `@Id` 标注实体，`JdbcDaoFactory#of(Class)` 产出 `JdbcDao<T>`，`EntityMetaData` + `EntityRowMapper` 反射解析表名、主键与列名（驼峰↔下划线），`QueryWrapper<T>` 链式条件（eq/ne/lt/le/gt/ge/in/notIn/between/isNull/like/groupBy/having/orderBy/limit/last），`PageQuery` / `PageResult<T>` 分页——**这两个类型属于 [ext-servlet](../ext-common/AGENTS.md#ext-servlet) 而非本模块**，`selectPage` 直接用基座那一套，全库只有一份定义。

**决策（名字转换与反查）**：列名↔属性名的互转全部复用 `ext-tool` 的 `StringUtil.underlineToCamelCase` / `camelToUnderline`，`EntityMetaData` 只做委托，不再自己写一遍字符循环——下划线风格全库只有一处定义，改风格只改一处。反查（拿到数据库返回的列名找 setter）不靠每次反射：`EntityRowMapper` 构造时用 JavaBeans 内省（`Introspector`）一次性建好「小写属性名 → setter + 属性类型」的不可变 `Map`，回填时只做一次查表加类型转换。

**不变量**：

- 所有值以 `?` 占位符绑定，绝不拼接进 SQL 文本。
- 无条件的 `update` / `delete` 直接拒绝执行（抛 `ExtException`）——必须带 `QueryWrapper` 或主键。这是防误删全表的硬约束，不要为了"方便"放开。

**边界**：`starter-jdbc` 为 `compileOnly`（由消费方的数据源提供）；不管理事务、不做 DDL 迁移。

### ext-job

任务调度模块，直接复用 ext-postgres 的 `PostgresQueue`，不引入独立调度框架：

- **处理器**: `JobHandler<T>` 声明负载类型、队列名与处理逻辑，`handle` 抛异常即视为本次尝试失败
- **投递器**: `JobDispatcher` 按队列投递任务，支持延迟投递、重试次数上限与队列统计
- **执行器**: `JobExecutor` 每轮按队列出队处理，由 `@Scheduled` 轮询触发
- **运维接口**: `countPending`/`stats`/`cleanup`/`resetStuck`/`queues`，用于积压观测与卡死任务回收

**包结构**: `ext.library.job`

**配置前缀**: `ext.job`

**核心类**:
- `JobHandler` - 处理器接口（含 `of(Class, Consumer)` 便捷工厂）
- `JobDispatcher` - 任务投递器
- `JobExecutor` - 轮询执行器
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

**决策**：队列直接用 PostgreSQL 的 `FOR UPDATE SKIP LOCKED`，因此不需要额外的消息中间件——这也是本库"用 PG 替代 Redis"路线的一部分。

**边界与装配**：未注册任何 `JobHandler` 时只装配投递器与执行器，不启动轮询；配置前缀 `ext.job`（`enabled`、`batch-size`、`poll-interval-millis`、`stuck-timeout`、`completed-retention`、`recover-on-startup`、`cleanup-on-startup`）。

**建表**：队列表由 [ext-postgres](#ext-postgres) 的 `ext.postgres.queue.auto-init-schema` 决定是否自动创建（默认 `true`，表名 `ext.postgres.queue.table-name`，默认 `pg_jobs`）。关掉它就真的不建表——`ext-job` 自己不做任何 DDL，也不会在启动时偷偷把表补上；这时消费方必须先用迁移工具建好同名表（列结构见 [ext-postgres](#ext-postgres)），否则 `ext-job` 不可用。

### ext-i18n

国际化模块，把翻译文案拆成多个来源，查不到就回退到下一个：

- **管理器**: `I18nMessageManager` 按 `Locale` 顺序查询各翻译源，支持占位符参数、`find` 返回 `Optional`、`contains` 判定与 `clear` 清缓存
- **来源 SPI**: `TranslationSource` 接口（`load(Locale)` / `name()` / `order()`），`order()` 越小越先查询
- **资源文件源**: `ResourceBundleTranslationSource` 读 classpath 下的 `ResourceBundle`
- **数据库源**: `JdbcTranslationSource` 读翻译表，查询走语言降级链 `zh-CN` → `zh` → `ROOT`，按降级顺序合并
- **缓存**: 按语言缓存翻译表，`ext.i18n.cache-ttl` 到期重载，便于翻译变更后自动��效

**包结构**: `ext.library.i18n`

**配置前缀**: `ext.i18n`

**核心类**:
- `I18nMessageManager` - 翻译管理器
- `TranslationSource` - 翻译来源 SPI
- `ResourceBundleTranslationSource` / `JdbcTranslationSource` - 两个翻译源
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

- **门面**: `OssTemplate` - put/putBytes/putFile/copy/move/get/getString/getBytes/stat/delete/exists/list/presign/presignPut/generateKey
- **SPI**: `ObjectStorage` 接口，两种后端各实现一份；`copy` / `move` 带跨桶重载，无桶概念的实现抛 `UnsupportedOperationException`
- **本地实现**: `LocalObjectStorage` 写本地目录，可配置对外访问前缀
- **S3 实现**: `S3ObjectStorage` 基于 AWS SDK for Java v2（`S3Client` + `S3Presigner`），超过 `part-size` 走分片上传，复制走服务端 `CopyObject`
- **返回值**: `OssObject`（bucket/key/contentType/size/content 流，实现 `Closeable`）、`ObjectMeta`（stat 结果，不含内容）、`ObjectItem`（列表项）
- **实现选择**: 按 `ext.oss.type` 选 `local` / `s3`；厂商类型只出现在自动配置类方法体内部，且各方 SDK 都是 `compileOnly`，容器里没有对应 SDK 时其他模式照样能启动；S3 模式还要额外引 `software.amazon.awssdk:apache-client`（或 `url-connection-client`），`s3` 本身不带 HTTP 客户端实现
- **不在范围**: 跨实现复制（local → s3 属于数据迁移）、CDN 回源、图片处理

**包结构**: `ext.library.oss`

**配置前缀**: `ext.oss`

**核心类**:
- `OssTemplate` - 对象存储门面
- `ObjectStorage` - 存储 SPI
- `LocalObjectStorage` / `S3ObjectStorage` - 两种实现
- `OssObject` / `ObjectMeta` / `ObjectItem` - 返回模型
- `OssProperties` / `OssAutoConfiguration` - 配置属性与自动配置

**使用示例**:
```java
String key = ossTemplate.generateKey("avatar", "logo.png");
String url = ossTemplate.put(key, Files.newInputStream(file), size, "image/png");

ossTemplate.copy(key, "backup/" + key);                  // 服务端复制，数据不过进程
ossTemplate.move(key, "archive/" + key);                 // copy + delete
String url2 = ossTemplate.presign(key);                  // 预签名链接
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
    chunked-encoding: true         # 旧网关不认 aws-chunked 时关掉
```

#### 设计约束

**职责**：`OssTemplate` 作为唯一门面（`put` / `putBytes` / `putFile` / `copy` / `move` / `get` / `getString` / `getBytes` / `stat` / `delete` / `exists` / `list` / `presign` / `presignPut` / `generateKey`），背后是可替换的 `ObjectStorage` SPI，两份实现：`LocalObjectStorage`（写本地目录，可配对外访问前缀）与 `S3ObjectStorage`（AWS SDK for Java v2，分片上传 + 预签名链接 + 服务端复制）。返回模型 `OssObject`（`Closeable`，含 bucket/key/contentType/size/流）、`ObjectMeta`（`stat` 结果：size/contentType/lastModified，不含内容）与 `ObjectItem`（列表项）。

**装配约束**：按 `ext.oss.type` 选 `local` / `s3`。各实现的厂商类型（AWS SDK 的 `S3Client` / `S3Presigner`）只出现在自动配置类的方法体内部，且各自是 `compileOnly`，因此**容器里没有对应 SDK 时其他模式仍能启动**——新增存储实现必须沿用这个约束，不能把厂商类型泄进门面或 SPI 签名。S3 模式除 `software.amazon.awssdk:s3` 外还必须引一个 HTTP 客户端实现（`apache-client` 或 `url-connection-client`）——`s3` 只带 `http-client-spi` 接口，SDK 找不到实现会在建 `S3Client` 时抛 `SdkClientException`。

**S3 模式**：`ext.oss.type=s3`，配置 `ext.oss.*` 的 `endpoint` / `access-key` / `secret-key` / `region` / `bucket` / `auto-create-bucket` / `part-size`（默认 10MB）/ `presign-expiry`，外加两个 S3 兼容性开关：`path-style-access`（默认 `true`——MinIO、Ceph、R2 这类自建端点几乎都要求 path-style，AWS 官方端点要关掉）与 `chunked-encoding`（默认 `true`，个别旧网关不认 `aws-chunked` 时要关）。

**复制语义**：`copy(sourceKey, targetKey)` 与跨桶重载 `copy(sourceBucket, sourceKey, targetBucket, targetKey)` 走服务端复制（`CopyObject`），数据不经过应用进程；`move` 是 `copy` + `delete` 的组合，因为 S3 协议里没有原子 rename。本地实现用 `Files.copy` / `Files.move`。跨**实现**复制（如 local → s3）不做：那是数据迁移工具的事，门面不为它引入双客户端装配。

**边界**：不实现 CDN 回源、图片处理、配额管理，也不做跨实现复制；对象生命周期（过期清理）由消费方负责。

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
    +---> ext-jdbc (独立)
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
```

**注意**: ext-postgres 和 ext-cache 之间没有循环依赖。ext-cache 通过可选依赖引用 ext-postgres。

## 相关文件

- `ext-redis/src/main/java/ext/library/redis/` - Redis 相关源码
- `ext-lock/src/main/java/ext/library/lock/` - 分布式锁源码
- `ext-postgres/src/main/java/ext/library/postgres/` - PostgreSQL 相关源码
- `ext-cache/src/main/java/ext/library/cache/` - 缓存相关源码
- `ext-mail/src/main/java/ext/library/mail/` - 邮件相关源码
- `ext-jdbc/src/main/java/ext/library/jdbc/` - JdbcClient DAO 相关源码
- `ext-job/src/main/java/ext/library/job/` - 任务调度源码
- `ext-i18n/src/main/java/ext/library/i18n/` - 国际化源码
- `ext-oss/src/main/java/ext/library/oss/` - 对象存储源码

## 变更记录

| 日期 | 变更内容 |
|------|----------|
| 2026-11-01 | ext-oss 移除 SFTP 后端（`presign` / `presignPut` 无法实现，属接口违约），只留 local + S3；`sshd` 依赖与 BOM 约束一并删除 |
| 2026-10-11 | 收编原 ext-misc 层的 ext-i18n / ext-oss，ext-misc 层解散 |
| 2026-10-10 | 新增 ext-job 模块（基于 PostgreSQL 队列的任务调度） |
| 2026-10-10 | 新增 ext-jdbc 模块（JdbcClient 增强与轻量 DAO） |
| 2026-01-26 | ext-cache 支持 PostgreSQL 缓存后端，解除循环依赖 |
| 2026-01-22 | 添加 ext-postgres 模块 |
| 2026-01-19 | 初始化 AGENTS.md 文档 |
