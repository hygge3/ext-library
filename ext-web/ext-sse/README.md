# ext-sse

> Server-Sent Events 模块 - 提供服务器推送功能

## 简介

`ext-sse` 是 ext-library 的 SSE 模块，提供基于 Server-Sent Events 的服务器推送功能，支持分布式环境。

## 快速开始

### Maven

```xml
<dependency>
    <groupId>ext.library</groupId>
    <artifactId>ext-sse</artifactId>
</dependency>
```

### Gradle

```groovy
implementation("ext.library:ext-sse")
```

## 依赖说明

| 依赖 | 说明 |
|------|------|
| ext-redis | 分布式连接管理 |
| ext-security | 认证授权 |
| ext-mvc | Web 基础功能 |

## 功能特性

- SSE 服务器推送支持
- 基于 Redis 的分布式连接管理
- 主题订阅机制
- 自动重连和连接管理
- 安全的认证授权集成

## 配置项

| 配置项 | 默认值 | 说明 |
|-------|-------|------|
| ext.sse.timeout | 30m | SSE 连接超时时间 |
| ext.sse.max-connections | 10000 | 最大连接数 |
| ext.sse.redis-topic | sse:topic | Redis 主题前缀 |

## 核心类说明

| 类名 | 说明 |
|------|------|
| `SseEmitterManager` | SSE 连接管理器 |
| `SseUtil` | SSE 工具类 |
| `SseController` | SSE 控制器 |
| `SseMessage` | SSE 消息实体 |
| `SseTopicListener` | 主题监听器 |

## 使用示例

### 建立 SSE 连接

```java
@Autowired
private SseEmitterManager sseEmitterManager;

@GetMapping("/sse/connect")
public SseEmitter connect() {
    return sseEmitterManager.createEmitter();
}
```

### 发送消息到用户

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

### 前端连接

```javascript
const eventSource = new EventSource('/sse/connect');

eventSource.onopen = function() {
    console.log('SSE 连接已建立');
};

eventSource.onmessage = function(event) {
    console.log('收到消息:', event.data);
};

eventSource.onerror = function(error) {
    console.error('SSE 错误:', error);
};

// 监听自定义事件
eventSource.addEventListener('notification', function(event) {
    console.log('收到通知:', event.data);
});
```

## 许可证

[Apache License 2.0](../../LICENSE)
