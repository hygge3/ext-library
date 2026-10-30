---
id: ext-crypto
type: module-design
title: ext-crypto — 加密算法工具库
status: draft
parent: ext-library-architecture
depends-on: [ext-tool]
---

# ext-crypto — 加密算法工具库

**职责**：纯静态工具，覆盖四类算法：

- **对称**：`AESUtil`（AES-GCM 256，AEAD 认证加密）、`SM4Util`（ECB / CBC）。
- **非对称**：`RSAUtil`（4096 位，含签名验签）、`SM2Util`（国密，含 `SM2KeyPair` record）。
- **摘要**：`DigestUtil`（MD5 / SHA / SHA3）、`SM3Util`（国密，含 HMAC）。
- **密码编码**：`PasswordEncoder`，基于 Spring `DelegatingPasswordEncoder`（BCrypt / Argon2 / SCrypt / PBKDF2），带 `upgradeEncoding` 判断。

**决策**：能用 Spring Security Crypto 实现的（AES、密码编码）就不另写一套，只有国密 SM2 / SM3 / SM4 走 Bouncy Castle 与 `bcpkix-jdk18on`——对应「不重复造 Spring 已提供的能力」。

**边界**：不依赖其他 ext 模块（`ext-tool` 除外）；无自动配置、无 Spring 容器依赖，可当普通工具库用在非 Spring 代码里。
