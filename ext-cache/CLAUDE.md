[根目录](../../CLAUDE.md) > **ext-cache**

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
  - `FULL`: 全量缓存
  - `PUT`: 仅写入
  - `DELETE`: 仅删除
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

    @Cache(cacheName = "user", key = "#id", timeout = 300)
    public User getUserById(Long id) {
        // 从数据库查询
        return userRepository.findById(id);
    }

    @Cache(cacheName = "user", key = "#user.id", type = CacheType.PUT)
    public User updateUser(User user) {
        // 更新数据库
        return userRepository.save(user);
    }

    @Cache(cacheName = "user", key = "#id", type = CacheType.DELETE)
    public void deleteUser(Long id) {
        // 删除数据库记录
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
    // 实现自定义缓存逻辑
}
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
使用 L2 缓存策略，本地缓存作为一级，Redis 作为二级。

## 相关文件清单

### 主要源码文件
- `src/main/java/ext/library/cache/annotion/Cache.java`
- `src/main/java/ext/library/cache/core/CacheAspect.java`
- `src/main/java/ext/library/cache/strategy/CaffeineStrategy.java`
- `src/main/java/ext/library/cache/config/CacheAutoConfig.java`

## 变更记录 (Changelog)

### 2025-12-19
- 📝 创建模块文档
- ✨ 支持多种缓存策略
- 🔧 优化 SpEL 表达式解析