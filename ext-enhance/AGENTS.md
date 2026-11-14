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
| ext-api-crypto | API 请求/响应加解密、接口签名 | ext-json, ext-crypto, ext-cache |
| ext-resilience | 限流（令牌桶）与降级（熔断） | ext-core, ext-cache, ext-servlet(可选) |
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
- `RepeatSubmit` / `RepeatSubmitKeyGenerator` - 防重提交注解与键生成器
- 幂等键的存储由 ext-cache 的 `CacheStrategy.putIfAbsent` 承担，本模块不再自带 KeyStore

**包结构**: `ext.library.idempotent`

#### 设计约束

**职责**：`@Idempotent` + `IdempotentAspect`：按幂等键在时间窗内只放行一次，后到请求被拒。`KeyGenerator` / `DefaultKeyGenerator` 生成键（可替换）。

**两个注解的分工**：`@Idempotent` 的键由业务表达式推导，防的是「同一笔业务重复生效」（订单号、表单 ID）；`@RepeatSubmit` 的键由「用户 + 接口 + 参数指纹」推导，防的是「同一个人连点同一接口」，业务方不写表达式。两者的写入、释放、异常处理完全共用 `IdempotentAspect.guard`，只有键来源与默认窗口不同（`@RepeatSubmit` 默认 3 秒，`@Idempotent` 默认 10 秒）。

**防重键的隐私约束**：`RepeatSubmitKeyGenerator` 键 = `prefix:hash(用户):方法:URI:hash(参数指纹)`。用户标识与参数只留 SHA-256 截断摘要，不写明文——用户标识可能是 session id（写进缓存键等于把凭证摆到 Redis / 数据库里），参数里常带手机号、身份证。参数指纹用 JSON 而非拼接字符串，保留字段名与嵌套结构，避免 `"a"+"b"` 与 `"ab"+""` 撞键；servlet 对象、`Principal`、`MultipartFile` 不参与指纹（不可序列化也无需区分）。无 `Principal` 时退到 session id，再退到 `anon`——匿名调用下同参数请求会互相顶掉，属预期。

**存储**：幂等键经 [ext-cache](../ext-infra/AGENTS.md#ext-cache) 的 `CacheStrategy.putIfAbsent(cacheName, key, value, expireTime)` 写入，因此自动继承缓存后端选择（Caffeine / Redis / PostgreSQL）与 `ext.cache.key-prefix`，本模块不再自带 `KeyStore` 与后端探测。

**约束**：`putIfAbsent` 必须是后端的原子操作（Caffeine `asMap().putIfAbsent`、Redis `SET NX PX`、PostgreSQL `INSERT … ON CONFLICT DO NOTHING`），不能用 `get` + `put` 拼出来——否则并发重复请求会双双放行，而这正是本模块唯一要保证的事。`L2` 也可用：`L2Strategy.putIfAbsent` 以分布式层为唯一裁决者，本地层只在写入成功后回填，不存在「本地没命中、远端已存在」的分裂判定。

**释放**：业务抛异常时**无条件**释放幂等键（参数校验、下游超时这类可恢复错误，用户重试是正当行为）；释放失败只记 warn，不能用缓存异常盖掉原始业务异常。防暴力重试用 @RateLimit，不是靠幂等键锁人。键生成器对 SpEL 求值为空的情况直接抛异常——求空时键会退化成同一个常量，整个接口被锁死一个 `duration`，而对外表现只是「重复请求」，极难排查。

**边界**：依赖 `ext-cache` 承担存储；不引任何后端时只剩内存实现，多实例部署下幂等失效。不实现业务侧幂等语义（如订单去重、状态机校验），只负责“同一个键在时间窗内只放行一次”。缓存名由注解的 `cacheName` 决定（默认 `idempotent`；`@RepeatSubmit` 默认 `repeat-submit`）。

### ext-api-crypto

API 加解密模块，提供：

- `@RequestDecrypt` - 请求解密注解（支持 algorithm 属性）
- `@ResponseEncrypt` - 响应加密注解（支持 algorithm 属性）
- `@ApiSign` - 接口签名与防重放注解
- `Algorithm` - 加密算法枚举 (RSA/SM2/AES/SM4/BASE64)
- `NonceStore` / `CacheNonceStore` - 防重放的 nonce 存储 SPI 与 ext-cache 实现
- `ApiCryptoAutoConfiguration` - 自动配置类
- `CryptoStrategy` - 加密策略接口及实现

**包结构**: `ext.library.apicrypto`

#### 设计约束

**职责**：在接口边界做请求解密与响应加密：`@RequestDecrypt` / `@ResponseEncrypt`（均可指定 `algorithm`），`Algorithm` 枚举把算法映射到 `CryptoStrategy` 实现——现有值 `RSA`、`SM2`、`AES`、`SM4`、`BASE64`（`BASE64` 只是编解码，不是加密），由 `ApiCryptoAutoConfiguration` 装配。

**边界**：算法实现来自 [ext-crypto](../ext-crypto/AGENTS.md)，本模块只负责接口切入、报文结构（请求体解密的字段位置）与策略分发，不重复实现密码学；`spring-webmvc` 为接口侧依赖，servlet 能力经 [ext-servlet](../ext-common/AGENTS.md#ext-servlet) 取得——本模块不直接声明 `jakarta.servlet-api`，该依赖由消费方的 servlet 容器提供，自动配置按类名探测 servlet API。解密通过 MVC 的 `RequestBodyAdvice` 拿到 `HttpInputMessage`（读完返回新消息，不影响下游绑定）；**在 advice 之外裸读 `getInputStream()` 会耗尽流**，那种场景（过滤器、握手拦截器等）要走 [ext-servlet](../ext-common/AGENTS.md#ext-servlet) 的 `RepeatableRequestWrapper`。

**接口签名与防重放**：`@ApiSign` 走过滤链（`ApiSignHandler`），不是切面——验签必须发生在请求体绑定之前，且要能直接返回错误响应。报文约定：`X-Timestamp`（秒）、`X-Nonce`、`X-Sign`；签名 = `HMAC-SHA256(secret, method + "\n" + uri + "\n" + query + "\n" + timestamp + "\n" + nonce + "\n" + body)`，`uri` 含 context-path，`query` 是未解码的原始查询串（无参数时为空串），`X-Sign` 按小写 hex 比较——**四段少任何一段都可被篡改**：不绑 method/uri 的签名能打到别的接口，不带 query 的签名挡不住 GET 参数被改。要求 body 可重复读，复用同一个 `RepeatableRequestWrapper`。时间窗由 `ext.api-crypto.sign.max-skew-seconds`（默认 300）控制，nonce 存活时长就是时间窗；nonce 在**验签通过之后**才占，否则未认证请求能灌满缓存、客户端重试还会被误判成重放。`ext.api-crypto.sign.secret` 必填（全局单密钥，无 AppKey 租户隔离），`enabled` 默认 `true`；还要开 `ext.servlet.repeatable-request.enabled=true`，否则签名读不到 body。

**nonce 存储**：`NonceStore` SPI 的默认实现 `CacheNonceStore` 只是 `CacheStrategy.putIfAbsent` 的转发，**后端由 ext-cache 统一选型**（`ext.cache.cache-storage`：Caffeine / REDIS / POSTGRES / L2），本模块不再有 `nonce-storage` 这类重复配置。各后端本身就是原子操作（Caffeine `asMap().putIfAbsent`、Redis `SET NX PX`、PostgreSQL `INSERT … ON CONFLICT DO NOTHING`），不需要 `get` + `put` 拼接。单机 Caffeine 够用，**多实例必须选 REDIS / POSTGRES / L2**，否则各实例各记各的，重放防护失败。

### ext-resilience

限流与降级模块，提供：

- `@RateLimit` - 令牌桶限流注解（`key` 支持 SpEL、`limit` 桶容量、`permits` 单次令牌数、`window` 补满周期）
- `RateLimitAspect` - 限流切面
- `RateLimitKeyGenerator` - 限流键生成器（SpEL）
- `RateLimitException` - 触发限流时抛出，携带剩余令牌数与 `Retry-After`
- `@Degrade` - 熔断降级注解（`minCalls` / `failureRate` / `window` / `openDuration` / `fallback` / `executor` / `timeoutMillis` / `critical`）
- `DegradeAspect` - 降级切面，熔断打开时调降级方法；配了 `executor` 则在隔离线程池里执行并超时放弃
- `CircuitBreaker` - 进程内熔断状态机（CLOSED / OPEN / HALF_OPEN）
- `CircuitOpenException` - 熔断且无降级方法时抛出
- `IsolationTimeoutException` - 隔离线程池执行超时时抛出
- `@Retry` - 失败重试注解（`maxAttempts` / `initialDelayMillis` / `multiplier` / `jitter` / `retryOn` / `assumeIdempotent`）
- `RetryAspect` - 重试切面，指数退避 + 抖动

**包结构**: `ext.library.resilience`（子包 `ratelimit` / `degrade` / `retry` / `config`）

令牌桶走 [ext-cache](../ext-infra/AGENTS.md#ext-cache) 的 `CacheStrategy#tryAcquire`，后端跟随 `ext.cache.storage`，本模块没有自己的 `store-type`。

#### 设计约束

**限流是令牌桶，不是固定窗口计数**：`limit` 是桶容量（允许的瞬时突发量），`window` 是把空桶补满的时长，平滑速率 = `limit / window`。固定窗口在两个窗口交界处能放过 2×limit，令牌桶不会。后端用 GCRA 等价实现（只存一个「理论到达时间」），三后端各自原子：Caffeine `synchronized` + Caffeine 容器、Redis Lua、PostgreSQL 条件 `UPDATE … RETURNING`。

**`increment` 仍保留**：它是固定窗口计数原语，锁与其它计数器在用；限流不再用它。

**熔断是进程内状态，不做分布式熔断**：`CircuitBreaker` 每个实例各自统计，一次网络抖动打穿所有实例才是常态，全局一致只会让故障传播更快。统计窗口固定，`minCalls` 不到不熔断（避免冷启动一次失败就打开）；`OPEN` 到期进 `HALF_OPEN` 只放**一个**探测请求，探测在途时其它请求继续降级。

**只有限流需要分布式，熔断不需要**：限流配额是全局资源，进程内桶在 N 个实例下等于放行 N×limit，所以令牌桶走 `CacheStrategy#tryAcquire`，多实例共用一个桶。熔断保护的是**本实例**——下游挂了每个实例看到的现象一样，各自统计几十毫秒内就会自己熔断，不需要谁通知谁；而在熔断路径上加一次远端往返，恰好是在下游和缓存都不稳的时候把降级决策也搭进去。共享半开探测还会退化成「全局只有一个实例探路」，其余实例白等一个 `openDuration`。

**降级只对熔断态生效**：原方法自己抛的异常照原样上抛，不吞进降级方法——把业务异常降级会掩盖真实故障。`fallback` 必须是同 bean 方法，参数表同原方法或空参，返回类型须兼容原方法。

**隔离与超时必须在同一层**：`timeoutMillis` 只有配了 `executor` 才有意义，调用线程上没法中断一个同步调用；线程池大小与拒绝策略由业务定，本模块不代管。`critical = true`（核心接口）**禁止**配 `fallback`，同时在首次调用时报配置错：主流程拿到过期的兜底数据比直接报错更危险。

**重试只给幂等接口用**：方法或所在类必须带 `@Idempotent` / `@RepeatSubmit`，否则首次调用报配置错；`assumeIdempotent` 只留给第三方接口本身幂等这类没法标注解的场景。退避带抖动（默认 50%），否则同一时刻失败的一批调用会齐步重试、自己造一个新尖峰。

**HTTP 语义由异常自带**：`RateLimitException` / `CircuitOpenException` / `IsolationTimeoutException` 实现 `HttpAware`，引了 `ext-mvc` 时分别落成 `429`（带 `Retry-After`）/ `503` / `504`；纯 servlet 场景由使用方自己的异常处理器映射。

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

ext-json + ext-crypto + ext-cache
    |
    +---> ext-api-crypto

ext-redis + ext-postgres（ext-cache 的分布式后端，由 ext.cache.cache-storage 选）
    |
    +---> ext-cache

ext-cache
    |
    +---> ext-resilience

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

// 限流（令牌桶：平均 100 次/分钟，允许瞬时 100 个）
@RateLimit(key = "#request.getRemoteAddr()", limit = 100, window = 1, timeUnit = TimeUnit.MINUTES)
public Result<Void> sendCode(String phone) { ... }

// 熔断降级（10 秒窗口内 20 次调用失败率过 50% 就熔断 30 秒，期间走 fallback）
@Degrade(minCalls = 20, failureRate = 50, window = 10, openDuration = 30, fallback = "fallbackRecommend")
public List<Item> recommend(long userId) { ... }

// 操作审计
@OperationLog(module = "订单", type = OperationType.INSERT, desc = "提交订单 #orderId")
public Result<Void> submit(Order order) { ... }
```

## 相关文件

- `ext-captcha/src/main/java/ext/library/captcha/` - 验证码源码
- `ext-idempotent/src/main/java/ext/library/idempotent/` - 幂等性源码
- `ext-api-crypto/src/main/java/ext/library/apicrypto/` - API 加密源码
- `ext-resilience/src/main/java/ext/library/resilience/` - 限流与降级源码
- `ext-audit/src/main/java/ext/library/audit/` - 操作审计源码

## 变更记录

| 日期 | 变更内容 |
|------|----------|
| 2026-01-19 | 初始化 AGENTS.md 文档 |
| 2026-10-10 | ext-trans 与 ext-desensitize 并入 ext-common/ext-json；新增 ext-ratelimit（限流）与 ext-audit（操作审计） |
| 2026-11-11 | ext-api-crypto 新增 `@ApiSign` 接口签名与防重放（nonce 存储走 ext-cache 的 `putIfAbsent`，后端由 `ext.cache.cache-storage` 选 Caffeine / Redis / PostgreSQL / L2） |
| 2026-11-12 | ext-ratelimit 改名 ext-resilience：限流从固定窗口计数改为令牌桶（`CacheStrategy#tryAcquire`，GCRA 等价实现），并新增熔断降级（`@Degrade` + 进程内 `CircuitBreaker`） |
