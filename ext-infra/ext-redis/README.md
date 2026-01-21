# ext-redis

> Redis 集成模块 - 提供 Spring Data Redis 增强和分布式锁支持

## 简介

`ext-redis` 是 ext-library 的 Redis 集成模块，基于 Spring Data Redis 提供增强功能，包括键前缀管理、分布式锁、队列操作等。

## 快速开始

### Maven

```xml
<dependency>
    <groupId>ext.library</groupId>
    <artifactId>ext-redis</artifactId>
</dependency>
```

### Gradle

```groovy
implementation("ext.library:ext-redis")
```

## 依赖说明

| 依赖 | 说明 |
|------|------|
| ext-core | 核心工具类 |
| ext-json | JSON 序列化 |
| spring-boot-starter-data-redis | Redis 集成 |
| commons-pool2 | 连接池支持 |

## 功能特性

- Spring Data Redis 增强
- 键前缀自动管理
- JSON 序列化优化
- 分布式锁支持
- 队列操作工具
- Redis 工具类

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
|------|------|
| `RedisUtil` | Redis 操作工具类 |
| `DistributedLock` | 分布式锁 |
| `QueueUtil` | 队列操作工具 |
| `RedisProperties` | Redis 配置属性 |
| `CacheSerializer` | 缓存序列化器 |
| `PrefixStringRedisSerializer` | 前缀字符串序列化器 |
| `PrefixJdkRedisSerializer` | 前缀 JDK 序列化器 |

## 使用示例

### 基本操作

```java
// RedisUtil 是静态工具类，直接调用即可

// String 操作
RedisUtil.set("key", "value");
String value = RedisUtil.get("key");

// 带过期时间
RedisUtil.set("key", "value", Duration.ofSeconds(60));

// Hash 操作
RedisUtil.hashOps().put("hash", "field", "value");
Object value = RedisUtil.hashOps().get("hash", "field");

// List 操作
RedisUtil.listOps().leftPush("list", "value");
String value = RedisUtil.listOps().rightPop("list");
```

### 分布式锁

```java
// 基础用法
DistributedLock lock = new DistributedLock("myLockName");
if (lock.tryLock()) {
    try {
        // 业务逻辑
    } finally {
        lock.unlock();
    }
}

// 带超时的锁获取
DistributedLock lock = new DistributedLock("myLockName", Duration.ofSeconds(30));
if (lock.tryLock(Duration.ofSeconds(10))) {
    try {
        // 业务逻辑
    } finally {
        lock.unlock();
    }
}

// 推荐：使用 try-with-resources (实现了 AutoCloseable)
try (DistributedLock lock = new DistributedLock("myLockName")) {
    lock.lock();
    // 业务逻辑，锁会在 try 块结束时自动释放
}
```

### 自定义键前缀转换器

```java
// 实现 RedisPrefixConverter 接口以自定义前缀逻辑
@Component
public class MyPrefixConverter implements RedisPrefixConverter {

    @Override
    public String prefix() {
        return "myapp:";  // 自定义前缀
    }

    @Override
    public boolean enabled() {
        return true;  // 启用前缀
    }
}
```

### 队列操作

```java
// QueueUtil 是静态工具类

// 普通队列
QueueUtil.producer("queue:task", "taskData");
String data = QueueUtil.consumer("queue:task");

// 延迟队列 (需要 Redis 5.0+)
QueueUtil.delayedProducer("delayed:task", "taskData", 60);  // 60 秒后可消费
QueueUtil.delayedProducer("delayed:task", "taskData", Duration.ofMinutes(5));  // 5 分钟后
String delayedData = QueueUtil.delayedConsumer("delayed:task");

// 删除和销毁
QueueUtil.remove("queue:task", "taskData");
QueueUtil.destroy("queue:task");
```

## 许可证

[Apache License 2.0](../../LICENSE)
