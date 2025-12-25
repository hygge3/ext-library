# ext-websocket（WebSocket 实时通信）

## 功能

- WebSocket 连接管理
- 分布式 Session 管理
- 消息广播
- 主题订阅
- 安全认证集成
- 拦截器支持
- 心跳检测

## 依赖引用

### Maven

```xml
<dependency>
    <groupId>ext.library</groupId>
    <artifactId>ext-websocket</artifactId>
    <version>${version}</version>
</dependency>
```

### Gradle

```groovy
compile("ext.library:ext-websocket:${version}")
```

## 依赖模块

ext-websocket 依赖以下模块：
- ext-redis：分布式 Session 管理
- ext-security：认证授权

## 配置项

| 配置项 | 默认值 | 说明 |
|-------|-------|------|
| ext.websocket.endpoint | /ws | WebSocket 端点 |
| ext.websocket.allowed-origins | * | 允许的跨域源 |
| ext.websocket.heartbeat-interval | 25秒 | 心跳间隔 |
| ext.websocket.session-timeout | 30分钟 | Session 超时 |

## 核心类说明

| 类名 | 说明 |
|-----|------|
| ExtWebSocketHandler | WebSocket 处理器 |
| WebSocketSessionHolder | Session 管理 |
| WebSocketMessage | WebSocket 消息 |
| WebSocketProperties | 配置属性 |
| WebSocketTopicListener | 主题监听器 |
| ExtWebSocketInterceptor | 拦截器 |
| WebSocketConstants | 常量定义 |

## 使用示例

### 建立连接

```javascript
const socket = new WebSocket('ws://localhost:8080/ws');

// 连接成功
socket.onopen = function() {
    console.log('WebSocket 连接成功');
};

// 接收消息
socket.onmessage = function(event) {
    const message = JSON.parse(event.data);
    console.log('收到消息:', message);
};

// 连接关闭
socket.onclose = function() {
    console.log('WebSocket 连接关闭');
};

// 错误处理
socket.onerror = function(error) {
    console.error('WebSocket 错误:', error);
};
```

### 发送消息

```javascript
// 发送文本消息
socket.send(JSON.stringify({
    type: 'chat',
    content: 'Hello, World!'
}));

// 发送心跳
setInterval(function() {
    if (socket.readyState === WebSocket.OPEN) {
        socket.send(JSON.stringify({
            type: 'heartbeat',
            timestamp: Date.now()
        }));
    }
}, 25000);
```

### 服务端处理

```java
@Component
@ServerEndpoint("/ws")
public class MyWebSocketHandler {

    @OnOpen
    public void onOpen(Session session) {
        String userId = getUserIdFromToken(session);
        WebSocketSessionHolder.addSession(userId, session);
    }

    @OnClose
    public void onClose(Session session) {
        String userId = getUserIdFromToken(session);
        WebSocketSessionHolder.removeSession(userId);
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        WebSocketMessage msg = JsonUtils.fromJson(message, WebSocketMessage.class);
        // 处理消息
    }

    @OnError
    public void onError(Session session, Throwable error) {
        log.error("WebSocket 错误", error);
    }
}
```

### 消息广播

```java
@Autowired
private WebSocketSessionHolder sessionHolder;

public void broadcast(String message) {
    sessionHolder.broadcast(message);
}
```

### 发送特定用户

```java
public void sendToUser(String userId, String message) {
    Session session = sessionHolder.getSession(userId);
    if (session != null && session.isOpen()) {
        session.getAsyncRemote().sendText(message);
    }
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

### 安全认证

```java
@Component
public class AuthInterceptor extends ExtWebSocketInterceptor {

    @Override
    public boolean beforeHandshake(ServerHttpRequest request,
                                   ServerHttpResponse response,
                                   WebSocketHandler wsHandler,
                                   Map<String, Object> attributes) {
        // 从 URL 参数或 Header 获取 Token
        String token = request.getHeaders().getFirst("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            // 验证 Token
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
