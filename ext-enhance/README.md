# ext-enhance

> 业务增强层 - 提供验证码、幂等性、脱敏、翻译、API加密等业务增强功能

## 简介

`ext-enhance` 是 ext-library 的业务增强层聚合模块，提供常见业务场景的增强功能，通过注解和 AOP 实现无侵入式的功能扩展。

## 子模块

| 模块 | 说明 | 主要依赖 |
|------|------|----------|
| [ext-captcha](ext-captcha/README.md) | 图形验证码生成与校验 | ext-cache |
| [ext-idempotent](ext-idempotent/README.md) | 接口幂等性控制 | ext-redis, caffeine (可选) |
| [ext-desensitize](ext-desensitize/README.md) | 数据脱敏 | ext-tool, jackson |
| [ext-trans](ext-trans/README.md) | 字段翻译/转换 | jackson |
| [ext-api-crypto](ext-api-crypto/README.md) | API 请求/响应加解密 | ext-json, ext-crypto |

## 依赖关系

```
ext-cache
    │
    └──> ext-captcha

ext-redis
    │
    └──> ext-idempotent

ext-tool
    │
    └──> ext-desensitize

jackson-databind
    │
    └──> ext-trans

ext-json + ext-crypto
    │
    └──> ext-api-crypto
```

## 快速开始

### 引入 ext-captcha

```xml
<dependency>
    <groupId>ext.library</groupId>
    <artifactId>ext-captcha</artifactId>
</dependency>
```

### 引入 ext-idempotent

```xml
<dependency>
    <groupId>ext.library</groupId>
    <artifactId>ext-idempotent</artifactId>
</dependency>
```

### 引入 ext-desensitize

```xml
<dependency>
    <groupId>ext.library</groupId>
    <artifactId>ext-desensitize</artifactId>
</dependency>
```

### 引入 ext-trans

```xml
<dependency>
    <groupId>ext.library</groupId>
    <artifactId>ext-trans</artifactId>
</dependency>
```

### 引入 ext-api-crypto

```xml
<dependency>
    <groupId>ext.library</groupId>
    <artifactId>ext-api-crypto</artifactId>
</dependency>
```

## 许可证

[Apache License 2.0](../LICENSE)
