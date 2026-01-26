[根目录](../CLAUDE.md) > **ext-web**

# ext-web - Web 层

> 提供 Web 开发相关功能，包括 MVC 增强、OpenAPI、SSE、WebSocket

## 层级职责

ext-web 负责 Web 层的功能增强，提供统一的请求处理、API 文档、实时通信等能力。

## 子模块列表

| 模块 | 描述 | 主要依赖 |
|------|------|----------|
| ext-mvc | Spring MVC 增强，自定义验证器、异常处理 | ext-core, ext-json, spring-webmvc |
| ext-openapi | OpenAPI/Swagger 文档支持 | ext-core, springdoc-openapi |
| ext-sse | Server-Sent Events 服务端推送 | ext-redis, ext-security, ext-mvc |
| ext-websocket | WebSocket 双向通信支持 | ext-redis, ext-security, spring-websocket |

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

### ext-openapi

OpenAPI 文档模块，提供：

- SpringDoc OpenAPI 集成
- Swagger UI 支持
- JavaDoc 运行时读取 (therapi-runtime-javadoc)
- API 文档自动生成

**包结构**: `ext.library.openapi`

### ext-sse

Server-Sent Events 模块，提供：

- `SseController` - SSE 控制器
- `SseEmitterManager` - SSE 连接管理
- `SseTopicListener` - 主题监听器
- 跨服务器消息推送 (通过 Redis)

**包结构**: `ext.library.sse`

### ext-websocket

WebSocket 模块，提供：

- `WebSocketSessionHolder` - 会话管理
- `WebSocketConstants` - 常量定义
- 认证集成 (依赖 ext-security)
- 跨服务器消息推送 (通过 Redis)

**包结构**: `ext.library.websocket`

## 依赖关系

```
ext-common (ext-core, ext-json)
    |
    +---> ext-mvc
              |
              +---> ext-sse (依赖 ext-mvc, ext-redis, ext-security)

ext-openapi (依赖 ext-core)

ext-websocket (依赖 ext-redis, ext-security)
```

## 相关文件

- `ext-mvc/src/main/java/ext/library/web/` - MVC 增强源码
- `ext-openapi/src/main/java/ext/library/openapi/` - OpenAPI 源码
- `ext-sse/src/main/java/ext/library/sse/` - SSE 源码
- `ext-websocket/src/main/java/ext/library/websocket/` - WebSocket 源码

## 变更记录

| 日期 | 变更内容 |
|------|----------|
| 2026-01-19 | 初始化 CLAUDE.md 文档 |
