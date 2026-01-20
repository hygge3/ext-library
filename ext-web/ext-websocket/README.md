# ext-websocket

> WebSocket 模块 - 提供实时双向通信功能

## 简介

`ext-websocket` 是 ext-library 的 WebSocket 模块，提供完整的 WebSocket 实时通信解决方案，支持分布式环境。

## 快速开始

### Maven

```xml
<dependency>
    <groupId>ext.library</groupId>
    <artifactId>ext-websocket</artifactId>
</dependency>
```

### Gradle

```groovy
implementation("ext.library:ext-websocket")
```

## 依赖说明

| 依赖 | 说明 |
|------|------|
| ext-redis | 分布式 Session 管理 |
| ext-security | 认证授权 |
| spring-boot-starter-websocket | WebSocket 支持 |

## 功能特性

- WebSocket 连接管理
- 分布式 Session 管理
- 消息广播
- 主题订阅
- 安全认证集成
- 拦截器支持
- 心跳检测

## 配置项

| 配置项 | 默认值 | 说明 |
|-------|-------|------|
| ext.websocket.endpoint | /ws | WebSocket 端点 |
| ext.websocket.allowed-origins | * | 允许的跨域源 |
| ext.websocket.heartbeat-interval | 25s | 心跳间隔 |
| ext.websocket.session-timeout | 30m | Session 超时 |

## 核心类说明

| 类名 | 说明 |
|------|------|
| `ExtWebSocketHandler` | WebSocket 处理器 |
| `WebSocketSessionHolder` | Session 管理 |
| `WebSocketMessage` | WebSocket 消息 |
| `WebSocketProperties` | 配置属性 |
| `WebSocketTopicListener` | 主题监听器 |
| `ExtWebSocketInterceptor` | 拦截器 |
| `WebSocketConstants` | 常量定义 |

## 使用示例

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
