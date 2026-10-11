[根目录](../AGENTS.md) > **ext-web**

# ext-web - Web 层

> 提供 Web 开发相关功能，包括 MVC 增强与实时通信

## 层级职责

ext-web 负责 Web 层的功能增强，提供统一的请求处理、API 文档、实时通信等能力。

## 子模块列表

| 模块 | 描述 | 主要依赖 |
|------|------|----------|
| ext-mvc | Spring MVC 增强与 OpenAPI 文档，自定义验证器、异常处理 | ext-core, ext-json, springdoc-openapi, spring-webmvc |
| ext-realtime | 实时通信，SSE 服务端推送与 WebSocket 双向通信 | ext-security, ext-mvc, ext-redis, spring-websocket |

## 模块详情

### ext-mvc

Spring MVC 增强模块，提供：

**自定义验证约束**:
- `@Chinese` - 中文字符校验
- `@English` - 英文字符校验
- `@Cellphone` - 手机号校验
- `@ZipCode` - 邮编校验
- `@Username` - 用户名校验
- `@Xss` - XSS 攻击校验
- `@RangeIn` - 范围校验
- `@Exclusion` - 排除值校验
- `@Mutual` - 互斥字段校验

**验证分组**:
- `ValidationGroups` - 预定义验证分组

**消息插值**:
- `EmptyCurlyToDefaultMessageInterpolator` - 空大括号转默认消息

**包结构**: `ext.library.web`

### ext-realtime

实时通信模块，包含两套实时通道实现，均支持分布式环境（Redis / PostgreSQL PubSub）：

**SSE** (`ext.library.sse`)：
- `SseController` - SSE 控制器
- `SseEmitterManager` / `SseConnectionManager` - 连接管理
- `SseTopicListener` - 主题监听器
- `SseProperties` - 配置属性 (`ext.sse.*`)

**WebSocket** (`ext.library.websocket`)：
- `ExtWebSocketHandler` - WebSocket 处理器
- `WebSocketConnectionManager` - 会话管理
- `WebSocketTopicListener` - 主题监听器
- `ExtWebSocketInterceptor` - 握手拦截器（认证集成）
- `WebSocketProperties` - 配置属性 (`ext.websocket.*`)

## 依赖关系

```
ext-common (ext-core, ext-json)
    |
    +---> ext-mvc
              |
              +---> ext-realtime (依赖 ext-mvc, ext-security, ext-redis)
```

## 相关文件

- `ext-mvc/src/main/java/ext/library/web/` - MVC 增强源码
- `ext-mvc/src/main/java/ext/library/openapi/` - OpenAPI 源码
- `ext-realtime/src/main/java/ext/library/sse/` - SSE 源码
- `ext-realtime/src/main/java/ext/library/websocket/` - WebSocket 源码

## 变更记录

| 日期 | 变更内容 |
|------|----------|
| 2026-01-19 | 初始化 AGENTS.md 文档 |
| 2026-10-10 | ext-openapi 并入 ext-mvc，ext-sse 与 ext-websocket 合并为 ext-realtime |
