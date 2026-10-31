---
id: ext-security
type: module-design
title: ext-security — 认证与授权框架
status: draft
parent: ext-library-architecture
depends-on: [ext-core, ext-json, ext-crypto, ext-cache, ext-servlet, ext-useragent]
---

# ext-security — 认证与授权框架

**职责**：完整的 token 会话与权限控制：

- **会话**：`SecuritySession` / `SecurityToken` / `TokenState`，`SecurityUtil` 提供 `doLogin` / `logout` / `isLogin` / `kickToken`（踢下线）/ `replaceToken`（顶下线）/ `bannedToken` / `unsealToken`；会话属性挂自定义数据（`getAttributes(Class)`）。
- **权限**：`@RequiresPermissions`、`@RequiresRoles`（`Logical.AND` / `Logical.OR`）、`@SecurityIgnore`、`SecurityInterceptor`、`SecurityExceptionHandler`；权限码与角色码由业务实现 `SecurityAuthority` 提供。
- **策略配置** `ext.security.*`：`token-name`、`token-prefix`、`timeout`（默认 30d）、`activity-timeout`（1h）、`auto-renewal` / `auto-renewal-interval`（3m）、多端登录开关与 `max-login-limit` / `max-login-device-type-limit` / `issue-token-max-limit`、cookie 相关配置。时长一律 `Duration`。
- **存储**：`SecurityRepository` 接口 + `SecurityCacheRepository` 实现，落盘经 `ext-cache`，因此 Caffeine / Redis / PostgreSQL / L2 任一后端都可用，无需为安全再引一套存储。

**不变量**：

- `SecurityService` 是唯一的持久化编排层，所有 I/O 收口于它；`domain` 包不带 Spring 容器依赖（2026-02-18 的 SRP 重构结论，勿回退）。
- 会话与 token 索引存于共享缓存，进程内状态不可作为权威来源——多实例部署下必须走分布式后端。

**依赖边界**：只向下依赖 `ext-common/ext-servlet`（`R`、`ServletUtil`、`ErrorEnvelope` / `ErrorLog`），**不依赖 `ext-web:ext-mvc`**——安全层在 Web 层之下，历史上那条 `ext-security → ext-mvc` 的越层边已随基座抽出而消除（见 [architecture](../architecture.md) 的「已执行的重构」），不得再引入。`spring-webmvc` 由本模块直接声明（拦截器与 `@RestControllerAdvice` 需要它），不经由 `ext-mvc` 传递；`jakarta.servlet-api` 为 `compileOnly`。`ext-useragent` 现在也在 `ext-common`，是正常的向下依赖。
