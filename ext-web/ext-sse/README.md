# ext-sse（SSE 服务器推送）

## 功能

- SSE（Server-Sent Events）服务器推送支持
- 基于 Redis 的分布式连接管理
- 主题订阅机制
- 自动重连和连接管理
- 安全的认证授权集成

## 依赖引用

### Maven

```xml
<dependency>
    <groupId>ext.library</groupId>
    <artifactId>ext-sse</artifactId>
    <version>${version}</version>
</dependency>
```

### Gradle

```groovy
compile("ext.library:ext-sse:${version}")
```

## 依赖模块

ext-sse 依赖以下模块：
- ext-redis：连接管理和分布式支持
- ext-security：认证授权
- ext-web：Web 基础功能

## 配置项

| 配置项 | 默认值 | 说明 |
|-------|-------|------|
| ext.sse.timeout | 30分钟 | SSE 连接超时时间 |
| ext.sse.max-connections | 10000 | 最大连接数 |
| ext.sse.redis-topic | sse:topic | Redis 主题前缀 |

## 核心类说明

| 类名 | 说明 |
|-----|------|
| SseEmitterManager | SSE 连接管理器 |
| SseUtil | SSE 工具类 |
| SseController | SSE 控制器 |
| SseMessage | SSE 消息实体 |
| SseTopicListener | 主题监听器 |

## 使用示例

### 基础 SSE 连接

```java
@Autowired
private SseEmitterManager sseEmitterManager;

@GetMapping("/sse/connect")
public SseEmitter connect() {
    return sseEmitterManager.createEmitter();
}
```

### 发送消息到客户端

```java
@Autowired
private SseUtil sseUtil;

public void sendMessage(String userId, String data) {
    sseUtil.sendToUser(userId, "message", data);
}
```

### 主题广播

```java
@Autowired
private SseUtil sseUtil;

public void broadcast(String topic, String data) {
    sseUtil.sendToTopic(topic, "message", data);
}
```
