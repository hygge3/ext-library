# ext-cache

> 缓存抽象模块 - 提供多级缓存支持 (Caffeine + Redis/PostgreSQL)

## 简介

`ext-cache` 是 ext-library 的缓存抽象模块，支持本地缓存 (Caffeine) 和分布式缓存 (Redis/PostgreSQL) 的多级缓存架构。

## 快速开始

### Maven

```xml
<dependency>
    <groupId>ext.library</groupId>
    <artifactId>ext-cache</artifactId>
</dependency>
```

### Gradle

```groovy
implementation("ext.library:ext-cache")
```

## 依赖说明

| 依赖 | 说明 | 是否必需 |
|------|------|---------|
| caffeine | 高性能本地缓存 | 是 |
| ext-redis | Redis 分布式缓存支持 | 可选 |
| ext-postgres | PostgreSQL 分布式缓存支持 | 可选 |

> **注意**：使用 Redis 或 PostgreSQL 作为分布式缓存时，需要引入对应的模块依赖。

## 功能特性

- **本地缓存**：使用 Caffeine，提供高性能的本地缓存
- **分布式缓存**：支持 Redis 或 PostgreSQL 作为分布式缓存后端
- **多级缓存**：L2 策略自动管理 Caffeine + 分布式缓存的两级缓存
- **后端切换**：二级缓存支持在 Redis 和 PostgreSQL 之间灵活切换
- **自定义注解**：使用 `@Cache` 注解实现声明式缓存

## 配置项

| 配置项 | 默认值 | 说明 |
|-------|-------|------|
| `ext.cache.key-prefix` | `ext:cache` | 缓存 key 前缀 |
| `ext.cache.expire-time` | `86400s` | 默认缓存过期时间 |
| `ext.cache.cache-storage` | `AUTO` | 缓存存储方式，唯一的选型入口 |
| `ext.cache.expire-jitter` | `0.1` | 过期抖动比例，写入的 TTL 在 `[ttl × (1-jitter), ttl]` 内随机，`0` 关闭 |
| `ext.cache.load-timeout` | `3s` | 单飞时等待同 key 首次加载的上限，超时后自行回源 |
| `ext.cache.caffeine.maximum-size` | `10000` | Caffeine 最大缓存条目数 |
| `ext.cache.caffeine.refresh-on-access` | `true` | 访问后是否刷新过期时间 |

## 缓存存储方式

| 类型 | 说明 | 依赖模块 |
|------|------|---------|
| `AUTO` | 优先级 Redis > PostgreSQL > 本地内存：有 ext-redis 用 Redis，其次 ext-postgres，都没引用 Caffeine（默认） | - |
| `REDIS` | Redis 分布式缓存 | ext-redis |
| `POSTGRES` | PostgreSQL 分布式缓存 | ext-postgres |
| `CAFFEINE` | Caffeine 本地内存缓存 | - |
| `L2` | 二级缓存（Caffeine + 分布式缓存） | ext-redis 或 ext-postgres |

## 二级缓存的分布式后端

`L2` 的第二级后端不单独配置：按类路径自动选择，有 ext-redis 用 Redis，否则用 ext-postgres。
两个都在时 Redis 优先。只有一个选型键——`ext.cache.cache-storage`。

## @Cache 注解

自定义缓存注解，支持以下属性：

| 属性 | 类型 | 默认值 | 说明 |
|------|------|-------|------|
| `cacheName` | String | (必填) | 缓存名称 |
| `key` | String | (必填) | 缓存键，支持 SpEL 表达式 |
| `timeout` | long | `120` | 超时时间 |
| `timeUnit` | TimeUnit | `SECONDS` | 时间单位 |
| `type` | CacheType | `FULL` | 缓存类型 |
| `cacheNull` | boolean | `false` | 空结果是否写负缓存 |
| `nullTimeout` | long | `30` | 负缓存时长，单位同 `timeUnit` |

## 穿透 / 击穿 / 雪崩

| 问题 | 机制 | 怎么开 |
|------|------|--------|
| 穿透 | 空结果写短 TTL 负缓存，存在 `key + ":null"` 独立键上 | `@Cache(cacheNull = true, nullTimeout = 30)` |
| 击穿 | 同 `cacheName:key` 的并发回源合并为一次（进程内） | `FULL` 默认生效，无需配置 |
| 雪崩 | 写入 TTL 前随机抖动 | `ext.cache.expire-jitter`，默认 `0.1` |

负缓存只适合「查不到是常态」的方法，而且负缓存期间真实数据写进来也读不到，只能等 `nullTimeout` 过期；`CacheType.DELETE` 会连 `key + ":null"` 一起删。

### CacheType 类型

| 类型 | 说明 |
|------|------|
| `FULL` | 完整缓存：先查缓存，未命中则执行方法并缓存结果 |
| `PUT` | 强制更新：执行方法并更新缓存 |
| `DELETE` | 删除缓存：删除缓存后执行方法 |

## 使用示例

### 配置二级缓存

二级后端按依赖自动选择，不写 `l2-backend`：引入 ext-redis 就用 Redis，只引 ext-postgres 就用 PostgreSQL。

```yaml
ext:
  cache:
    key-prefix: myapp:cache
    expire-time: 1h
    cache-storage: L2
    caffeine:
      maximum-size: 5000
      refresh-on-access: true
```

### 仅使用 PostgreSQL 缓存

```yaml
ext:
  cache:
    cache-storage: POSTGRES
```

### 使用 @Cache 注解

```java
@Service
public class UserService {

    // 完整缓存：先查缓存，未命中则查数据库并缓存
    @Cache(cacheName = "users", key = "#id")
    public User getById(Long id) {
        return userMapper.selectById(id);
    }

    // 自定义超时时间
    @Cache(cacheName = "users", key = "#id", timeout = 5, timeUnit = TimeUnit.MINUTES)
    public User getByIdWithTimeout(Long id) {
        return userMapper.selectById(id);
    }

    // 强制更新缓存
    @Cache(cacheName = "users", key = "#user.id", type = CacheType.PUT)
    public User update(User user) {
        userMapper.update(user);
        return user;
    }

    // 删除缓存
    @Cache(cacheName = "users", key = "#id", type = CacheType.DELETE)
    public void delete(Long id) {
        userMapper.delete(id);
    }
}
```

### SpEL 表达式示例

```java
// 使用方法参数
@Cache(cacheName = "users", key = "#id")

// 使用对象属性
@Cache(cacheName = "users", key = "#user.id")

// 使用多个参数
@Cache(cacheName = "orders", key = "#userId + ':' + #orderId")

// 使用方法调用
@Cache(cacheName = "users", key = "#user.getName()")
```

## 架构说明

### L2 缓存工作流程

```
┌─────────────────────────────────────────────────────────────┐
│                        应用层                                │
│                     @Cache 注解                              │
└─────────────────────┬───────────────────────────────────────┘
                      │
                      ▼
┌─────────────────────────────────────────────────────────────┐
│                    L2Strategy                                │
│  ┌─────────────┐         ┌─────────────────────────────┐   │
│  │  Caffeine   │ ──miss─▶│  Redis / PostgreSQL         │   │
│  │  (L1 本地)   │◀─fill───│  (L2 分布式，可配置)          │   │
│  └─────────────┘         └─────────────────────────────┘   │
└─────────────────────────────────────────────────────────────┘
```

1. **读取**：先查 Caffeine，未命中查分布式缓存，命中后回填 Caffeine
2. **写入**：同时写入分布式缓存和 Caffeine
3. **删除**：同时从分布式缓存和 Caffeine 删除

## 许可证

[Apache License 2.0](../../LICENSE)
