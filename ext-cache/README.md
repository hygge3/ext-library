# ext-cache（缓存抽象）

## 功能

- 多级缓存支持
- 统一缓存接口
- Caffeine 缓存集成
- 缓存自动配置
- 灵活的配置方式

## 依赖引用

### Maven

```xml
<dependency>
    <groupId>ext.library</groupId>
    <artifactId>ext-cache</artifactId>
    <version>${version}</version>
</dependency>
```

### Gradle

```groovy
compile("ext.library:ext-cache:${version}")
```

## 依赖模块

ext-cache 依赖以下模块：
- ext-redis：Redis 缓存支持
- Caffeine：高性能本地缓存

## 配置项

| 配置项 | 默认值 | 说明 |
|-------|-------|------|
| ext.cache.caffeine.spec | 空 | Caffeine 配置规格 |
| ext.cache.redis-ttl | 5分钟 | Redis 缓存 TTL |

## 核心特性

### 多级缓存

- **本地缓存**：使用 Caffeine，提供高性能的本地缓存
- **分布式缓存**：使用 Redis，支持多节点共享
- **自动同步**：两级缓存自动保持一致

### 缓存注解

支持 Spring Cache 标准注解：
- @Cacheable：缓存查询结果
- @CacheEvict：清除缓存
- @CachePut：更新缓存
- @Caching：组合注解

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
}
```

### 手动操作

```java
@Autowired
private CacheManager cacheManager;

public void manualCache() {
    Cache userCache = cacheManager.getCache("users");
    userCache.put(1L, user);
    User user = userCache.get(1L, User.class);
}
```
