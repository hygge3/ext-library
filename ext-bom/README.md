# ext-bom

> BOM (Bill of Materials) - 依赖版本统一管理

## 简介

`ext-bom` 是 ext-library 的 BOM 模块，用于统一管理所有子模块及第三方依赖的版本号。通过引入此模块，项目可以避免版本冲突，确保依赖版本的一致性。

## 快速开始

### Maven 引入

在项目的 `pom.xml` 中添加以下依赖管理：

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>ext.library</groupId>
            <artifactId>ext-bom</artifactId>
            <version>2.1.0</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```

引入后，使用其他 ext 模块时无需指定版本号：

```xml
<dependencies>
    <dependency>
        <groupId>ext.library</groupId>
        <artifactId>ext-core</artifactId>
    </dependency>
    <dependency>
        <groupId>ext.library</groupId>
        <artifactId>ext-redis</artifactId>
    </dependency>
</dependencies>
```

## 管理的模块

### ext-common 层

| 模块 | 说明 |
|------|------|
| ext-tool | 通用工具类库 |
| ext-core | 核心功能模��� |
| ext-json | JSON 处理工具 |
| ext-servlet | servlet 基座（统一响应体 / 分页 / 错误信封） |
| ext-http | HTTP 客户端��装 |
| ext-useragent | User-Agent 解析 |
| ext-qrcode | 二维码生成与识别 |
| ext-logback | 日志脱敏 |

### ext-infra 层

| 模块 | 说明 |
|------|------|
| ext-redis | Redis 操作封装 |
| ext-cache | 多级缓存支持 |
| ext-mail | 邮件发送服务 |
| ext-postgres | PostgreSQL 集成（缓存 / 队列 / 发布订阅 / 限流） |
| ext-lock | 分布式锁 |
| ext-jdbc | JdbcClient 增强 |
| ext-job | 任务调度 |
| ext-i18n | 国际化 |
| ext-oss | 对象存储（本地 / S3 / SFTP） |

### 安全/加密层

| 模块 | 说明 |
|------|------|
| ext-security | 认证授权框架 |
| ext-crypto | 加密算法工具库 |

### ext-web 层

| 模块 | 说明 |
|------|------|
| ext-mvc | Spring MVC 增强（含 OpenAPI 文档） |
| ext-realtime | 实时通信，SSE 推送与 WebSocket |

### ext-enhance 层

| 模块 | 说明 |
|------|------|
| ext-captcha | 图形验证码 |
| ext-idempotent | 接口幂等性控制 |
| ext-api-crypto | API 加解密 |
| ext-ratelimit | 接口限流 |
| ext-audit | 操作审计 |

## 第三方依赖版本

| 依赖 | 版本 | 说明 |
|------|------|------|
| Spring Boot | 4.1.1 | 框架全家桶（BOM 引入） |
| Swagger | 2.2.55 | API 注解 |
| Bouncy Castle | 1.86 | 加密库 |
| SpringDoc | 3.1.1 | OpenAPI 文档 |
| ZXing | 3.5.4 | 二维码 |
| ZXing | 3.5.3 | 二维码生成 |
| Therapi JavaDoc | 0.15.0 | 运行时 JavaDoc |

## 许可证

[Apache License 2.0](../LICENSE)
