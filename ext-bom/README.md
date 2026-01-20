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
            <version>4.0.0</version>
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
| ext-core | 核心功能模块 |
| ext-json | JSON 处理工具 |

### ext-infra 层

| 模块 | 说明 |
|------|------|
| ext-redis | Redis 操作封装 |
| ext-cache | 多级缓存支持 |
| ext-mail | 邮件发送服务 |

### 安全/加密层

| 模块 | 说明 |
|------|------|
| ext-security | 认证授权框架 |
| ext-crypto | 加密算法工具库 |

### ext-web 层

| 模块 | 说明 |
|------|------|
| ext-mvc | Spring MVC 增强 |
| ext-openapi | OpenAPI 文档支持 |
| ext-sse | Server-Sent Events |
| ext-websocket | WebSocket 支持 |

### ext-enhance 层

| 模块 | 说明 |
|------|------|
| ext-captcha | 图形验证码 |
| ext-idempotent | 接口幂等性控制 |
| ext-desensitize | 数据脱敏 |
| ext-trans | 字段翻译转换 |
| ext-api-crypto | API 加解密 |

### ext-misc 层

| 模块 | 说明 |
|------|------|
| ext-http | HTTP 客户端封装 |
| ext-qrcode | 二维码生成 |
| ext-monitor | 系统监控信息 |

## 第三方依赖版本

| 依赖 | 版本 | 说明 |
|------|------|------|
| MapStruct Plus | 1.5.0 | 对象映射框架 |
| Swagger | 2.2.40 | API 注解 |
| Bouncy Castle | 1.83 | 加密库 |
| OSHI | 6.9.2 | 系统信息采集 |
| SpringDoc | 3.0.0 | OpenAPI 文档 |
| ZXing | 3.5.3 | 二维码生成 |
| Therapi JavaDoc | 0.15.0 | 运行时 JavaDoc |

## 许可证

[Apache License 2.0](../LICENSE)
