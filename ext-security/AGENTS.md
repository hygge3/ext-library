[根目录](../AGENTS.md) > **ext-security**

# ext-security - 安全层

> 提供认证、授权、权限控制等安全功能

## 模块职责

ext-security 是一个独立的安全模块，提供完整的认证授权框架，包括：

- 用户认证与会话管理
- 基于注解的权限控制
- 角色与权限校验
- 两步验证（TOTP + 恢复码）与通行密钥（WebAuthn）原语
- 多存储后端支持（基于 ext-cache，支持 Caffeine/Redis/PostgreSQL/L2）

## 主要功能

### 权限注解

- `@RequiresPermissions` - 权限校验注解
- `@RequiresRoles` - 角色校验注解
- `@SecurityIgnore` - 忽略安全校验

### 逻辑运算

- `Logical.AND` - 需要满足所有条件
- `Logical.OR` - 满足任一条件即可

### 存储后端

- `SecurityRepository` - 安全信息存储接口
- `SecurityCacheRepository` - 基于 ext-cache 的存储实现（支持多种缓存后端）

### 核心组件

- `SecurityAuthority` - 权限校验接口（需业务实现）
- `SecurityEventPublishManager` - 安全事件发布管理
- `SecurityService` - 安全服务（持久化编排层，统一负责所有 I/O 操作）
- `MfaService` - 两步验证（TOTP + 恢复码）
- `PasskeyService` - 通行密钥（WebAuthn）注册与认证

## 依赖关系

```
ext-useragent (UserAgent 解析)
ext-core (核心工具)
ext-json (JSON 处理)
ext-cache (缓存支持，支持 Caffeine/Redis/PostgreSQL/L2)
ext-servlet (servlet 基座：R、ServletUtil、ErrorEnvelope)
ext-crypto (加密工具)
webauthn4j-core (WebAuthn 注册/断言校验)
spring-webmvc (拦截器与 @RestControllerAdvice，jakarta.servlet-api 为 compileOnly)
```

## 包结构

```
ext.library.security
├── annotation/        # 安全注解
│   ├── RequiresPermissions.java
│   ├── RequiresRoles.java
│   └── SecurityIgnore.java
├── authority/         # 权限校验
│   └── SecurityAuthority.java
├── config/            # 自动配置
│   └── SecurityAutoConfiguration.java
├── domain/            # 领域模型（纯数据，无 Spring 依赖）
│   ├── MfaCredential.java
│   ├── MfaSetup.java
│   ├── PasskeyAuthenticationOptions.java
│   ├── PasskeyCredential.java
│   ├── PasskeyRegistrationOptions.java
│   ├── SecurityLoginParams.java
│   ├── SecuritySession.java
│   └── SecurityToken.java
├── enums/             # 枚举
│   ├── Logical.java
│   └── TokenState.java
├── exception/         # 异常
│   ├── ForbiddenException.java
│   └── UnauthorizedException.java
├── handler/           # 异常处理器
│   └── SecurityExceptionHandler.java
├── interceptor/       # 拦截器
│   └── SecurityInterceptor.java
├── listener/          # 事件监听
│   ├── SecurityEventPublishManager.java
│   ├── SecurityListener.java
│   └── SecurityListenerManager.java
├── properties/        # 配置属性
│   ├── CookieProperties.java
│   ├── MfaProperties.java
│   ├── PasskeyProperties.java
│   └── SecurityProperties.java
├── repository/        # 存储接口
│   ├── CacheMfaCredentialRepository.java
│   ├── CachePasskeyCredentialRepository.java
│   ├── MfaCredentialRepository.java
│   ├── PasskeyCredentialRepository.java
│   ├── SecurityRepository.java
│   └── SecurityCacheRepository.java
├── service/           # 服务（持久化编排层）
│   ├── MfaService.java
│   ├── PasskeyService.java
│   └── SecurityService.java
└── util/              # 工具类
    ├── PermissionUtil.java
    ├── SecurityUtil.java
    └── TotpUtil.java
```

## 配置说明

安全模块配置前缀：`ext.security`

| 配置项 | 默认值 | 说明 |
|-------|-------|------|
| token-name | Authorization | token 在 HTTP Header / 请求参数中的键名 |
| token-prefix | Bearer  | token 前缀，拼接在 token 值之前（含尾部空格） |
| timeout | 30d | 授权有效期，支持 Duration 格式（30d, 720h） |
| activity-timeout | 1h | 最低活跃频率，支持 Duration 格式（1h, 60m） |
| auto-renewal | true | 是否自动续约 |
| auto-renewal-interval | 3m | 自动续约间隔，支持 Duration 格式 |
| is-concurrent-login | true | 是否允许多地同时登录 |
| max-login-limit | -1 | 同一账号最大登录数量，-1 不限制 |
| max-login-device-type-limit | -1 | 同一账号允许同时登录的设备类型数量，-1 不限制 |
| issue-token-max-limit | -1 | 全局颁发 token 最大数量，-1 不限制 |
| enable-cookie | true | 是否开启 cookie |
| cookie-config.cookie-name | Token | cookie 名称 |
| cookie-config.http-only | true | 是否禁止 JS 操作 Cookie |
| cookie-config.domain |  | cookie 域设置 |
| cookie-config.path |  | cookie 路径设置 |
| cookie-config.secure |  | 是否仅在 HTTPS 下发送 |

存储配置通过 ext-cache 模块的 `ext.cache` 前缀进行配置。

两步验证（前缀 `ext.security.mfa`）：

| 配置项 | 默认值 | 说明 |
|-------|-------|------|
| issuer | ext | 发行方名称，显示在验证器 App 里 |
| window | 1 | 时间片容差，1 表示接受前后各一个时间片（±30 秒） |
| recovery-code-count | 10 | 恢复码数量 |
| setup-timeout | 10m | 未确认绑定的 secret 保留时长 |

通行密钥（前缀 `ext.security.passkey`）：

| 配置项 | 默认值 | 说明 |
|-------|-------|------|
| rp-id | 无 | 依赖方 ID（如 example.com）；不配则不装配 `PasskeyService` |
| rp-name | ext | 依赖方展示名称 |
| origins | 空 | 允许的来源，留空按 `https://{rpId}` 生成 |
| challenge-timeout | 5m | 挑战有效期 |
| user-verification-required | false | 是否强制用户验证（生物识别 / PIN） |

## 使用示例

```java
// 登录
SecurityUtil.doLogin(userId.toString());

// 登录并携带自定义数据
SecurityUtil.doLogin(userId.toString(), userInfo);

// 获取当前登录 ID
String loginId = SecurityUtil.getCurrentLoginId();

// 获取当前 Session 挂载的自定义数据
UserInfo userInfo = SecurityUtil.getCurrentSecuritySession().getAttributes(UserInfo.class);

// 退出
SecurityUtil.logout();

// 踢下线 / 顶下线 / 封禁 / 解封
SecurityUtil.kickToken(token);
SecurityUtil.replaceToken(token);
SecurityUtil.bannedToken(token);
SecurityUtil.unsealToken(token);

// 是否已登录
boolean logged = SecurityUtil.isLogin();
```

```java
// 权限校验注解
@RequiresPermissions("user:read")
public User getUser(Long id) { ... }

// 角色校验（满足任一）
@RequiresRoles(value = {"admin", "manager"}, logical = Logical.OR)
public void adminOperation() { ... }

// 忽略安全校验
@SecurityIgnore
public String publicApi() { ... }
```

```java
// 实现权限接口（必须注册为 Spring Bean）
@Component
public class MySecurityAuthority implements SecurityAuthority {

    @Override
    public List<String> getPermissionCodeList(String loginId) {
        return permissionService.listByUserId(loginId);
    }

    @Override
    public List<String> getRoleCodeList(String loginId) {
        return roleService.listByUserId(loginId);
    }
}
```

```java
// 两步验证：登录编排由业务自己拼（密码通过后再验第二步）
if (mfaService.isEnabled(loginId) && !mfaService.verify(loginId, mfaCode)) {
    throw new UnauthorizedException("验证码错误");
}
securityService.doLogin(loginId, params);

// 绑定：返回的 otpauthUri 交给二维码组件渲染
MfaSetup setup = mfaService.beginSetup(loginId);
List<String> recoveryCodes = mfaService.confirmSetup(loginId, codeFromApp);

// 解绑（需先通过一次验证码）
mfaService.disable(loginId, codeFromApp);
```

```java
// 通行密钥注册
PasskeyRegistrationOptions registrationOptions = passkeyService.beginRegistration(loginId, "alice", "Alice");
// 前端：navigator.credentials.create({ publicKey: registrationOptions })
passkeyService.finishRegistration(loginId, responseJsonFromBrowser);

// 通行密钥登录（loginId 传 null 即免用户名登录）
PasskeyAuthenticationOptions authOptions = passkeyService.beginAuthentication(loginId);
// 前端：navigator.credentials.get({ publicKey: authOptions })
String authenticatedLoginId = passkeyService.finishAuthentication(loginId, responseJsonFromBrowser);
securityService.doLogin(authenticatedLoginId, params);
```

## 相关文件

- `src/main/java/ext/library/security/` - 安全模块源码
- `src/test/java/ext/library/security/` - 单元测试
- `pom.xml` - 模块依赖配置

## 设计约束

**职责**：完整的 token 会话与权限控制：

- **会话**：`SecuritySession` / `SecurityToken` / `TokenState`，`SecurityUtil` 提供 `doLogin` / `logout` / `isLogin` / `kickToken`（踢下线）/ `replaceToken`（顶下线）/ `bannedToken` / `unsealToken`；会话属性挂自定义数据（`getAttributes(Class)`）。
- **权限**：`@RequiresPermissions`、`@RequiresRoles`（`Logical.AND` / `Logical.OR`）、`@SecurityIgnore`、`SecurityInterceptor`、`SecurityExceptionHandler`；权限码与角色码由业务实现 `SecurityAuthority` 提供。
- **策略配置** `ext.security.*`：`token-name`、`token-prefix`、`timeout`（默认 30d）、`activity-timeout`（1h）、`auto-renewal` / `auto-renewal-interval`（3m）、多端登录开关与 `max-login-limit` / `max-login-device-type-limit` / `issue-token-max-limit`、cookie 相关配置。时长一律 `Duration`。
- **存储**：`SecurityRepository` 接口 + `SecurityCacheRepository` 实现，落盘经 `ext-cache`，因此 Caffeine / Redis / PostgreSQL / L2 任一后端都可用，无需为安全再引一套存储。

**不变量**：

- `SecurityService` 是唯一的持久化编排层，所有 I/O 收口于它；`domain` 包不带 Spring 容器依赖（2026-02-18 的 SRP 重构结论，勿回退）。
- 会话与 token 索引存于共享缓存，进程内状态不可作为权威来源——多实例部署下必须走分布式后端。

**依赖边界**：只向下依赖 `ext-common/ext-servlet`（`R`、`ServletUtil`、`ErrorEnvelope` / `ErrorLog`），**不依赖 `ext-web:ext-mvc`**——安全层在 Web 层之下，历史上那条 `ext-security → ext-mvc` 的越层边已随基座抽出而消除（见 [architecture](../architecture.md) 的「已执行的重构」），不得再引入。`spring-webmvc` 由本模块直接声明（拦截器与 `@RestControllerAdvice` 需要它），不经由 `ext-mvc` 传递；`jakarta.servlet-api` 为 `compileOnly`。`ext-useragent` 现在也在 `ext-common`，是正常的向下依赖。

### 两步验证与通行密钥

**登录流程由接入方编排，本模块只给原语**。`SecurityService.doLogin(loginId, params)` 仍是登录成功的入口：接入方校验密码 → 若 `MfaService.isEnabled` 则要求提交验证码并 `MfaService.verify` → 通过后 `doLogin`。库不定义半登录态（PENDING token），也不接管登录编排——接入方本来就要写这一段。

**两步验证**（`MfaService`，TOTP + 恢复码）：

- `TotpUtil` 纯 JDK 实现 RFC 6238（HMAC-SHA1、6 位、30 秒步长、Base32 secret 无填充），不引 OTP 库。
- 绑定三步：`beginSetup(loginId)` 返回 secret 与 `otpauth://` URI（前端渲染二维码）→ 用户扫描 → `confirmSetup(loginId, code)` 校验通过后启用并把恢复码明文**一次性**返回。未确认的 secret 只保留 `setup-timeout`。
- 恢复码明文不落存储，只存 SHA-256 摘要；使用时按「长度 10 位」与 6 位 TOTP 区分，用完即从列表移除。
- 解绑 `disable(loginId, code)` 必须先通过一次验证码校验。

**通行密钥**（`PasskeyService`，WebAuthn）：

- 校验一律交给 `webauthn4j-core`（`WebAuthnRegistrationManager` / `WebAuthnAuthenticationManager`），**不手写 CBOR/COSE 验签**。注册用 non-strict manager（`attestation: none`，不做 attestation 链校验）；需要 attestation 策略时另说。
- challenge 32 字节随机，`put` 进共享缓存（注册与认证分开 key），校验前取出并 `evict`，一个 challenge 只能用一次。
- 公钥不拆字段：`AttestedCredentialData` 的 CBOR 字节以 Base64URL 整体存进 `PasskeyCredential`，校验时用 `AttestedCredentialDataConverter` 原样还原。
- `finishAuthentication(loginId, responseJson)` 的 `loginId` 传 `null` 表示不限定账号（可发现凭证 / 免用户名登录）；非 null 时校验证书归属，防跨账号冒用。成功后把 signCount 与 `lastUsedTime` 写回凭证。
- `rp-id` 未配置时 `PasskeyService` 不装配（`origin` 校验无从下手）。

**存储**：`MfaCredentialRepository` / `PasskeyCredentialRepository` 各一套接口 + `ext-cache` 默认实现，落业务库时实现接口覆盖 Bean 即可。和会话一样走 `distributed()`，解绑/删凭证必须全网可见。

**不要做的抽象**：不要加 `SecondFactorVerifier` 这类 `supports(type)/verify(...)` 调度器——两种认证方式直接把方法摆出来就够了，需要统一入口时再由接入方在业务层拼。

**新增认证方式时不要动的不变量**：`SecurityService` 仍是唯一的会话持久化编排层；`domain` 包仍不带 Spring 容器依赖；任何登录态都存共享缓存，进程内标记在多实例下必然丢。

## 变更记录

| 日期 | 变更内容 |
|------|----------|
| 2026-11-01 | 新增两步验证（`MfaService` / `TotpUtil`）与通行密钥（`PasskeyService`，基于 webauthn4j）；凭证存储各一套接口 + ext-cache 默认实现；登录编排仍由接入方负责，不引入半登录态 |
| 2026-02-18 | SRP 重构：领域对象移除 Spring 容器依赖，持久化逻辑统一收归 SecurityService；修复多处 bug（tokenIndex 分布式、createLoginByLoginId token 持久化、issueTokenMaxLimit 逻辑反向、PermissionUtil AND 分支）；修复 CookieProperties final 字段、SecurityExceptionHandler 注解、SecurityListenerManager 线程安全 |
| 2026-01-26 | 时间配置改用 Duration 类型，支持 30d、1h、3m 等格式 |
| 2026-01-26 | 重构存储层，改用 ext-cache 替代独立的 RAM/Redis 实现 |
| 2026-01-19 | 初始化 AGENTS.md 文档 |
