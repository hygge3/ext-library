# ext-infra

> 基础设施层 - 提供 Redis、缓存、邮件等基础设施集成

## 简介

`ext-infra` 是 ext-library 的基础设施层聚合模块，负责与外部基础设施的集成，为上层业务模块提供数据缓存、消息通知等能力。

## 子模块

| 模块 | 说明 | 主要依赖 |
|------|------|----------|
| [ext-redis](ext-redis/README.md) | Redis 操作封装 | ext-core, ext-json, spring-data-redis |
| [ext-cache](ext-cache/README.md) | 多级缓存支持 | ext-redis, caffeine |
| [ext-mail](ext-mail/README.md) | 邮件发送服务 | spring-boot-starter-mail |

## 依赖关系

```
ext-common (ext-core, ext-json)
    │
    └──> ext-redis
             │
             └──> ext-cache (依赖 ext-redis)

ext-mail (独立，仅依赖 spring-boot-starter-mail)
```

## 快速开始

### 引入 ext-redis

```xml
<dependency>
    <groupId>ext.library</groupId>
    <artifactId>ext-redis</artifactId>
</dependency>
```

### 引入 ext-cache

```xml
<dependency>
    <groupId>ext.library</groupId>
    <artifactId>ext-cache</artifactId>
</dependency>
```

### 引入 ext-mail

```xml
<dependency>
    <groupId>ext.library</groupId>
    <artifactId>ext-mail</artifactId>
</dependency>
```

## 许可证

[Apache License 2.0](../LICENSE)
