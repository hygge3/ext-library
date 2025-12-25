[根目录](../CLAUDE.md) > **ext-crypto**

# ext-crypto 模块文档

## 模块职责

ext-crypto 提供加密算法工具类，支持对称加密、非对称加密、哈希算法和国密算法。

## 入口与启动

该模块是纯工具类集合，无需自动配置。

## 核心组件

### 1. 工具类
- **DigestUtil**: 摘要算法工具（MD5、SHA系列）
- **SymmetricUtil**: 对称加密工具（AES、DES、SM4）
- **AsymmetricUtil**: 非对称加密工具（RSA、SM2）
- **EncryptUtil**: 综合加密工具

### 2. 支持的算法
- 对称加密：AES、DES、SM4
- 非对称加密：RSA、SM2
- 哈希算法：MD5、SHA-1、SHA-256、SHA-384、SHA-512
- Base64 编码

## 关键依赖

- **ext-tool**: 基础工具类
- **bcpkix-jdk18on**: BouncyCastle 加密库
- **spring-security-crypto**: Spring Security 加密支持

## 测试与质量

- 测试位置：`src/test/java/ext/library/crypto/`
- 测试覆盖：5 个测试类
- 主要测试类：
  - `AESUtilTest`: AES 加密测试
  - `DESUtilTest`: DES 加密测试
  - `RSAUtilTest`: RSA 加密测试
  - `SM2UtilTest`: SM2 国密测试
  - `SM4UtilTest`: SM4 国密测试

## 使用示例

### 摘要算法
```java
// MD5
String md5 = DigestUtil.md5("password");

// SHA-256
String sha256 = DigestUtil.sha256("password");
```

### 对称加密
```java
// AES 加密
String encrypted = SymmetricUtil.aesEncrypt("plaintext", "key");
String decrypted = SymmetricUtil.aesDecrypt(encrypted, "key");

// SM4 国密加密
String sm4Encrypted = SymmetricUtil.sm4Encrypt("plaintext", "key");
String sm4Decrypted = SymmetricUtil.sm4Decrypt(sm4Encrypted, "key");
```

### 非对称加密
```java
// RSA 生成密钥对
KeyPair keyPair = AsymmetricUtil.generateRSAKeyPair();

// RSA 加密
String encrypted = AsymmetricUtil.rsaEncrypt("plaintext", keyPair.getPublic());
String decrypted = AsymmetricUtil.rsaDecrypt(encrypted, keyPair.getPrivate());

// SM2 国密
KeyPair sm2KeyPair = AsymmetricUtil.generateSM2KeyPair();
String sm2Encrypted = AsymmetricUtil.sm2Encrypt("plaintext", sm2KeyPair.getPublic());
String sm2Decrypted = AsymmetricUtil.sm2Decrypt(sm2Encrypted, sm2KeyPair.getPrivate());
```

### Base64
```java
// 编码
String encoded = Base64Util.encode("hello");

// 解码
String decoded = Base64Util.decode(encoded);
```

## 常见问题 (FAQ)

### Q: 如何选择加密算法？
- AES：高效对称加密，推荐使用
- RSA：非对称加密，用于密钥交换
- SM2/SM4：国密算法，国内项目推荐

### Q: 如何处理密钥？
建议使用密钥管理系统（KMS）或配置中心管理密钥。

## 相关文件清单

### 主要源码文件
- `src/main/java/ext/library/crypto/DigestUtil.java`
- `src/main/java/ext/library/crypto/SymmetricUtil.java`
- `src/main/java/ext/library/crypto/AsymmetricUtil.java`
- `src/main/java/ext/library/crypto/EncryptUtil.java`

### 测试文件
- `src/test/java/ext/library/crypto/AESUtilTest.java`
- `src/test/java/ext/library/crypto/DESUtilTest.java`
- `src/test/java/ext/library/crypto/RSAUtilTest.java`
- `src/test/java/ext/library/crypto/SM2UtilTest.java`
- `src/test/java/ext/library/crypto/SM4UtilTest.java`

## 变更记录 (Changelog)

### 2025-12-24
- 创建：模块文档
