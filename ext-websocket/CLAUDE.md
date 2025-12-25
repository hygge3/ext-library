[根目录](../CLAUDE.md) > **ext-websocket**

# ext-websocket 模块文档

## 模块职责

ext-websocket 提供 WebSocket 支持，包括会话管理、Redis 发布订阅等功能。

## 入口与启动

### 自动配置类
- **WebSocketAutoConfig**: WebSocket 自动配置

## 核心组件

### 1. 配置类 (config/)
- **WebSocketAutoConfig**: WebSocket 自动配置

### 2. 配置属性 (properties/)
- **WebSocketProperties**: WebSocket 配置参数

### 3. 处理器 (handler/)
- **ExtWebSocketHandler**: WebSocket 处理器

### 4. 拦截器 (interceptor/)
- **ExtWebSocketInterceptor**: WebSocket 拦截器

### 5. 持有器 (holder/)
- **WebSocketSessionHolder**: WebSocket 会话持有器

### 6. 工具类 (util/)
- **WebSocketUtil**: WebSocket 工具类

### 7. 领域对象 (domain/)
- **WebSocketMessage**: WebSocket 消息

### 8. 监听器 (listener/)
- **WebSocketTopicListener**: WebSocket 主题监听器

### 9. 常量 (constant/)
- **WebSocketConstants**: WebSocket 常量

## 关键依赖

- **ext-redis**: Redis 支持
- **ext-security**: 安全支持
- **spring-boot-starter-websocket**: WebSocket 支持

## 使用示例

### 发送消息
```java
// 发送消息给指定用户
WebSocketUtil.sendMessage(userId, message);

// 广播消息
WebSocketUtil.broadcast(message);

// 发送消息给指定会话
WebSocketUtil.sendMessage(sessionId, message);
```

### 会话管理
```java
// 获取在线用户数
int count = WebSocketSessionHolder.getOnlineCount();

// 获取所有在线用户
Set<String> users = WebSocketSessionHolder.getOnlineUsers();

// 移除会话
WebSocketSessionHolder.removeSession(sessionId);
```

## 常见问题 (FAQ)

### Q: 如何实现分布式 WebSocket？
使用 Redis 发布订阅功能，通过 `WebSocketTopicListener` 实现。

### Q: 如何处理会话超时？
通过 `ExtWebSocketInterceptor` 拦截器处理。

## 相关文件清单

### 主要源码文件
- `src/main/java/ext/library/websocket/config/WebSocketAutoConfig.java`
- `src/main/java/ext/library/websocket/handler/ExtWebSocketHandler.java`
- `src/main/java/ext/library/websocket/util/WebSocketUtil.java`
- `src/main/java/ext/library/websocket/holder/WebSocketSessionHolder.java`

### 配置文件
- `src/main/resources/META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`

## 变更记录 (Changelog)

### 2025-12-24
- 创建：模块文档
