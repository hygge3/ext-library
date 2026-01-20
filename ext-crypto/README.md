# ext-crypto

> 加密工具库 - 提供常用加密算法封装

## 简介

`ext-crypto` 是 ext-library 的加密工具模块，封装了常用的对称加密、非对称加密和摘要算法，基于 Bouncy Castle 和 Spring Security Crypto。

## 快速开始

### Maven

```xml
<dependency>
    <groupId>ext.library</groupId>
    <artifactId>ext-crypto</artifactId>
</dependency>
```

### Gradle

```groovy
implementation("ext.library:ext-crypto")
```

## 依赖说明

| 依赖 | 说明 |
|------|------|
| ext-tool | 基础工具类 |
| bcpkix-jdk18on | Bouncy Castle 加密库 |
| spring-security-crypto | 密码加密支持 |

## 功能模块

### 对称加密

| 工具类 | 算法 | 说明 |
|--------|------|------|
| `AESUtil` | AES | 高级加密标准，推荐使用 |
| `DESUtil` | DES | 数据加密标准 |
| `SM4Util` | SM4 | 国密对称加密算法 |

### 非对称加密

| 工具类 | 算法 | 说明 |
|--------|------|------|
| `RSAUtil` | RSA | RSA 公钥加密算法 |
| `SM2Util` | SM2 | 国密非对称加密算法 |

### 摘要与编码

| 工具类 | 功能 | 说明 |
|--------|------|------|
| `DigestUtil` | 摘要算法 | MD5, SHA 等摘要计算 |
| `PasswordEncoder` | 密码编码 | 基于 Spring Security 的密码加密 |

## 包结构

```
ext.library.crypto
├── AESUtil.java         # AES 加密工具
├── DESUtil.java         # DES 加密工具
├── SM4Util.java         # SM4 国密加密
├── RSAUtil.java         # RSA 非对称加密
├── SM2Util.java         # SM2 国密非对称加密
├── DigestUtil.java      # 摘要算法工具
└── PasswordEncoder.java # 密码编码器
```

## 使用示例

### AES 加密

```java
// 加密
String encrypted = AESUtil.encrypt("plaintext", "secretKey");

// 解密
String decrypted = AESUtil.decrypt(encrypted, "secretKey");
```

### RSA 加密

```java
// 生成密钥对
KeyPair keyPair = RSAUtil.generateKeyPair();
String publicKey = RSAUtil.getPublicKey(keyPair);
String privateKey = RSAUtil.getPrivateKey(keyPair);

// 公钥加密
String encrypted = RSAUtil.encryptByPublicKey("data", publicKey);

// 私钥解密
String decrypted = RSAUtil.decryptByPrivateKey(encrypted, privateKey);

// 私钥签名
String sign = RSAUtil.sign("data", privateKey);

// 公钥验签
boolean valid = RSAUtil.verify("data", sign, publicKey);
```

### SM2 国密加密

```java
// 生成密钥对
KeyPair keyPair = SM2Util.generateKeyPair();

// 加密
String encrypted = SM2Util.encrypt("data", keyPair.getPublic());

// 解密
String decrypted = SM2Util.decrypt(encrypted, keyPair.getPrivate());

// 签名
String sign = SM2Util.sign("data", keyPair.getPrivate());

// 验签
boolean valid = SM2Util.verify("data", sign, keyPair.getPublic());
```

### SM4 国密加密

```java
// 加密
String encrypted = SM4Util.encrypt("plaintext", "secretKey");

// 解密
String decrypted = SM4Util.decrypt(encrypted, "secretKey");
```

### 摘要算法

```java
// MD5
String md5 = DigestUtil.md5("text");

// SHA-256
String sha256 = DigestUtil.sha256("text");

// SHA-512
String sha512 = DigestUtil.sha512("text");
```

### 密码编码

```java
// 编码密码
String encoded = PasswordEncoder.encode("password");

// 验证密码
boolean matches = PasswordEncoder.matches("password", encoded);
```

## 许可证

[Apache License 2.0](../LICENSE)
