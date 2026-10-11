# ext-realtime

> 实时通信模块 - 提供 SSE 服务器推送与 WebSocket 双向通信

## 简介

`ext-realtime` 是 ext-library 的实时通信模块，包含两套实时通道实现，均支持分布式环境：

- **SSE**（`ext.library.sse.*`）：基于 Server-Sent Events 的单向服务器推送，实现见下方 SSE 章节
- **WebSocket**（`ext.library.websocket.*`）：全双工双向通信，实现见下方 WebSocket 章节

两者共享同一套分布式连接管理机制（Redis / PostgreSQL PubSub），配置前缀分别为 `ext.sse.*` 和 `ext.websocket.*`。

## 快速开始

### Maven

```xml
<dependency>
    <groupId>ext.library</groupId>
    <artifactId>ext-realtime</artifactId>
</dependency>
```

### Gradle

```groovy
implementation("ext.library:ext-realtime")
```

## 依赖说明

| 依赖 | 说明 |
|------|------|
| ext-security | 认证授权 |
| ext-mvc | Web 基础功能 |
| ext-redis（可选） | 分布式连接管理 |
| ext-postgres（可选） | 分布式连接管理 |
| spring-boot-starter-websocket | WebSocket 支持 |

---

# SSE（Server-Sent Events）

提供基于 Server-Sent Events 的单向服务器推送。

## SSE 配置项

| 配置项 | 默认值 | 说明 |
|-------|-------|------|
| ext.sse.timeout | 30m | SSE 连接超时时间 |
| ext.sse.max-connections | 10000 | 最大连接数 |
| ext.sse.redis-topic | sse:topic | Redis 主题前缀 |

## SSE 核心类

| 类名 | 说明 |
|------|------|
| `SseEmitterManager` | SSE 连接管理器 |
| `SseUtil` | SSE 工具类 |
| `SseController` | SSE 控制器 |
| `SseMessage` | SSE 消息实体 |
| `SseTopicListener` | 主题监听器 |

## SSE 使用示例

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

---

# WebSocket

提供完整的 WebSocket 实时双向通信解决方案。

## WebSocket 配置项

| 配置项 | 默认值 | 说明 |
|-------|-------|------|
| ext.websocket.endpoint | /ws | WebSocket 端点 |
| ext.websocket.allowed-origins | * | 允许的跨域源 |
| ext.websocket.heartbeat-interval | 25s | 心跳间隔 |
| ext.websocket.session-timeout | 30m | Session 超时 |

## WebSocket 核心类

| 类名 | 说明 |
|------|------|
| `ExtWebSocketHandler` | WebSocket 处理器 |
| `WebSocketSessionHolder` | Session 管理 |
| `WebSocketMessage` | WebSocket 消息实体 |
| `WebSocketProperties` | 配置属性 |
| `WebSocketTopicListener` | 主题监听器 |
| `ExtWebSocketInterceptor` | 拦截器 |

## WebSocket 使用示例

### 前端连接

```javascript
const socket = new WebSocket('ws://localhost:8080/ws');

socket.onopen = function() {
    console.log('WebSocket 连接成功');
};

socket.onmessage = function(event) {
    const message = JSON.parse(event.data);
    console.log('收到消息:', message);
};

socket.onclose = function() {
    console.log('WebSocket 连接关闭');
};

socket.onerror = function(error) {
    console.error('WebSocket 错误:', error);
};

// 发送消息
socket.send(JSON.stringify({
    type: 'chat',
    content: 'Hello, World!'
}));

// 心跳保活
setInterval(function() {
    if (socket.readyState === WebSocket.OPEN) {
        socket.send(JSON.stringify({
            type: 'heartbeat'
        }));
    }
}, 25000);
```

### 服务端消息广播

```java
@Autowired
private WebSocketSessionHolder sessionHolder;

// 广播所有用户
public void broadcast(String message) {
    sessionHolder.broadcast(message);
}

// 发送给特定用户
public void sendToUser(String userId, String message) {
    sessionHolder.sendToUser(userId, message);
}
```

### 主题订阅

```java
@Autowired
private WebSocketTopicListener topicListener;

// 订阅主题
topicListener.subscribe("topic:news", session);

// 取消订阅
topicListener.unsubscribe("topic:news", session);

// 发布消息到主题
topicListener.publish("topic:news", message);
```

### 安全认证拦截器

```java
@Component
public class AuthInterceptor extends ExtWebSocketInterceptor {

    @Override
    public boolean beforeHandshake(ServerHttpRequest request,
                                   ServerHttpResponse response,
                                   WebSocketHandler wsHandler,
                                   Map<String, Object> attributes) {
        String token = extractToken(request);
        if (token != null) {
            User user = securityService.validateToken(token);
            if (user != null) {
                attributes.put("user", user);
                return true;
            }
        }
        return false;
    }
}
```

## 许可证

[Apache License 2.0](../../LICENSE)