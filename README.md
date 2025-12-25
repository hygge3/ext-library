# ext-library（SpringBoot 功能扩展库）

[![zread](https://img.shields.io/badge/Ask_Zread-_.svg?style=flat&color=00b0aa&labelColor=000000&logo=data%3Aimage%2Fsvg%2Bxml%3Bbase64%2CPHN2ZyB3aWR0aD0iMTYiIGhlaWdodD0iMTYiIHZpZXdCb3g9IjAgMCAxNiAxNiIgZmlsbD0ibm9uZSIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj4KPHBhdGggZD0iTTQuOTYxNTYgMS42MDAxSDIuMjQxNTZDMS44ODgxIDEuNjAwMSAxLjYwMTU2IDEuODg2NjQgMS42MDE1NiAyLjI0MDFWNC45NjAxQzEuNjAxNTYgNS4zMTM1NiAxLjg4ODEgNS42MDAxIDIuMjQxNTYgNS42MDAxSDQuOTYxNTZDNS4zMTUwMiA1LjYwMDEgNS42MDE1NiA1LjMxMzU2IDUuNjAxNTYgNC45NjAxVjIuMjQwMUM1LjYwMTU2IDEuODg2NjQgNS4zMTUwMiAxLjYwMDEgNC45NjE1NiAxLjYwMDFaIiBmaWxsPSIjZmZmIi8%2BCjxwYXRoIGQ9Ik00Ljk2MTU2IDEwLjM5OTlIMi4yNDE1NkMxLjg4ODEgMTAuMzk5OSAxLjYwMTU2IDEwLjY4NjQgMS42MDE1NiAxMS4wMzk5VjEzLjc1OTlDMS42MDE1NiAxNC4xMTM0IDEuODg4MSAxNC4zOTk5IDIuMjQxNTYgMTQuMzk5OUg0Ljk2MTU2QzUuMzE1MDIgMTQuMzk5OSA1LjYwMTU2IDE0LjExMzQgNS42MDE1NiAxMy43NTk5VjExLjAzOTlDNS42MDE1NiAxMC42ODY0IDUuMzE1MDIgMTAuMzk5OSA0Ljk2MTU2IDEwLjM5OTlaIiBmaWxsPSIjZmZmIi8%2BCjxwYXRoIGQ9Ik0xMy43NTg0IDEuNjAwMUgxMS4wMzg0QzEwLjY4NSAxLjYwMDEgMTAuMzk4NCAxLjg4NjY0IDEwLjM5ODQgMi4yNDAxVjQuOTYwMUMxMC4zOTg0IDUuMzEzNTYgMTAuNjg1IDUuNjAwMSAxMS4wMzg0IDUuNjAwMUgxMy43NTg0QzE0LjExMTkgNS42MDAxIDE0LjM5ODQgNS4zMTM1NiAxNC4zOTg0IDQuOTYwMVYyLjI0MDFDMTQuMzk4NCAxLjg4NjY0IDE0LjExMTkgMS42MDAxIDEzLjc1ODQgMS42MDAxWiIgZmlsbD0iI2ZmZiIvPgo8cGF0aCBkPSJNNCAxMkwxMiA0TDQgMTJaIiBmaWxsPSIjZmZmIi8%2BCjxwYXRoIGQ9Ik00IDEyTDEyIDQiIHN0cm9rZT0iI2ZmZiIgc3Ryb2tlLXdpZHRoPSIxLjUiIHN0cm9rZS1saW5lY2FwPSJyb3VuZCIvPgo8L3N2Zz4K&logoColor=ffffff)](https://zread.ai/hygge3/ext-library)
[![Codacy Badge](https://app.codacy.com/project/badge/Grade/6eafae2ee8d24d80a0d8c7994d91d7ac)](https://app.codacy.com/gh/hygge3/ext-library/dashboard?utm_source=gh&utm_medium=referral&utm_content=&utm_campaign=Badge_grade)
![Version](https://img.shields.io/badge/version-4.0.0-green.svg)
![JAVA 25](https://img.shields.io/badge/JDK-25-brightgreen.svg)
![Spring Boot](https://img.shields.io/github/v/release/spring-projects/spring-boot?label=version)
![GitHub last commit](https://img.shields.io/github/last-commit/hygge3/ext-library)
![GitHub code size in bytes](https://img.shields.io/github/languages/code-size/hygge3/ext-library)

## 介绍

ext-library，一个全面的 Spring Boot 扩展库，旨在简化和提升您的开发体验。基于 Spring Boot 4 和 JDK 25 构建，这个开源框架提供了一系列模块化组件，用于解决常见的开发挑战，让您能够专注于业务逻辑而非样板代码。

ext-library 是一个精心设计的扩展框架，以简洁直观的设计集成了强大的实用工具和功能。您可以将其视为 Spring Boot 开发者的瑞士军刀——无需为不同功能搜索和集成多个库，ext-library 提供了具有统一 API 和设计模式的整体解决方案。

## 模块列表

该库采用模块化架构，每个 ext-* 模块都针对特定领域或功能。这种方法让您可以按需选择组件，保持应用程序的轻量化和专注性。

| 模块 | 功能描述 |
|------|---------|
| ext-tool | 通用工具类，提供常见编程任务和实用工具 |
| ext-core | 核心实用工具和配置，作为基础依赖 |
| ext-json | JSON 处理，增强的 JSON 操作 |
| ext-redis | Redis 集成，Spring Data Redis 增强 |
| ext-cache | 缓存抽象，统一的缓存接口 |
| ext-security | 安全框架，认证授权支持 |
| ext-captcha | 验证码生成和验证 |
| ext-crypto | 加密操作，加密/解密功能 |
| ext-desensitize | 数据脱敏，敏感信息保护 |
| ext-interface-crypto | 接口加解密，请求解密响应加密 |
| ext-http | HTTP 客户端增强 |
| ext-idempotent | 幂等处理，防止重复请求 |
| ext-mail | 邮件发送支持 |
| ext-monitor | 系统监控，性能指标采集 |
| ext-openapi | OpenAPI 文档生成 |
| ext-qrcode | 二维码生成和识别 |
| ext-sse | SSE 服务器推送 |
| ext-trans | 分布式事务支持 |
| ext-web | Web 增强，MVC 工具和验证 |
| ext-websocket | WebSocket 实时通信 |

## 快速开始

### Maven 引入

在父 pom 中声明：

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>ext.library</groupId>
            <artifactId>ext-parent</artifactId>
            <version>4.0.0</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```

然后在子模块中引入即可：

```xml
<dependencies>
    <dependency>
        <groupId>ext.library</groupId>
        <artifactId>ext-core</artifactId>
    </dependency>
</dependencies>
```

### Gradle 引入

```groovy
dependencies {
    implementation 'ext.library:ext-core:4.0.0'
}
```

## 为什么选择 ext-library？

- **模块化设计**：只使用您需要的，保持应用程序轻量化
- **一致的 API**：所有模块都遵循相同的设计模式和约定
- **性能优化**：以性能为核心构建，利用最新的 Java 功能
- **生产就绪**：经过充分测试并在生产环境中使用
- **积极开发**：持续更新新功能和改进
- **全面文档**：所有模块都有详细的文档和示例

## 许可证

本项目采用 Apache License 2.0 许可证。

## 联系方式

- 项目地址：https://hygge.eu.org
- GitHub：https://github.com/hygge3/ext-library
