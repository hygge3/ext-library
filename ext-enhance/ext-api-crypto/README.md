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

## 配置项

| 配置项 | 默认值 | 说明 |
|-------|-------|------|
| ext.api-crypto.public-key | - | RSA 公钥 |
| ext.api-crypto.private-key | - | RSA 私钥 |

## 核心注解

| 注解 | 说明 |
|------|------|
| `@RequestDecrypt` | 请求解密注解 |
| `@ResponseEncrypt` | 响应加密注解 |

## 支持的加密算法

| 算法 | 说明 |
|------|------|
| AES | AES 对称加密 |
| RSA | RSA 非对称加密 |
| SM2 | 国密 SM2 加密 |
| SM4 | 国密 SM4 加密 |

## 使用示例

### 配置密钥

```yaml
ext:
  api-crypto:
    public-key: "MIGfMA0GCSqGSIb3DQEBAQUAA4..."
    private-key: "MIICdQIBADANBgkqhkiG9w0BAQ..."
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
@RequestDecrypt(algorithm = Algorithm.AES)
@ResponseEncrypt(algorithm = Algorithm.AES)
public R<DataVO> handleAes(@RequestBody DataDTO dto) {
    return R.ok(dataService.handle(dto));
}
```

## 许可证

[Apache License 2.0](../../LICENSE)
