[根目录](../CLAUDE.md) > **ext-enhance**

# ext-enhance - 业务增强层

> 提供验证码、幂等性、脱敏、翻译、API加密等业务增强功能

## 层级职责

ext-enhance 提供常见业务场景的增强功能，通过注解和 AOP 实现无侵入式的功能扩展。

## 子模块列表

| 模块 | 描述 | 主要依赖 |
|------|------|----------|
| ext-captcha | 图形验证码生成与校验 | ext-cache |
| ext-idempotent | 接口幂等性控制 | ext-redis, caffeine(可选) |
| ext-desensitize | 数据脱敏 (手机号、身份证等) | ext-tool, jackson |
| ext-trans | 字段翻译/转换 (字典翻译等) | jackson |
| ext-api-crypto | API 请求/响应加解密 | ext-json, ext-crypto |

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

### ext-desensitize

数据脱敏模块，提供：

- `@Sensitive` - 敏感字段注解
- `SensitiveStrategy` - 脱敏策略枚举
- `IDesensitizeRule` - 脱敏规则接口
- `SensitiveHandler` - 脱敏处理器
- `DesensitizationUtil` - 脱敏工具类

**包结构**: `ext.library.desensitize`

### ext-trans

字段翻译模块，提供：

- `@Translate` - 字段翻译注解
- `@TranslationType` - 翻译类型注解
- `Translator` - 翻译器接口
- `TranslatorRegistry` - 翻译器注册表
- `TranslationHandler` - Jackson 序列化处理器

**包结构**: `ext.library.trans`

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

ext-tool
    |
    +---> ext-desensitize

jackson-databind
    |
    +---> ext-trans

ext-json + ext-crypto
    |
    +---> ext-api-crypto
```

## 使用示例

```java
// 幂等性
@Idempotent(timeout = 5000)
public void createOrder(OrderDTO order) { ... }

// 脱敏
@Sensitive(strategy = SensitiveStrategy.MOBILE_PHONE)
private String phone;

// 翻译
@Translate(type = "dict", mapper = "status", param = "user_status")
private String statusLabel;

// API 加密
@ResponseEncrypt(algorithm = Algorithm.AES)
public Result getData() { ... }
```

## 相关文件

- `ext-captcha/src/main/java/ext/library/captcha/` - 验证码源码
- `ext-idempotent/src/main/java/ext/library/idempotent/` - 幂等性源码
- `ext-desensitize/src/main/java/ext/library/desensitize/` - 脱敏源码
- `ext-trans/src/main/java/ext/library/trans/` - 翻译源码
- `ext-api-crypto/src/main/java/ext/library/apicrypto/` - API 加密源码

## 变更记录

| 日期 | 变更内容 |
|------|----------|
| 2026-01-19 | 初始化 CLAUDE.md 文档 |
