---
id: ext-api-crypto
type: module-design
title: ext-api-crypto — 接口加解密
status: draft
parent: ext-library-architecture
depends-on: [ext-tool, ext-json, ext-crypto, ext-servlet]
---

# ext-api-crypto — 接口加解密

**职责**：在接口边界做请求解密与响应加密：`@RequestDecrypt` / `@ResponseEncrypt`（均可指定 `algorithm`），`Algorithm` 枚举把算法映射到 `CryptoStrategy` 实现——现有值 `RSA`、`SM2`、`AES`、`SM4`、`BASE64`（`BASE64` 只是编解码，不是加密），由 `ApiCryptoAutoConfiguration` 装配。

**边界**：算法实现来自 [ext-crypto](../../ext-crypto/SPEC.md)，本模块只负责接口切入、报文结构（请求体解密的字段位置）与策略分发，不重复实现密码学；`spring-webmvc` 为接口侧依赖，servlet 能力经 [ext-servlet](../../ext-common/ext-servlet/SPEC.md) 取得——本模块不直接声明 `jakarta.servlet-api`，该依赖由消费方的 servlet 容器提供，自动配置按类名探测 servlet API。解密通过 MVC 的 `RequestBodyAdvice` 拿到 `HttpInputMessage`（读完返回新消息，不影响下游绑定）；**在 advice 之外裸读 `getInputStream()` 会耗尽流**，那种场景（过滤器、握手拦截器等）要走 [ext-servlet](../../ext-common/ext-servlet/SPEC.md) 的 `RepeatableRequestWrapper`。
