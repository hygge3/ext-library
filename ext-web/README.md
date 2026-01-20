# ext-web

> Web 层 - 提供 Web 开发相关功能，包括 MVC 增强、OpenAPI、SSE、WebSocket

## 简介

`ext-web` 是 ext-library 的 Web 层聚合模块，负责 Web 层的功能增强，提供统一的请求处理、API 文档、实时通信等能力。

## 子模块

| 模块 | 说明 | 主要依赖 |
|------|------|----------|
| [ext-mvc](ext-mvc/README.md) | Spring MVC 增强 | ext-core, ext-json, spring-webmvc |
| [ext-openapi](ext-openapi/README.md) | OpenAPI 文档支持 | ext-core, springdoc-openapi |
| [ext-sse](ext-sse/README.md) | Server-Sent Events | ext-redis, ext-security, ext-mvc |
| [ext-websocket](ext-websocket/README.md) | WebSocket 支持 | ext-redis, ext-security |

## 依赖关系

```
ext-common (ext-core, ext-json)
    │
    ├──> ext-mvc
    │       │
    │       └──> ext-sse (依赖 ext-mvc, ext-redis, ext-security)
    │
    └──> ext-openapi (依赖 ext-core)

ext-websocket (依赖 ext-redis, ext-security)
```

## 快速开始

### 引入 ext-mvc

```xml
<dependency>
    <groupId>ext.library</groupId>
    <artifactId>ext-mvc</artifactId>
</dependency>
```

### 引入 ext-openapi

```xml
<dependency>
    <groupId>ext.library</groupId>
    <artifactId>ext-openapi</artifactId>
</dependency>
```

### 引入 ext-sse

```xml
<dependency>
    <groupId>ext.library</groupId>
    <artifactId>ext-sse</artifactId>
</dependency>
```

### 引入 ext-websocket

```xml
<dependency>
    <groupId>ext.library</groupId>
    <artifactId>ext-websocket</artifactId>
</dependency>
```

## 许可证

[Apache License 2.0](../LICENSE)
