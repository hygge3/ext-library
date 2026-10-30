[根目录](../AGENTS.md) > **ext-common**

# ext-common - 通用基础层

> 提供项目基础设施，包括工具类、核心功能、JSON 处理与独立工具模块

## 层级职责

ext-common 是整个项目的基础层，所有其他模块都直接或间接依赖此层。主要职责包括：

- 提供通用工具类和辅助函数
- 定义核心功能和公共抽象
- 封装 JSON 序列化/反序列化能力

## 子模块列表

| 模块 | 描述 | 主要依赖 |
|------|------|----------|
| ext-tool | 通用工具类库 (ID生成、重试、函数式接口等) | 无外部依赖 |
| ext-core | 核心功能 (SpEL表达式、验证、AOP等) | ext-tool, spring-validation |
| ext-json | JSON 处理工具 (Jackson封装、JsonPath、翻译、脱敏) | ext-tool, spring-boot-starter-json, json-path |
| ext-servlet | servlet 基座 (统一响应体、分页模型、servlet 工具、错误信封、请求体重读) | ext-tool, ext-json, spring-web |
| ext-http | HTTP 客户端封装 | ext-tool |
| ext-useragent | User-Agent 解析器 | ext-tool |
| ext-qrcode | 二维码生成工具 | ext-tool, zxing |
| ext-logback | 日志脱敏，Logback 脱敏转换器 | ext-json, logback-classic |

## 模块详情

### ext-tool

通用工具类库，提供：

- **ID 生成**: `SnowflakeId` (雪花算法), `MongoObjectId` (MongoDB风格ID)
- **树形构建**: `TreeBuilder` 用于构建树形数据结构
- **版本处理**: `Version` 版本号比较工具
- **重试机制**: `IRetry`, `RetryCallback` 重试回调接口
- **函数式接口**: `CheckedFunction`, `CheckedConsumer`, `CheckedSupplier` 等支持异常的函数式接口
- **延迟加载**: `Lazy`, `Once` 延迟初始化工具
- **字典接口**: `IDict` 枚举字典接口

**包结构**: `ext.library.tool`

### ext-core

核心功能模块，提供：

- **SpEL 支持**: `ExtExpressionEvaluator` 增强的表达式求值器
- **验证**: 与 spring-validation 集成
- **对象映射**: 基于 Spring `BeanUtils` 的属性拷贝
- **AOP**: AspectJ 支持

**包结构**: `ext.library.core`

### ext-json

JSON 处理模块，提供：

- Jackson ObjectMapper 增强配置
- JsonPath 查询支持
- JSON 序列化/反序列化工具类
- 字段翻译 (`ext.library.trans`)：`@Translate` 注解 + `TranslatorRegistry` + Jackson `TranslationHandler`
- 数据脱敏 (`ext.library.desensitize`)：`@Sensitive` 注解 + `SensitiveStrategy` + `DesensitizationUtil`

**包结构**: `ext.library.json`, `ext.library.trans`, `ext.library.desensitize`

### ext-servlet

servlet 基座，把「接口返回什么形状、分页怎么传、错误怎么回」这类与框架版本无关的契约集中定义：

- **弱模型** (`ext.library.servlet.response`)：`R`、`PageQuery`、`PageResult`、`SelectData`——Web 层与 infra 层共用同一份，不再各自持有副本
- **servlet 工具** (`ext.library.servlet.util`)：`ServletUtil`（请求/响应/参数/头/Cookie/IP/写出 JSON）、`RepeatableRequestWrapper`（请求体可重复读）
- **错误信封** (`ext.library.servlet.exception`)：`ErrorEnvelope`（HTTP 状态 + `R` 的配对）、`ErrorLog`（统一错误日志口径）
- **配置**：`ext.servlet.repeatable-request.enabled`（默认 `false`，开启后 body 整段驻留内存）

**包结构**: `ext.library.servlet`

**注意**：`R` / `ServletUtil` / 分页模型只在 `ext-servlet` 定义一份。下层模块需要就直接依赖它，不要因为「下层不能依赖 web」再拄一份同名类。

### ext-http

HTTP 客户端模块，提供：

- HTTP 请求封装
- 常用 HTTP 方法支持 (GET, POST, PUT, DELETE 等)
- 请求/响应处理

**包结构**: `ext.library.http`

### ext-useragent

User-Agent 解析模块，提供：

- 浏览器识别与版本解析
- 操作系统识别与版本解析
- 设备平台识别（移动/桌面/iOS/Android）
- 渲染引擎识别与版本解析

**包结构**: `ext.library.useragent`

**使用示例**:
```java
UserAgent ua = UserAgentParser.parse(userAgentString);
if (ua != null) {
    ua.getBrowser().getName();  // 浏览器名称
    ua.getOs().getName();       // 操作系统
    ua.isMobile();              // 是否移动设备
}
```

### ext-qrcode

二维码模块，基于 Google ZXing，提供：

- `QrCode` - 二维码生成与识别工具类
- 支持自定义尺寸、颜色、Logo、边距
- 多种输出格式 (PNG, JPG, Base64 等)

**包结构**: `ext.library.qrcode`

**使用示例**:
```java
byte[] qrImage = QrCode.from("https://example.com").size(300).toBytes();
String base64 = QrCode.from("内容").toBase64();   // 可直接用于 HTML img 标签
String content = QrCode.read(inputStream);        // 识别
```

### ext-logback

日志脱敏模块，把 ext-json 的脱敏口径搬到日志输出上：

- `LogMaskConverter` - Logback 转换器，用于 `%maskedMsg` 占位符
- `LogMaskRule` - 单条脱敏规则（正则 + 遮蔽函数），支持只遮蔽指定捕获组
- `LogMaskRules` - 内置规则集：手机号、身份证、邮箱、银行卡、IPv4、键值对凭据
- `LogMaskRegistry` - 静态规则注册表，充当 converter 与 Spring 容器之间的桥
- `LogMaskProperties` - 配置属性，开关每条规则

**包结构**: `ext.library.logback.mask` / `ext.library.logback.config`

**配置前缀**: `ext.logback.mask`

遮蔽函数直接复用 `DesensitizeStrategy`，所以日志里的手机号和接口返回里的手机号遮蔽成同一形态。

**使用示例**（logback.xml）:
```xml
<conversionRule conversionWord="maskedMsg" converterClass="ext.library.logback.mask.LogMaskConverter"/>
```

> converter 由 logback XML 实例化，拿不到 Spring 容器，所以规则由 `LogMaskAutoConfiguration` 启动时写入 `LogMaskRegistry`。规则表为空时 converter 直接放行。

## 依赖关系

```
ext-tool (基础工具)
    |
    +---> ext-core (核心功能)
    |
    +---> ext-json (JSON处理)
    |         |
    |         +---> ext-logback (+ logback-classic)
    |
    +---> ext-http
    |
    +---> ext-useragent
    |
    +---> ext-qrcode (+ zxing)
```

## 相关文件

- `ext-tool/src/main/java/ext/library/tool/` - 工具类源码
- `ext-core/src/main/java/ext/library/core/` - 核心功能源码
- `ext-json/src/main/java/ext/library/json/` - JSON 处理源码
- `ext-json/src/main/java/ext/library/trans/` - 字段翻译源码
- `ext-json/src/main/java/ext/library/desensitize/` - 数据脱敏源码
- `ext-http/src/main/java/ext/library/http/` - HTTP 客户端源码
- `ext-useragent/src/main/java/ext/library/useragent/` - User-Agent 解析源码
- `ext-qrcode/src/main/java/ext/library/qrcode/` - 二维码源码
- `ext-logback/src/main/java/ext/library/logback/` - 日志脱敏源码

## 变更记录

| 日期 | 变更内容 |
|------|----------|
| 2026-10-11 | 收编原 ext-misc 层的 ext-http / ext-useragent / ext-qrcode / ext-logback，ext-misc 层解散 |
| 2026-01-19 | 移除 ext-pom，BOM 模块已迁移至根目录 ext-bom |
| 2026-01-19 | 初始化 AGENTS.md 文档 |
