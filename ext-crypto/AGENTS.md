[根目录](../AGENTS.md) > **ext-crypto**

# ext-crypto - 加密层

> 提供常用加密算法工具库，基于 Spring Security Crypto 和 Bouncy Castle

## 模块职责

ext-crypto 是一个独立的加密模块，封装了对称加密、非对称加密、摘要算法和密码编码功能。优先使用 Spring Security Crypto 实现，国密算法使用 Bouncy Castle 实现。

## 主要功能

### 对称加密

| 工具类 | 算法 | 说明 |
|--------|------|------|
| `AESUtil` | AES-GCM 256 位 | 基于 Spring Security，AEAD 认证加密 |
| `SM4Util` | SM4-ECB/CBC | 国密对称加密算法 |

### 非对称加密

| 工具类 | 算法 | 说明 |
|--------|------|------|
| `RSAUtil` | RSA 4096 位 | RSA 公钥加密算法 |
| `SM2Util` | SM2 | 国密非对称加密算法 |

### 摘要算法

| 工具类 | 算法 | 说明 |
|--------|------|------|
| `DigestUtil` | MD5/SHA/SHA3 | 通用摘要算法工具 |
| `SM3Util` | SM3 | 国密摘要算法（256 位） |

### 密码编码

| 工具类 | 算法 | 说明 |
|--------|------|------|
| `PasswordEncoder` | BCrypt/Argon2/SCrypt/PBKDF2 | 基于 Spring Security DelegatingPasswordEncoder |

## 依赖关系

```
spring-security-crypto (AES, 密码编码)
bcpkix-jdk18on (SM2, SM3, SM4)
ext-tool (基础工具)
```

## 包结构

```
ext.library.crypto
├── AESUtil.java           # AES-GCM 256 位加密（Spring Security）
├── SM4Util.java           # SM4 国密加密
├── RSAUtil.java           # RSA 4096 位非对称加密
├── SM2Util.java           # SM2 国密非对称加密
│   └── SM2KeyPair         # SM2 密钥对 record
├── DigestUtil.java        # 通用摘要算法工具
├── SM3Util.java           # SM3 国密摘要算法
└── PasswordEncoder.java   # 密码编码器（Spring Security）
```

## 使用示例

### AES 加密（推荐）

```java
// 生成密码和盐值
String password = AESUtil.generatePassword();
String salt = AESUtil.generateSalt();

// 加密
String encrypted = AESUtil.encrypt(password, salt, "sensitive data");

// 解密
String decrypted = AESUtil.decrypt(password, salt, encrypted);
```

### RSA 加密

```java
KeyPair keyPair = RSAUtil.generateKeyPair();
String publicKey = Base64Util.encodeUrlSafeToStr(keyPair.getPublic().getEncoded());
String privateKey = Base64Util.encodeUrlSafeToStr(keyPair.getPrivate().getEncoded());

// 加密
String encrypted = RSAUtil.encrypt(publicKey, "data");
String decrypted = RSAUtil.decrypt(privateKey, encrypted);

// 签名
String signature = RSAUtil.sign(privateKey, "data");
boolean verified = RSAUtil.verify(publicKey, "data", signature);
```

### 国密 SM2 加密

```java
SM2Util.SM2KeyPair sm2Keys = SM2Util.generateKeyPair();

// 加密
String encrypted = SM2Util.encrypt(sm2Keys.publicKey(), "data");
String decrypted = SM2Util.decrypt(sm2Keys.privateKey(), encrypted);

// 签名
String signature = SM2Util.sign(sm2Keys.privateKey(), "data");
boolean verified = SM2Util.verify(sm2Keys.publicKey(), "data", signature);
```

### 国密 SM4 加密

```java
String key = SM4Util.generateKey(128);
String iv = SM4Util.generateKey(128);

// CBC 模式（推荐）
String encrypted = SM4Util.encryptByCBC(key, iv, "plaintext");
String decrypted = SM4Util.decryptByCBC(key, iv, encrypted);
```

### 国密 SM3 摘要

```java
// 计算哈希
String hash = SM3Util.hash("hello world");

// 计算 HMAC
String hmac = SM3Util.hmac("secret-key", "message");

// 验证
boolean valid = SM3Util.verify("hello world", hash);
boolean hmacValid = SM3Util.verifyHmac("secret-key", "message", hmac);
```

### 密码编码

```java
// 加密密码（默认 BCrypt）
String hashed = PasswordEncoder.encode("myPassword");
// 输出: {bcrypt}$2a$10$...

// 验证密码
boolean matches = PasswordEncoder.matches("myPassword", hashed);

// 检查是否需要升级算法
boolean needsUpgrade = PasswordEncoder.upgradeEncoding(hashed);
```

### 通用摘要

```java
String sha256 = DigestUtil.hash("SHA-256", "data");
String md5 = DigestUtil.hash("MD5", "data");
String sha3 = DigestUtil.hash("SHA3-256", "data");
```

## 算法选择指南

| 场景 | 推荐算法 | 工具类 |
| ------ | --------- | -------- |
| 数据加密（通用） | AES-GCM 256 位 | `AESUtil` |
| 数据加密（国密） | SM4-CBC | `SM4Util` |
| 非对称加密（通用） | RSA 4096 位 | `RSAUtil` |
| 非对称加密（国密） | SM2 | `SM2Util` |
| 密码存储 | BCrypt | `PasswordEncoder` |
| 数据摘要（通用） | SHA-256/SHA3-256 | `DigestUtil` |
| 数据摘要（国密） | SM3 | `SM3Util` |

## 测试覆盖

- `AESUtilTest.java` - AES 加解密测试
- `RSAUtilTest.java` - RSA 加解密、签名验签测试
- `SM2UtilTest.java` - SM2 加解密、签名验签测试
- `SM4UtilTest.java` - SM4 ECB/CBC 模式测试
- `SM3UtilTest.java` - SM3 哈希、HMAC 测试
- `PasswordEncoderTest.java` - 密码编码测试

## 变更记录

| 日期 | 变更内容 |
| ------ | ---------- |
| 2026-01-20 | 重构：删除 DESUtil，AESUtil 改用 AES-GCM 256 位，简化 PasswordEncoder，新增 SM3Util |
| 2026-01-20 | 重构 API：统一方法命名、修复一致性问题、完善文档 |
| 2026-01-19 | 初始化 AGENTS.md 文档 |
