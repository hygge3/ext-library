[根目录](../CLAUDE.md) > **ext-infra**

# ext-infra - 基础设施层

> 提供 Redis、PostgreSQL、缓存、邮件等基础设施集成

## 层级职责

ext-infra 负责与外部基础设施的集成，为上层业务模块提供数据缓存、消息通知等能力。

## 子模块列表

| 模块 | 描述 | 主要依赖 |
|------|------|----------|
| ext-redis | Redis 操作封装，提供统一的 Redis 访问接口 | ext-core, ext-json, spring-data-redis |
| ext-postgres | PostgreSQL 功能模块（缓存、队列、发布订阅、限流、会话） | ext-core, ext-json, postgresql |
| ext-cache | 多级缓存抽象 (Caffeine + Redis/PostgreSQL) | ext-redis(可选), ext-postgres(可选), caffeine |
| ext-mail | 邮件发送服务封装 | spring-boot-starter-mail |

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
- **会话管理**: 基于数据库的会话存储

**包结构**: `ext.library.postgres`

**配置前缀**: `ext.postgres`

**核心类**:
- `PostgresCacheManager` - 缓存管理器
- `PostgresQueue` - 任务队列
- `PostgresPubSub` - 发布订阅管理器
- `PostgresRateLimiter` - 限流器
- `PostgresSessionManager` - 会话管理器

**表结构**:
- `pg_cache` - 缓存表（UNLOGGED）
- `pg_jobs` - 任务队列表
- `pg_rate_limits` - 限流记录表
- `pg_sessions` - 会话表

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
- **邮件模型**: `MailDetails`, `MailSendInfo`
- **事件支持**: `MailSendEvent` 邮件发送事件

**包结构**: `ext.library.mail`

**配置前缀**: `spring.mail`

## 依赖关系

```
ext-common (ext-core, ext-json)
    |
    +---> ext-redis (独立)
    |
    +---> ext-postgres (独立)
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
- `ext-postgres/src/main/java/ext/library/postgres/` - PostgreSQL 相关源码
- `ext-cache/src/main/java/ext/library/cache/` - 缓存相关源码
- `ext-mail/src/main/java/ext/library/mail/` - 邮件相关源码

## 变更记录

| 日期 | 变更内容 |
|------|----------|
| 2026-01-26 | ext-cache 支持 PostgreSQL 缓存后端，解除循环依赖 |
| 2026-01-22 | 添加 ext-postgres 模块 |
| 2026-01-19 | 初始化 CLAUDE.md 文档 |
