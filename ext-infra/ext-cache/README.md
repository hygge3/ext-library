# ext-cache

> 缓存抽象模块 - 提供多级缓存支持 (Caffeine + Redis)

## 简介

`ext-cache` 是 ext-library 的缓存抽象模块，支持本地缓存 (Caffeine) 和分布式缓存 (Redis) 的多级缓存架构。

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

| 依赖 | 说明 |
|------|------|
| ext-redis | Redis 缓存支持 |
| caffeine | 高性能本地缓存 |

## 功能特性

- **本地缓存**：使用 Caffeine，提供高性能的本地缓存
- **分布式缓存**：使用 Redis，支持多节点共享
- **多级缓存**：两级缓存自动保持一致
- **缓存注解**：支持 Spring Cache 标准注解

## 配置项

| 配置项 | 默认值 | 说明 |
|-------|-------|------|
| ext.cache.caffeine.spec | 空 | Caffeine 配置规格 |
| ext.cache.redis-ttl | 5m | Redis 缓存 TTL |

## 缓存注解

支持 Spring Cache 标准注解：

| 注解 | 说明 |
|------|------|
| `@Cacheable` | 缓存查询结果 |
| `@CacheEvict` | 清除缓存 |
| `@CachePut` | 更新缓存 |
| `@Caching` | 组合注解 |

## 使用示例

### 配置缓存

```yaml
ext:
  cache:
    caffeine:
      spec: maximumSize=1000,expireAfterWrite=5m
    redis-ttl: 5m
```

### 使用注解

```java
@Service
public class UserService {

    @Cacheable(value = "users", key = "#id")
    public User getById(Long id) {
        return userMapper.selectById(id);
    }

    @CachePut(value = "users", key = "#user.id")
    public User update(User user) {
        userMapper.update(user);
        return user;
    }

    @CacheEvict(value = "users", key = "#id")
    public void delete(Long id) {
        userMapper.delete(id);
    }

    @CacheEvict(value = "users", allEntries = true)
    public void clearAll() {
        // 清除所有缓存
    }
}
```

### 手动操作缓存

```java
@Autowired
private CacheManager cacheManager;

public void manualCache() {
    Cache userCache = cacheManager.getCache("users");

    // 写入缓存
    userCache.put(1L, user);

    // 读取缓存
    User user = userCache.get(1L, User.class);

    // 删除缓存
    userCache.evict(1L);
}
```

## 许可证

[Apache License 2.0](../../LICENSE)
