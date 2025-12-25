# ext-redis（Redis 集成）

## 功能

- Spring Data Redis 增强
- 键前缀自动管理
- 序列化优化
- 分布式锁支持
- 队列操作工具
- Redis 工具类

## 依赖引用

### Maven

```xml
<dependency>
    <groupId>ext.library</groupId>
    <artifactId>ext-redis</artifactId>
    <version>${version}</version>
</dependency>
```

### Gradle

```groovy
compile("ext.library:ext-redis:${version}")
```

## 依赖模块

ext-redis 依赖以下模块：
- ext-core：核心工具
- ext-json：JSON 处理

## 配置项

| 配置项 | 默认值 | 说明 |
|-------|-------|------|
| spring.data.redis.host | localhost | Redis 主机 |
| spring.data.redis.port | 6379 | Redis 端口 |
| spring.data.redis.password | - | Redis 密码 |
| ext.redis.prefix | 空 | 键前缀 |
| ext.redis.database | 0 | 数据库编号 |

## 核心类说明

| 类名 | 说明 |
|-----|------|
| RedisUtil | Redis 操作工具类 |
| DistributedLock | 分布式锁 |
| QueueUtil | 队列操作工具 |
| RedisProperties | Redis 配置属性 |
| CacheSerializer | 缓存序列化器 |
| PrefixStringRedisSerializer | 前缀字符串序列化器 |
| PrefixJdkRedisSerializer | 前缀 JDK 序列化器 |

## 使用示例

### 基本操作

```java
@Autowired
private RedisUtil redisUtil;

// String 操作
redisUtil.set("key", "value");
String value = redisUtil.get("key");

// Hash 操作
redisUtil.hset("hash", "field", "value");
Object value = redisUtil.hget("hash", "field");

// List 操作
redisUtil.lpush("list", "value");
String value = redisUtil.rpop("list");
```

### 分布式锁

```java
@Autowired
private DistributedLock lock;

public void doWithLock(String lockKey, long timeout, TimeUnit unit) {
    boolean acquired = lock.tryLock(lockKey, timeout, unit);
    if (acquired) {
        try {
            // 业务逻辑
        } finally {
            lock.unlock(lockKey);
        }
    }
}
```

### 键前缀

```java
@Autowired
private IRedisPrefixConverter prefixConverter;

public void example() {
    String prefix = prefixConverter.prefix("user:");
    // prefix = "project:user:"
    redisUtil.set(prefix + "1", "value");
}
```

### 队列操作

```java
@Autowired
private QueueUtil queueUtil;

// 生产
queueUtil.push("queue:task", task);

// 消费
Task task = queueUtil.pop("queue:task", Task.class);
```
