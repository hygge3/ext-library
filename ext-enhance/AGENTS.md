[根目录](../AGENTS.md) > **ext-enhance**

# ext-enhance - 业务增强层

> 提供验证码、幂等性、API加密等业务增强功能

## 层级职责

ext-enhance 提供常见业务场景的增强功能，通过注解和 AOP 实现无侵入式的功能扩展。

## 子模块列表

| 模块 | 描述 | 主要依赖 |
|------|------|----------|
| ext-captcha | 图形验证码生成与校验 | ext-cache |
| ext-idempotent | 接口幂等性控制 | ext-redis, caffeine(可选) |
| ext-api-crypto | API 请求/响应加解密 | ext-json, ext-crypto |

> 字段翻译 (`ext.library.trans`) 与数据脱敏 (`ext.library.desensitize`) 已并入 ext-common/ext-json。

## 模块详情

### ext-captcha

图形验证码模块，提供：

**验证码绘制**:
- `CaptchaDraw` - 验证码绘制接口
- `RandomCaptchaDraw` - 随机字符验证码
- `MathCaptchaDraw` - 数学表达式验证码
- `BackgroundDraw` - 背景绘制
- `InterferenceDraw` - 干扰线绘制
- `CurveInterferenceDraw` - 曲线干扰

**核心组件**:
- `ICaptcha` - 验证码核心接口
- `ICaptchaService` - 验证码服务接口
- `CaptchaCache` - 验证码缓存
- `CaptchaVO` - 验证码响应对象

**包结构**: `ext.library.captcha`

### ext-idempotent

幂等性控制模块，提供：

- `@Idempotent` - 幂等性注解
- `IdempotentAspect` - 幂等性切面
- `IdempotentKeyGenerator` - 幂等键生成器
- `IdempotentKeyStore` - 幂等键存储接口
- `RedisIdempotentKeyStore` - Redis 存储实现
- `InMemoryIdempotentKeyStore` - 内存存储实现

**包结构**: `ext.library.idempotent`

### ext-api-crypto

API 加解密模块，提供：

- `@RequestDecrypt` - 请求解密注解（支持 algorithm 属性）
- `@ResponseEncrypt` - 响应加密注解（支持 algorithm 属性）
- `Algorithm` - 加密算法枚举 (RSA/SM2/AES/SM4/DES/BASE64)
- `ApiCryptoAutoConfiguration` - 自动配置类
- `CryptoStrategy` - 加密策略接口及实现

**包结构**: `ext.library.apicrypto`

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
```

## 相关文件

- `ext-captcha/src/main/java/ext/library/captcha/` - 验证码源码
- `ext-idempotent/src/main/java/ext/library/idempotent/` - 幂等性源码
- `ext-api-crypto/src/main/java/ext/library/apicrypto/` - API 加密源码

## 变更记录

| 日期 | 变更内容 |
|------|----------|
| 2026-01-19 | 初始化 AGENTS.md 文档 |
| 2026-10-10 | ext-trans 与 ext-desensitize 并入 ext-common/ext-json |
