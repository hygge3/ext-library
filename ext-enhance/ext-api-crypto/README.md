# ext-api-crypto

> API 加解密模块 - 提供请求解密和响应加密功能

## 简介

`ext-api-crypto` 是 ext-library 的 API 加解密模块，通过注解实现接口请求参数的自动解密和响应数据的自动加密。

## 快速开始

### Maven

```xml
<dependency>
    <groupId>ext.library</groupId>
    <artifactId>ext-api-crypto</artifactId>
</dependency>
```

### Gradle

```groovy
implementation("ext.library:ext-api-crypto")
```

## 依赖说明

| 依赖 | 说明 |
|------|------|
| ext-json | JSON 处理 |
| ext-crypto | 加密工具 |

## 功能特性

- 请求参数自动解密
- 响应数据自动加密
- 支持多种加密算法
- 基于注解，无侵入
- 支持方法级和类级注解

## 配置项

| 配置项 | 默认值 | 说明 |
|-------|-------|------|
| ext.api-crypto.algorithm | RSA | 默认加密算法 |
| ext.api-crypto.public-key | - | RSA/SM2 公钥（用于加密） |
| ext.api-crypto.private-key | - | RSA/SM2 私钥（用于解密） |
| ext.api-crypto.secret-key | - | AES/SM4 对称密钥 |
| ext.api-crypto.salt | - | AES 盐值 |

## 核心注解

| 注解 | 说明 |
|------|------|
| `@RequestDecrypt` | 请求解密注解 |
| `@ResponseEncrypt` | 响应加密注解 |

### 注解属性

| 属性 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| algorithm | Algorithm | RSA | 指定加密算法 |
| useDefault | boolean | true | 是否使用配置文件中的默认算法 |

## 支持的加密算法

| 算法 | 类型 | 说明 |
|------|------|------|
| RSA | 非对称 | RSA 加密，推荐用于安全性要求高的场景 |
| SM2 | 非对称 | 国密 SM2 加密 |
| AES | 对称 | AES 对称加密，支持盐值 |
| SM4 | 对称 | 国密 SM4 加密 |
| BASE64 | 编码 | Base64 编码（非加密，仅编解码） |

## 使用示例

### 配置密钥

```yaml
ext:
  api-crypto:
    algorithm: RSA
    public-key: "MIGfMA0GCSqGSIb3DQEBAQUAA4..."
    private-key: "MIICdQIBADANBgkqhkiG9w0BAQ..."
```

### 对称加密配置

```yaml
ext:
  api-crypto:
    algorithm: AES
    secret-key: "your-secret-key-here"
    salt: "your-salt-here"
```

### 请求解密

```java
@RestController
@RequestMapping("/api")
public class UserController {

    @PostMapping("/login")
    @RequestDecrypt
    public R<LoginVO> login(@RequestBody LoginDTO dto) {
        // dto 已自动解密
        return R.ok(authService.login(dto));
    }
}
```

### 响应加密

```java
@GetMapping("/user/info")
@ResponseEncrypt
public R<UserVO> getUserInfo() {
    // 返回的数据会自动加密
    return R.ok(userService.getCurrentUser());
}
```

### 同时使用

```java
@PostMapping("/sensitive")
@RequestDecrypt
@ResponseEncrypt
public R<SensitiveVO> handleSensitive(@RequestBody SensitiveDTO dto) {
    // 请求解密，响应加密
    return R.ok(sensitiveService.handle(dto));
}
```

### 指定加密算法

```java
@PostMapping("/aes")
@RequestDecrypt(algorithm = Algorithm.AES, useDefault = false)
@ResponseEncrypt(algorithm = Algorithm.AES, useDefault = false)
public R<DataVO> handleAes(@RequestBody DataDTO dto) {
    return R.ok(dataService.handle(dto));
}
```

### 类级注解

```java
@RestController
@RequestMapping("/api/secure")
@RequestDecrypt
@ResponseEncrypt
public class SecureController {

    // 该类下所有方法都会自动解密请求、加密响应

    @PostMapping("/data")
    public R<DataVO> handleData(@RequestBody DataDTO dto) {
        return R.ok(dataService.handle(dto));
    }
}
```

## 包结构

```
ext.library.apicrypto
├── annotation          # 注解定义
│   ├── RequestDecrypt
│   └── ResponseEncrypt
├── config              # 自动配置
│   └── ApiCryptoAutoConfiguration
├── enums               # 枚举
│   └── Algorithm
├── handler             # 处理器
│   ├── RequestDecryptHandler
│   └── ResponseEncryptHandler
├── properties          # 配置属性
│   └── ApiCryptoProperties
└── strategy            # 加密策略
    ├── CryptoStrategy
    ├── AESStrategy
    ├── RSAStrategy
    ├── SM2Strategy
    ├── SM4Strategy
    └── Base64Strategy
```

## 许可证

[Apache License 2.0](../../LICENSE)
