# ext-common

> 通用基础层 - 提供项目基础设施，包括工具类、核心功能和 JSON 处理

## 简介

`ext-common` 是 ext-library 的基础层聚合模块，整个项目的所有其他模块都直接或间接依赖此层。

## 子模块

| 模块 | 说明 | 主要依赖 |
|------|------|----------|
| [ext-tool](ext-tool/README.md) | 通用工具类库 | 无外部依赖 |
| [ext-core](ext-core/README.md) | 核心功能模块 | ext-tool, spring-validation |
| [ext-json](ext-json/README.md) | JSON 处理工具 | ext-tool, spring-boot-starter-json, json-path |
| [ext-http](ext-http/README.md) | HTTP 客户端封装 | ext-tool |
| [ext-useragent](ext-useragent/README.md) | User-Agent 解析器 | ext-tool |
| [ext-qrcode](ext-qrcode/README.md) | 二维码生成与识别 | ext-tool, zxing |
| ext-logback | 日志脱敏，Logback 脱敏转换器 | ext-json, logback-classic |

## 依赖关系

```
ext-tool (基础工具)
    │
    ├──> ext-core (核心功能)
    │
    ├──> ext-json (JSON处理)
    │
    ├──> ext-http
    │
    ├──> ext-useragent
    │
    └──> ext-qrcode (+ zxing)
```

## 快速开始

### 引入 ext-core (推荐)

如需完整的核心功能支持：

```xml
<dependency>
    <groupId>ext.library</groupId>
    <artifactId>ext-core</artifactId>
</dependency>
```

### 仅引入 ext-tool

如只需基础工具类：

```xml
<dependency>
    <groupId>ext.library</groupId>
    <artifactId>ext-tool</artifactId>
</dependency>
```

### 引入 ext-json

如需 JSON 处理能力：

```xml
<dependency>
    <groupId>ext.library</groupId>
    <artifactId>ext-json</artifactId>
</dependency>
```

### 引入独立工具模块

`ext-http` / `ext-useragent` / `ext-qrcode` / `ext-logback` 互不依赖，按需单独引入：

```xml
<dependency>
    <groupId>ext.library</groupId>
    <artifactId>ext-http</artifactId>
</dependency>
```

## 许可证

[Apache License 2.0](../LICENSE)
