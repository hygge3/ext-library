# ext-web

> Web 层 - 提供 Web 开发相关功能，包括 MVC 增强与实时通信

## 简介

`ext-web` 是 ext-library 的 Web 层聚合模块，负责 Web 层的功能增强，提供统一的请求处理、API 文档、实时通信等能力。

## 子模块

| 模块 | 说明 | 主要依赖 |
|------|------|----------|
| [ext-mvc](ext-mvc/README.md) | Spring MVC 增强与 OpenAPI 文档 | ext-core, ext-json, springdoc-openapi, spring-webmvc |
| [ext-realtime](ext-realtime/README.md) | 实时通信，SSE 推送与 WebSocket | ext-security, ext-mvc, ext-redis, spring-websocket |

## 依赖关系

```
ext-common (ext-core, ext-json)
    │
    └──> ext-mvc
            │
            └──> ext-realtime (依赖 ext-mvc, ext-security, ext-redis)
```

## 快速开始

### 引入 ext-mvc

```xml
<dependency>
    <groupId>ext.library</groupId>
    <artifactId>ext-mvc</artifactId>
</dependency>
```

### 引入 ext-realtime

```xml
<dependency>
    <groupId>ext.library</groupId>
    <artifactId>ext-realtime</artifactId>
</dependency>
```

## 许可证

[Apache License 2.0](../LICENSE)