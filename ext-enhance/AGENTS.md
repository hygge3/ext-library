[根目录](../AGENTS.md) > **ext-enhance**

# ext-enhance - 业务增强层

> 提供验证码、幂等性、API加密、限流、操作审计等业务增强功能

## 层级职责

ext-enhance 提供常见业务场景的增强功能，通过注解和 AOP 实现无侵入式的功能扩展。

## 子模块列表

| 模块 | 描述 | 主要依赖 |
|------|------|----------|
| ext-captcha | 图形验证码生成与校验 | ext-cache |
| ext-idempotent | 接口幂等性控制 | ext-core, ext-servlet, ext-redis, caffeine(可选) |
| ext-api-crypto | API 请求/响应加解密 | ext-json, ext-crypto |
| ext-ratelimit | 接口限流控制 | ext-core, ext-servlet, ext-redis(可选), ext-postgres(可选) |
| ext-audit | 操作审计留痕 | ext-json, ext-servlet |

> 字段翻译 (`ext.library.trans`) 与数据脱敏 (`ext.library.desensitize`) 已并入 ext-common/ext-json。

## 模块详情

### ext-captcha

图形验证码模块，提供：

**验证码绘制**:
- `CaptchaDraw` - 验证码绘制接口
- `RandomCaptchaDraw` - 随机字符验证码
- `MathCaptchaDraw` - 数学表达式验证码
- `BackgroundDraw` / `SmallCharBackgroundDraw` - 背景绘制
- `InterferenceDraw` - 干扰线绘制
- `CubicCurveInterferenceDraw` - 曲线干扰

**核心组件**:
- `Captcha` - 验证码核心接口
- `CaptchaService` - 验证码服务接口（`DefaultCaptchaService` 默认实现）
- `CaptchaCache` - 验证码缓存
- `CaptchaResponse` - 验证码响应对象

**包结构**: `ext.library.captcha`

#### 设计约束

**职责**：验证码的生成与校验：`CaptchaDraw` 绘制接口及实现（随机字符 `RandomCaptchaDraw`、数学表达式 `MathCaptchaDraw`、背景 `BackgroundDraw`/`SmallCharBackgroundDraw`、干扰线 `InterferenceDraw`、曲线干扰 `CubicCurveInterferenceDraw`）、`Captcha`/`DefaultCaptcha`/`CaptchaUtil`、`CaptchaService`/`DefaultCaptchaService`/`CaptchaWebService`、`CaptchaResponse` 响应模型、`CaptchaProperties`。

**存储**：答案经 `CaptchaCache` 存进 [ext-cache](../ext-infra/AGENTS.md#ext-cache)，因此自动继承缓存后端选择——单机用内存、多实例必须用 Redis / PostgreSQL，否则跨实例校验失败。

**边界**：依赖 `ext-cache` 承担存储；不实现登录流程与风控规则，只负责"生成一张图 + 校验一次答案"。

### ext-idempotent

幂等性控制模块，提供：

- `@Idempotent` - 幂等性注解
- `IdempotentAspect` - 幂等性切面
- `KeyGenerator` / `DefaultKeyGenerator` - 幂等键生成器
- 幂等键的存储由 ext-cache 的 `CacheStrategy.putIfAbsent` 承担，本模块不再自带 KeyStore

**包结构**: `ext.library.idempotent`

#### 设计约束

**职责**：`@Idempotent` + `IdempotentAspect`：按幂等键在时间窗内只放行一次，后到请求被拒。`KeyGenerator` / `DefaultKeyGenerator` 生成键（可替换）。

**存储**：幂等键经 [ext-cache](../ext-infra/AGENTS.md#ext-cache) 的 `CacheStrategy.putIfAbsent(cacheName, key, value, expireTime)` 写入，因此自动继承缓存后端选择（Caffeine / Redis / PostgreSQL）与 `ext.cache.key-prefix`，本模块不再自带 `KeyStore` 与后端探测。

**约束**：`putIfAbsent` 必须是后端的原子操作（Caffeine `asMap().putIfAbsent`、Redis `SET NX PX`、PostgreSQL `INSERT … ON CONFLICT DO NOTHING`），不能用 `get` + `put` 拼出来——否则并发重复请求会双双放行，而这正是本模块唯一要保证的事。`L2` 也可用：`L2Strategy.putIfAbsent` 以分布式层为唯一裁决者，本地层只在写入成功后回填，不存在「本地没命中、远端已存在」的分裂判定。

**边界**：依赖 `ext-cache` 承担存储；不引任何后端时只剩内存实现，多实例部署下幂等失效。不实现业务侧幂等语义（如订单去重、状态机校验），只负责“同一个键在时间窗内只放行一次”。缓存名由注解的 `cacheName` 决定（默认 `idempotent`）。

### ext-api-crypto

API 加解密模块，提供：

- `@RequestDecrypt` - 请求解密注解（支持 algorithm 属性）
- `@ResponseEncrypt` - 响应加密注解（支持 algorithm 属性）
- `Algorithm` - 加密算法枚举 (RSA/SM2/AES/SM4/BASE64)
- `ApiCryptoAutoConfiguration` - 自动配置类
- `CryptoStrategy` - 加密策略接口及实现

**包结构**: `ext.library.apicrypto`

#### 设计约束

**职责**：在接口边界做请求解密与响应加密：`@RequestDecrypt` / `@ResponseEncrypt`（均可指定 `algorithm`），`Algorithm` 枚举把算法映射到 `CryptoStrategy` 实现——现有值 `RSA`、`SM2`、`AES`、`SM4`、`BASE64`（`BASE64` 只是编解码，不是加密），由 `ApiCryptoAutoConfiguration` 装配。

**边界**：算法实现来自 [ext-crypto](../ext-crypto/AGENTS.md)，本模块只负责接口切入、报文结构（请求体解密的字段位置）与策略分发，不重复实现密码学；`spring-webmvc` 为接口侧依赖，servlet 能力经 [ext-servlet](../ext-common/AGENTS.md#ext-servlet) 取得——本模块不直接声明 `jakarta.servlet-api`，该依赖由消费方的 servlet 容器提供，自动配置按类名探测 servlet API。解密通过 MVC 的 `RequestBodyAdvice` 拿到 `HttpInputMessage`（读完返回新消息，不影响下游绑定）；**在 advice 之外裸读 `getInputStream()` 会耗尽流**，那种场景（过滤器、握手拦截器等）要走 [ext-servlet](../ext-common/AGENTS.md#ext-servlet) 的 `RepeatableRequestWrapper`。

### ext-ratelimit

限流控制模块，提供：

- `@RateLimit` - 限流注解（`key` 支持 SpEL、`limit` 阈值、`window` 窗口）
- `RateLimitAspect` - 限流切面
- `RateLimitKeyGenerator` - 限流键生成器（SpEL）
- `RateLimitStore` - 限流存储 SPI
- `MemoryRateLimitStore` / `RedisRateLimitStore` / `PostgresRateLimitStore` - 三种存储实现
- `RateLimitException` - 触发限流时抛出，携带限流结果供拼装 `Retry-After`

**包结构**: `ext.library.ratelimit`

配置项：`ext.ratelimit.store-type`，可选 `AUTO`（默认）/ `REDIS` / `POSTGRES` / `MEMORY`。

**AUTO** 优先级：Redis > 内存。

#### 设计约束

**职责**：`@RateLimit`（`key` 支持 SpEL、`limit` 阈值、`window` 窗口）+ `RateLimitAspect`；`RateLimitKeyGenerator` 生成键；`RateLimitStore` 为 SPI，实现有 `MemoryRateLimitStore` / `RedisRateLimitStore` / `PostgresRateLimitStore`；触发限流抛 `RateLimitException`，携带限流结果供上层拼装 `Retry-After`。

**后端选择**：`ext.ratelimit.store-type` = `AUTO`（默认）/ `REDIS` / `POSTGRES` / `MEMORY`，`AUTO` 优先级 Redis > 内存。

### ext-audit

操作审计模块，提供：

- `@OperationLog` - 审计注解（`module`/`type`/`desc` 支持 SpEL/`recordArgs`/`recordResult`）
- `AuditAspect` - 审计切面，成功与失败均留痕
- `AuditRecord` - 审计记录（操作人、模块、类型、描述、入参、结果、耗时、时间）
- `AuditHandler` - 处理器 SPI，业务实现它把记录落库或投递到 MQ
- `AuditDispatcher` - 处理器分发器，单个处理器异常不影响业务与其它处理器
- `AuditOperatorResolver` - 操作人解析器，默认反射调用 ext-security 的 `SecurityUtil`，不可用时回退客户端 IP

**包结构**: `ext.library.audit`

配置项：`ext.audit.log-to-console`，未注册任何 `AuditHandler` 时是否降级打日志（默认 `true`）。

#### 设计约束

**职责**：`@OperationLog`（`module` / `type` / `desc` 支持 SpEL，`recordArgs` / `recordResult` 控制入参与结果是否落痕）+ `AuditAspect`：**成功与失败都留痕**。`AuditRecord` 携带操作人、模块、类型、描述、入参、结果、耗时、时间；`AuditHandler` 为 SPI，业务实现它把记录落库或投递 MQ；`AuditOperatorResolver` 解析操作人。

**不变量**：`AuditDispatcher` 分发时，单个 `AuditHandler` 抛异常不得影响业务方法与其它处理器——审计永远不能把业务打挂。未注册任何 `AuditHandler` 时按 `ext.audit.log-to-console`（默认 `true`）降级打日志。

**边界**：对 [ext-security](../ext-security/AGENTS.md) 只做 `compileOnly` 依赖 + 反射调用 `SecurityUtil` 取当前登录人，取不到时回退客户端 IP；因此不引安全模块也能启动。依赖 `ext-json` 做序列化。客户端 IP 一律用 [ext-servlet](../ext-common/AGENTS.md#ext-servlet) 的 `ServletUtil.getIpAddr()`（走 `X-Forwarded-For` 链），**不要用裸 `getRemoteAddr()`**——那记下来的是反代后的内网地址，与限流、安全用的 IP 口径不一致。

## 依赖关系

```
ext-cache
    |
    +---> ext-captcha

ext-redis
    |
    +---> ext-idempotent

ext-json + ext-crypto
    |
    +---> ext-api-crypto

ext-redis + ext-postgres
    |
    +---> ext-ratelimit

ext-json + ext-security
    |
    +---> ext-audit
```

## 使用示例

```java
// 幂等性
@Idempotent(timeout = 5000)
public void createOrder(OrderDTO order) { ... }

// 脱敏（来自 ext-json）
@Sensitive(strategy = SensitiveStrategy.MOBILE_PHONE)
private String phone;

// 翻译（来自 ext-json）
@Translate(type = "dict", mapper = "status", param = "user_status")
private String statusLabel;

// API 加密
@ResponseEncrypt(algorithm = Algorithm.AES)
public Result getData() { ... }

// 限流
@RateLimit(key = "#request.getRemoteAddr()", limit = 100, window = 1, timeUnit = TimeUnit.MINUTES)
public Result<Void> sendCode(String phone) { ... }

// 操作审计
@OperationLog(module = "订单", type = OperationType.INSERT, desc = "提交订单 #orderId")
public Result<Void> submit(Order order) { ... }
```

## 相关文件

- `ext-captcha/src/main/java/ext/library/captcha/` - 验证码源码
- `ext-idempotent/src/main/java/ext/library/idempotent/` - 幂等性源码
- `ext-api-crypto/src/main/java/ext/library/apicrypto/` - API 加密源码
- `ext-ratelimit/src/main/java/ext/library/ratelimit/` - 限流源码
- `ext-audit/src/main/java/ext/library/audit/` - 操作审计源码

## 变更记录

| 日期 | 变更内容 |
|------|----------|
| 2026-01-19 | 初始化 AGENTS.md 文档 |
| 2026-10-10 | ext-trans 与 ext-desensitize 并入 ext-common/ext-json |
| 2026-10-10 | 新增 ext-ratelimit（限流）与 ext-audit（操作审计） |
