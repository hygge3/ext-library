[根目录](../AGENTS.md) > **ext-infra**

# ext-infra - 基础设施层

> 提供 Redis、PostgreSQL、缓存、邮件等基础设施集成

## 层级职责

ext-infra 负责与外部基础设施的集成，为上层业务模块提供数据缓存、消息通知等能力。

## 子模块列表

| 模块 | 描述 | 主要依赖 |
|------|------|----------|
| ext-redis | Redis 操作封装，提供统一的 Redis 访问接口 | ext-core, ext-json, spring-data-redis |
| ext-lock | 分布式锁，注解式 Redis 加锁 | ext-core, ext-redis |
| ext-postgres | PostgreSQL 功能模块（缓存、队列、发布订阅、限流） | ext-core, ext-json, postgresql |
| ext-cache | 多级缓存抽象 (Caffeine + Redis/PostgreSQL) | ext-redis(可选), ext-postgres(可选), caffeine |
| ext-mail | 邮件发送服务封装 | spring-boot-starter-mail |
| ext-jdbc | 基于 JdbcClient 的通用 DAO 与链式条件构造 | ext-core, spring-boot-starter-jdbc |
| ext-job | 基于 PostgreSQL 队列的声明式任务投递与执行 | ext-postgres |

## 模块详情

### ext-redis

Redis 操作模块，提供：

- RedisTemplate 增强配置
- JSON 序列化支持
- 连接池管理 (commons-pool2)

**包结构**: `ext.library.redis`

**配置前缀**: `spring.data.redis`

### ext-postgres

PostgreSQL 功能模块，使用 PostgreSQL 原生特性实现：

- **缓存**: UNLOGGED 表实现高性能缓存（无 WAL，性能接近内存）
- **队列**: `FOR UPDATE SKIP LOCKED` 实现无锁任务队列
- **发布订阅**: `LISTEN/NOTIFY` 原生消息机制
- **限流**: 计数表 + 固定窗口算法
- **表结构初始化**: 启动时自动建表（`ext.postgres.auto-init-schema`）

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

### ext-lock

分布式锁模块，在 `DistributedLock` 之上包一层声明式入口：

- `@RedisLock` - 锁注解（`key` 支持 SpEL、`timeout` 持有时间、`waitTime` 等待时间）
- `RedisLockAspect` - 锁切面，执行完毕自动释放
- `LockKeyGenerator` - 锁键生成器（SpEL），可替换为自定义实现
- `LockException` - 未抢到锁时抛出，携带锁 key

**包结构**: `ext.library.lock`

底层锁由 ext-redis 的 `DistributedLock` 提供（线程内可重入，Lua 脚本保证释放的原子性）。

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
- `CacheAutoConfig` - 自动配置类
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

### ext-mail

邮件发送模块，提供：

- **邮件发送**: `MailSender`, `MailSenderImpl`
- **邮件模型**: `MailDetails`
- **事件支持**: `MailSendEvent` 邮件发送事件

**包结构**: `ext.library.mail`

**配置前缀**: `spring.mail`

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

## 依赖关系

```
ext-common (ext-core, ext-json)
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

## 变更记录

| 日期 | 变更内容 |
|------|----------|
| 2026-10-10 | 新增 ext-job 模块（基于 PostgreSQL 队列的任务调度） |
| 2026-10-10 | 新增 ext-jdbc 模块（JdbcClient 增强与轻量 DAO） |
| 2026-01-26 | ext-cache 支持 PostgreSQL 缓存后端，解除循环依赖 |
| 2026-01-22 | 添加 ext-postgres 模块 |
| 2026-01-19 | 初始化 AGENTS.md 文档 |
