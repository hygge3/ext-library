[根目录](../CLAUDE.md) > **ext-captcha**

# ext-captcha 模块文档

## 模块职责

ext-captcha 提供验证码生成服务，支持随机字符、数学运算和表达式验证码。

## 入口与启动

### 自动配置类
- **CaptchaAutoConfig**: 验证码自动配置

## 核心组件

### 1. 配置类 (config/)
- **CaptchaAutoConfig**: 验证码自动配置

### 2. 配置属性 (properties/)
- **CaptchaProperties**: 验证码配置参数

### 3. 服务 (service/)
- **ICaptchaService**: 验证码服务接口
- **CaptchaServiceImpl**: 验证码服务实现

### 4. 核心类 (core/)
- **ICaptcha**: 验证码接口
- **Captcha**: 验证码实现

### 5. 绘制策略 (draw/)
- **CaptchaDraw**: 验证码绘制接口
- **RandomCaptchaDraw**: 随机字符绘制
- **MathCaptchaDraw**: 数学运算绘制
- **Expression**: 表达式绘制
- **BackgroundDraw**: 背景绘制
- **InterferenceDraw**: 干扰绘制
- **CurveInterferenceDraw**: 曲线干扰绘制
- **SmallCharsBackgroundDraw**: 小字符背景绘制

### 6. 枚举 (enums/)
- **CaptchaType**: 验证码类型枚举

### 7. 缓存 (cache/)
- **CaptchaCache**: 验证码缓存

### 8. 视图对象 (vo/)
- **CaptchaVO**: 验证码视图对象

## 关键依赖

- **ext-cache**: 缓存支持
- **spring-web**: Web 支持

## 使用示例

### 生成验证码
```java
@Autowired
private ICaptchaService captchaService;

// 生成随机验证码
CaptchaVO captcha = captchaService.generateCaptcha(CaptchaType.RANDOM);

// 生成数学验证码
CaptchaVO mathCaptcha = captchaService.generateCaptcha(CaptchaType.MATH);

// 生成表达式验证码
CaptchaVO expCaptcha = captchaService.generateCaptcha(CaptchaType.EXPRESSION);
```

### 验证验证码
```java
boolean valid = captchaService.verify(captchaId, userInput);
```

## 常见问题 (FAQ)

### Q: 如何自定义验证码样式？
实现 `CaptchaDraw` 接口并注册为 Bean。

## 相关文件清单

### 主要源码文件
- `src/main/java/ext/library/captcha/config/CaptchaAutoConfig.java`
- `src/main/java/ext/library/captcha/service/ICaptchaService.java`
- `src/main/java/ext/library/captcha/core/ICaptcha.java`
- `src/main/java/ext/library/captcha/draw/CaptchaDraw.java`

### 配置文件
- `src/main/resources/META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`

## 变更记录 (Changelog)

### 2025-12-24
- 创建：模块文档
