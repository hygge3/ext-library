# ext-captcha

> 图形验证码模块 - 提供验证码生成与校验功能

## 简介

`ext-captcha` 是 ext-library 的验证码模块，支持随机字符和数学表达式两种验证码类型，内置缓存支持。

## 快速开始

### Maven

```xml
<dependency>
    <groupId>ext.library</groupId>
    <artifactId>ext-captcha</artifactId>
</dependency>
```

### Gradle

```groovy
implementation("ext.library:ext-captcha")
```

## 依赖说明

| 依赖 | 说明 |
|------|------|
| ext-cache | 验证码缓存支持 |

## 功能特性

- 随机字符验证码
- 数学表达式验证码
- 验证码缓存
- 内置字体支持
- 多种输出格式

## 配置项

| 配置项 | 默认值 | 说明 |
|-------|-------|------|
| ext.captcha.captcha-type | RANDOM | RANDOM (随机) / MATH (算术) |
| ext.captcha.cache-name | captcha:cache#5m | 缓存名称，5分钟过期 |

## 核心类说明

| 类名 | 说明 |
|------|------|
| `ICaptchaService` | 验证码服务接口 |
| `ICaptchaCache` | 验证码缓存接口 |
| `CaptchaDraw` | 验证码绘制接口 |
| `RandomCaptchaDraw` | 随机字符验证码 |
| `MathCaptchaDraw` | 数学表达式验证码 |

## 使用示例

### 注入验证码服务

```java
@Autowired
private ICaptchaService captchaService;
```

### 生成验证码

```java
// 生成 Base64 格式
String base64 = captchaService.generateBase64(uuid);

// 生成字节数组
byte[] bytes = captchaService.generateBytes(uuid);

// 生成 ResponseEntity
ResponseEntity<Resource> response = captchaService.generateResponseEntity(uuid);
```

### 校验验证码

```java
boolean valid = captchaService.validate(uuid, userInputCaptcha);
```

### 控制器示例

```java
@RestController
@RequestMapping("/captcha")
public class CaptchaController {

    @Autowired
    private ICaptchaService captchaService;

    @GetMapping("/image")
    public ResponseEntity<Resource> getCaptcha(@RequestParam String uuid) {
        return captchaService.generateResponseEntity(uuid);
    }

    @PostMapping("/verify")
    public R<Boolean> verify(@RequestParam String uuid,
                             @RequestParam String code) {
        boolean valid = captchaService.validate(uuid, code);
        return R.ok(valid);
    }
}
```

### 自定义缓存

```java
@Component
public class CustomCaptchaCache implements ICaptchaCache {

    @Override
    public void put(String key, String value) {
        // 自定义缓存逻辑
    }

    @Override
    public String get(String key) {
        // 自定义获取逻辑
        return null;
    }

    @Override
    public void remove(String key) {
        // 自定义删除逻辑
    }
}
```

## 许可证

[Apache License 2.0](../../LICENSE)
