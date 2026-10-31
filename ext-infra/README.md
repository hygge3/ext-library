# ext-infra

> 基础设施层 - 提供 Redis、缓存、邮件等基础设施集成

## 简介

`ext-infra` 是 ext-library 的基础设施层聚合模块，负责与外部基础设施的集成，为上层业务模块提供数据缓存、消息通知等能力。

## 子模块

| 模块 | 说明 | 主要依赖 |
|------|------|----------|
| [ext-redis](ext-redis/README.md) | Redis 操作封装 | ext-core, ext-json, spring-data-redis |
| [ext-cache](ext-cache/README.md) | 多级缓存支持 | ext-redis, caffeine |
| [ext-postgres](ext-postgres/SPEC.md) | PostgreSQL 集成，缓存/队列/发布订阅/限流 | ext-core, ext-json, postgresql |
| [ext-lock](ext-lock/SPEC.md) | 分布式锁，注解式加锁（Redis / PostgreSQL 咨询锁） | ext-core；可选 ext-redis、ext-postgres |
| [ext-jdbc](ext-jdbc/SPEC.md) | JdbcClient 增强，注解驱动的轻量 DAO | ext-core, spring-boot-starter-jdbc |
| [ext-job](ext-job/SPEC.md) | 任务调度，基于 PostgreSQL 队列 | ext-postgres |
| [ext-mail](ext-mail/README.md) | 邮件发送服务 | spring-boot-starter-mail |
| ext-i18n | 国际化，资源文件与数据库双源翻译 | ext-core, ext-json, spring-boot-starter-jdbc(可选) |
| ext-oss | 对象存储，本地 / S3 / SFTP 三种后端 | ext-core；可选 AWS SDK v2、sshd-sftp |

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
