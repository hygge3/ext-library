[根目录](../CLAUDE.md) > **ext-redis**

# ext-redis 模块文档

## 模块职责

ext-redis 提供 Redis 集成和工具类，包括自定义序列化、Key 前缀支持、分布式锁等功能。

## 入口与启动

### 自动配置类
- **RedisAutoConfig**: Redis 模块自动配置
  - 配置 RedisTemplate
  - 配置 StringRedisTemplate
  - 配置 RedisMessageListenerContainer

## 核心组件

### 1. 配置类 (config/)
- **RedisAutoConfig**: Redis 自动配置
  - 自定义序列化器配置
  - Key 前缀转换器配置

### 2. 配置属性 (properties/)
- **RedisProperties**: Redis 配置参数
  - Key 前缀配置

### 3. 工具类 (util/)
- **RedisUtil**: Redis 操作工具类
- **QueueUtil**: Redis 队列工具类
- **DistributedLock**: 分布式锁实现

### 4. 序列化 (serialize/)
- **PrefixJdkRedisSerializer**: 带前缀的 JDK 序列化
- **PrefixStringRedisSerializer**: 带前缀的 String 序列化
- **CacheSerializer**: 缓存序列化器

### 5. 前缀处理 (prefix/)
- **IRedisPrefixConverter**: Redis Key 前缀转换器接口
- **DefaultRedisPrefixConverter**: 默认前缀转换器实现

## 关键依赖

- **ext-core**: Spring 基础支持
- **ext-json**: JSON 处理支持
- **spring-boot-starter-data-redis**: Redis 支持
- **commons-pool2**: 连接池支持

## 使用示例

### 基本使用
```java
@Autowired
private RedisUtil redisUtil;

// 设置值
redisUtil.set("key", "value");

// 获取值
Object value = redisUtil.get("key");

// 删除值
redisUtil.del("key");
```

### 分布式锁
```java
@Autowired
private DistributedLock distributedLock;

// 获取锁
boolean locked = distributedLock.lock("lock:key", 30);

// 释放锁
distributedLock.unlock("lock:key");
```

### Key 前缀配置
```yaml
ext:
  redis:
    key-prefix: "app:"
```

## 常见问题 (FAQ)

### Q: 如何自定义 Key 前缀？
实现 `IRedisPrefixConverter` 接口并注册为 Bean。

### Q: 如何使用分布式锁？
```java
DistributedLock lock = SpringUtil.getBean(DistributedLock.class);
try {
    if (lock.lock("key", 30)) {
        // 业务逻辑
    }
} finally {
    lock.unlock("key");
}
```

## 相关文件清单

### 主要源码文件
- `src/main/java/ext/library/redis/config/RedisAutoConfig.java`
- `src/main/java/ext/library/redis/util/RedisUtil.java`
- `src/main/java/ext/library/redis/util/DistributedLock.java`
- `src/main/java/ext/library/redis/serialize/PrefixJdkRedisSerializer.java`

### 配置文件
- `src/main/resources/META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`

## 变更记录 (Changelog)

### 2025-12-24
- 创建：模块文档
