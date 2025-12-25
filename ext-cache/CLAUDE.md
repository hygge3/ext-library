[根目录](../CLAUDE.md) > **ext-cache**

# ext-cache 模块文档

## 模块职责

ext-cache 提供基于注解的缓存解决方案，支持本地缓存、分布式缓存和二级缓存。通过 AOP 切面实现声明式缓存管理。

## 入口与启动

### 自动配置类
- **CacheAutoConfig**: 缓存模块自动配置
  - 注册 `CacheAspect` 切面
  - 根据配置初始化缓存策略

## 核心组件

### 1. 缓存注解 (annotation/)
- **@Cache**: 方法级缓存注解
  - `cacheName`: 缓存名称
  - `key`: SpEL 表达式键
  - `timeout`: 超时时间
  - `timeUnit`: 时间单位
  - `type`: 缓存类型（FULL/PUT/DELETE）

### 2. 切面 (aspect/)
- **CacheAspect**: 缓存切面处理器
  - 拦截 @Cache 注解方法
  - 根据缓存类型执行相应操作
  - 支持 SpEL 表达式解析

### 3. 缓存策略 (strategy/)
- **CacheStrategy**: 缓存策略接口
- **CaffeineStrategy**: 本地缓存实现
- **RedisStrategy**: Redis 分布式缓存实现
- **L2Strategy**: 二级缓存（本地+Redis）

### 4. 枚举 (enums/)
- **CacheType**: 缓存类型枚举
  - `FULL`: 全量缓存（先读缓存，没有则查库并写入）
  - `PUT`: 仅写入（只写入缓存，不读取）
  - `DELETE`: 仅删除（从缓存中删除）
- **CacheStorage**: 缓存存储枚举
  - `LOCAL`: 本地缓存
  - `REDIS`: Redis 缓存
  - `L2`: 二级缓存

### 5. 配置属性 (config/properties/)
- **CacheProperties**: 缓存配置参数
  - 默认超时时间
  - 缓存类型
  - 存储策略

## 关键依赖

- **ext-redis**: Redis 支持（可选）
- **caffeine**: 本地缓存实现
- **ext-core**: Spring 集成支持

## 使用示例

### 基本使用
```java
@Service
public class UserService {

    // 全量缓存：先查缓存，没有则查库并写入
    @Cache(cacheName = "user", key = "#id", timeout = 300, timeUnit = TimeUnit.SECONDS)
    public User getUserById(Long id) {
        return userRepository.findById(id);
    }

    // 更新缓存：更新后写入缓存
    @Cache(cacheName = "user", key = "#user.id", type = CacheType.PUT)
    public User updateUser(User user) {
        return userRepository.save(user);
    }

    // 删除缓存：从缓存中删除
    @Cache(cacheName = "user", key = "#id", type = CacheType.DELETE)
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }
}
```

### 配置示例
```yaml
ext:
  cache:
    # 默认缓存类型
    default-type: FULL
    # 默认存储策略
    storage: L2
    # 默认超时时间（秒）
    default-timeout: 120
```

### 自定义缓存策略
```java
@Component
public class CustomCacheStrategy implements CacheStrategy {
    @Override
    public <T> T get(String key, Class<T> clazz) {
        // 实现自定义缓存获取逻辑
        return null;
    }

    @Override
    public void set(String key, Object value, long timeout) {
        // 实现自定义缓存设置逻辑
    }

    @Override
    public void delete(String key) {
        // 实现自定义缓存删除逻辑
    }
}
```

### SpEL 表达式示例
```java
// 使用方法参数
@Cache(cacheName = "user", key = "#id")
public User getUser(Long id) { ... }

// 使用对象属性
@Cache(cacheName = "user", key = "#user.id")
public void updateUser(User user) { ... }

// 使用多个参数
@Cache(cacheName = "user", key = "#userId + ':' + #type")
public User getUserByType(Long userId, String type) { ... }

// 使用返回值
@Cache(cacheName = "user", key = "#result.id", type = CacheType.PUT)
public User createUser(User user) { ... }

// 使用 Bean
@Cache(cacheName = "user", key = "@beanName.methodName(#id)")
public User getUser(Long id) { ... }
```

## 常见问题 (FAQ)

### Q: 如何切换缓存实现？
通过配置 `ext.cache.storage` 属性：
- `LOCAL`: 使用 Caffeine 本地缓存
- `REDIS`: 使用 Redis 分布式缓存
- `L2`: 使用二级缓存（推荐）

### Q: SpEL 表达式支持哪些变量？
- 方法参数：直接使用参数名，如 `#id`、`#user`
- 返回值：`#result`
- Bean：`@beanName`

### Q: 如何处理缓存穿透？
使用 L2 缓存策略，本地缓存作为一级，Redis 作为二级。二级缓存可以设置空值，防止穿透。

### Q: 缓存超时时间如何设置？
可以在注解中单独设置，也可以使用全局默认值。

## 相关文件清单

### 主要源码文件
- `src/main/java/ext/library/cache/annotion/Cache.java`
- `src/main/java/ext/library/cache/core/CacheAspect.java`
- `src/main/java/ext/library/cache/strategy/CacheStrategy.java`
- `src/main/java/ext/library/cache/strategy/CaffeineStrategy.java`
- `src/main/java/ext/library/cache/strategy/RedisStrategy.java`
- `src/main/java/ext/library/cache/strategy/L2Strategy.java`
- `src/main/java/ext/library/cache/config/CacheAutoConfig.java`

### 配置文件
- `src/main/resources/META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`

## 变更记录 (Changelog)

### 2025-12-24
- 更新：补充详细的使用示例
- 更新：补充 SpEL 表达式示例
- 更新：补充常见问题解答

### 2025-12-19
- 创建：模块文档
- 支持：多种缓存策略
- 优化：SpEL 表达式解析
