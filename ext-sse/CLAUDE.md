[根目录](../CLAUDE.md) > **ext-sse**

# ext-sse 模块文档

## 模块职责

ext-sse 提供 Server-Sent Events (SSE) 支持，用于服务端向客户端推送事件。

## 入口与启动

### 自动配置类
- **SseAutoConfig**: SSE 自动配置

## 核心组件

### 1. 配置类 (config/)
- **SseAutoConfig**: SSE 自动配置

### 2. 配置属性 (properties/)
- **SseProperties**: SSE 配置参数

### 3. 控制器 (controller/)
- **SseController**: SSE 控制器

### 4. 管理器 (manager/)
- **SseEmitterManager**: SseEmitter 管理器

### 5. 工具类 (util/)
- **SseUtil**: SSE 工具类

### 6. 领域对象 (domain/)
- **SseMessage**: SSE 消息

### 7. 监听器 (listener/)
- **SseTopicListener**: SSE 主题监听器

## 关键依赖

- **ext-redis**: Redis 支持
- **ext-security**: 安全支持
- **ext-web**: Web 支持

## 使用示例

### 发送 SSE 事件
```java
// 发送消息给指定用户
SseUtil.sendMessage(userId, message);

// 广播消息
SseUtil.broadcast(message);
```

### 创建 SSE 端点
```java
@GetMapping("/sse/{userId}")
public SseEmitter subscribe(@PathVariable String userId) {
    return SseEmitterManager.createEmitter(userId);
}
```

## 常见问题 (FAQ)

### Q: SSE 和 WebSocket 的区别？
- SSE 是单向的（服务端到客户端）
- WebSocket 是双向的
- SSE 基于HTTP，更容易实现

## 相关文件清单

### 主要源码文件
- `src/main/java/ext/library/sse/config/SseAutoConfig.java`
- `src/main/java/ext/library/sse/controller/SseController.java`
- `src/main/java/ext/library/sse/manager/SseEmitterManager.java`
- `src/main/java/ext/library/sse/util/SseUtil.java`

### 配置文件
- `src/main/resources/META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`

## 变更记录 (Changelog)

### 2025-12-24
- 创建：模块文档
