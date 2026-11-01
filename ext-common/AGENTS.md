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
- **重试机制**: `RetryUtils` 重试工具
- **函数式接口**: `CheckedFunction`, `CheckedConsumer`, `CheckedSupplier` 等支持异常的函数式接口
- **延迟加载**: `Lazy`, `Once` 延迟初始化工具
- **字典接口**: `Dict` 枚举字典接口，`DictUtil` 转换工具

**包结构**: `ext.library.tool`

#### 设计约束

**职责**：不含业务语义的基础工具集，供上层任意模块使用：ID 生成（`SnowflakeId`、`MongoObjectId`）、树构建 `TreeBuilder`、版本比较 `Version`、重试 `RetryUtils`（`doWithRetry`，可配最大重试次数与间隔）、支持受检异常的函数式接口（`CheckedFunction`/`CheckedConsumer`/`CheckedSupplier` 等）、延迟初始化 `Lazy`/`Once`、枚举字典 `Dict`/`DictUtil`、字符串工具 `StringUtil`（含 `underlineToCamelCase`、`escapeHtml`）。

**边界**：不依赖任何其他 ext 模块；除根构建统一施加的 `slf4j-api` 与 `spring-boot-starter` 外不引入第三方依赖——需要第三方库的工具应落到别的模块，而不是这里。

**约定**：包 `ext.library.tool`，无自动配置、无 Spring 容器依赖，可被非 Spring 代码使用。

**归属**：本模块**不是** Spring / Spring Boot 工具类的家——`SpringUtil`（静态取 Bean）、`BeanUtil`（属性拷贝）、`ExtExpressionEvaluator`（SpEL）都在 [ext-core](#ext-core)，因为它们需要 Spring 容器上下文，而本模块要保持「非 Spring 代码可用」。同理，`@Xss` 这类 Web 校验器在 [ext-mvc](../ext-web/AGENTS.md#ext-mvc)，纯字符串转义（`StringUtil.escapeHtml`）留在本模块，不需要单独开一个 HTML 模块。

### ext-core

核心功能模块，提供：

- **SpEL 支持**: `ExtExpressionEvaluator` 增强的表达式求值器
- **验证**: 与 spring-validation 集成
- **对象映射**: 基于 Spring `BeanUtils` 的属性拷贝
- **AOP**: AspectJ 支持

**包结构**: `ext.library.core`

#### 设计约束

**职责**：全库共用的核心机制：SpEL 表达式求值（`ExtExpressionEvaluator`）、基于 Spring `BeanUtils` 的属性拷贝（`BeanUtil`）、AOP / AspectJ 支持、线程池与异步执行（`ThreadPoolConfiguration`、`AsyncConfiguration`，前缀 `ext.thread-pool`）、`SpringUtil` 静态容器访问、发布订阅 SPI `PubSub`（`publish` / `subscribe`，由 [ext-redis](../ext-infra/AGENTS.md#ext-redis) 与 [ext-postgres](../ext-infra/AGENTS.md#ext-postgres) 各实现一份）。

**边界**：只依赖 `ext-tool`。不依赖 `ext-json`，不引入任何 web / servlet 组件——它被非 Web 应用使用，拖入 web 栈会改变消费方的启动形态。

### ext-json

JSON 处理模块，提供：

- Jackson ObjectMapper 增强配置
- JsonPath 查询支持
- JSON 序列化/反序列化工具类
- 字段翻译 (`ext.library.trans`)：`@Translate` 注解 + `TranslatorRegistry` + Jackson `TranslationHandler`
- 数据脱敏 (`ext.library.desensitize`)：`@Sensitive` 注解 + `DesensitizeStrategy` + `DesensitizeUtil`

**包结构**: `ext.library.json`, `ext.library.trans`, `ext.library.desensitize`

#### 设计约束

**职责**：三件事——

- Jackson `ObjectMapper` 增强配置、JSON 序列化/反序列化工具、JsonPath 查询。
- **字段翻译** `ext.library.trans`：`@Translate` 注解 + `TranslatorRegistry` + Jackson `TranslationHandler`，在序列化期把编码翻成展示值。
- **数据脱敏** `ext.library.desensitize`：`@Sensitive` + `SensitiveStrategy` + `DesensitizationUtil`。

**边界**：依赖 `ext-tool`、`starter-json`、`json-path`；不做 HTTP 处理、不落库。

**包结构**：翻译与脱敏是同一层的两个兄弟子包，都直接挂在 `ext.library` 下——`ext.library.trans.*` 与 `ext.library.desensitize.*`，不嵌进 JSON 相关包（它们是「数据口径」，JSON 只是生效时机），也不合并成一个大包（两者使用场景独立）。

**决策**：脱敏策略是全库唯一口径。`ext-logback` 的日志脱敏直接复用 `DesensitizeStrategy`，所以同一个手机号在日志里和接口返回里被遮蔽成同一形态。新增规则改这里，不要在调用方另写遮蔽逻辑。

### ext-servlet

servlet 基座，把「接口返回什么形状、分页怎么传、错误怎么回」这类与框架版本无关的契约集中定义：

- **弱模型** (`ext.library.servlet.response`)：`R`、`PageQuery`、`PageResult`、`SelectData`——Web 层与 infra 层共用同一份，不再各自持有副本
- **servlet 工具** (`ext.library.servlet.util`)：`ServletUtil`（请求/响应/参数/头/Cookie/IP/写出 JSON）、`RepeatableRequestWrapper`（请求体可重复读）
- **错误信封** (`ext.library.servlet.exception`)：`ErrorEnvelope`（HTTP 状态 + `R` 的配对）、`ErrorLog`（统一错误日志口径）
- **配置**：`ext.servlet.repeatable-request.enabled`（默认 `false`，开启后 body 整段驻留内存）

**包结构**: `ext.library.servlet`

**注意**：`R` / `ServletUtil` / 分页模型只在 `ext-servlet` 定义一份。下层模块需要就直接依赖它，不要因为「下层不能依赖 web」再拄一份同名类。

#### 设计约束

**定位**：Web 层之下、任何层都可能需要的那部分"请求 / 响应"能力——弱模型（纯数据）、servlet 工具、错误信封、请求体处理、请求标识。它**不含任何 Spring MVC 语义**：没有 `@RestControllerAdvice`、没有 `ResponseBodyAdvice`、没有 `HandlerMethod`、没有参数解析器。

##### 内容

###### ① 弱模型

- **统一响应体 `R<T>`**（`code` / `msg` / `data`，状态码取自 `ext-tool` 的 `ResponseCode`）——从 `ext-mvc` 抽出。
- **分页与选项模型** `PageQuery`（`page` / `size` / `total`）、`PageResult<T>`（`records` / `total` / `pages` + `empty(query)`）、`SelectData`（下拉选项）——从 `ext-mvc` 抽出。`ext-jdbc` 里那份同名副本随之删除：它的类注释已经写明"由于 infra 不能反向依赖 web，此处独立定义一份"——基座建成后这个理由不存在了，分页类型只保留一份，Web 层不再需要手工转换。

###### ② servlet 工具 `ServletUtil`

- 现有：request / response / session / requestAttributes 取值；param、header、cookie 读写；`getIpAddr()`（走 `X-Forwarded-For` → `X-Real-IP` 链）；`getUA()`。
- 补齐 `getRequestOrNull()` / `getResponseOrNull()`：非 Web 线程返回 `null`，调用方不必再手写 `instanceof ServletRequestAttributes` 样板。
- 补齐 `writeJson(response, status, body)`：让 Filter、WebSocket 握手拦截器这类 servlet 级代码也能输出 `R` 结构——今天只有 MVC 的 advice 能产出统一信封。

###### ③ 错误信封与异常映射

把「异常 → (`HttpStatus`, `R`)」的信封组装与统一日志口径收到这里：`ErrorEnvelope`（HTTP 状态与响应体的配对，含业务码不进 HTTP 状态的判定）与 `ErrorLog`（模块标识 + URI + 消息）。`ext-mvc` 的 `GlobalExceptionHandler`、`ext-security` 的 `SecurityExceptionHandler` 只声明「哪种异常归我管」与对外消息，不各写一遍组装。这是安全层为了返回 `R` 而反向依赖 Web 层的根因之一。

###### ④ 请求体可重复读

`RepeatableRequestWrapper` + 可选 `Filter`：`ext-api-crypto` 解密要读 body、`ext-audit` 记录入参要读 body、`ext-idempotent` 算指纹要读 body——今天各读各的，一经读取流即空。

###### ⑤ 请求标识

`X-Request-Id` 的透传 / 生成与对应的 MDC 字段约定（供 `ext-logback` 的日志 pattern 使用）。**新能力：字段名、是否默认开启尚未确认**，实施前需先定——**当前未实现**，模块里尚无相关代码。

##### 归属判定

放进来的条件，缺一不可：

1. Web 层之下的模块确实需要它（否则留在 `ext-mvc`）；
2. 它不携带 MVC 框架语义（否则无论谁需要都留在 `ext-mvc`）。

满足这两条的典型形态就是"弱模型 + servlet 工具 + 信封"，新增内容先对照这两条再决定放哪。

明确**不**在这里：`@RestControllerAdvice` / `ResponseBodyAdvice` / `GlobalExceptionHandler` / `GlobalResponseHandler` / `IgnoreRestWrapper` / `HandlerMethodUtil` / `BodyParam` 与 `PageQueryArgumentResolver`（MVC 绑定机制）/ 校验注解与校验器（含 `@Xss`）/ HTML 转义工具（通用转义在 `ext-tool` 的 `StringUtil`）/ OpenAPI / `WebInvokeTimeInterceptor` / `StartupListener`（容器级，与 servlet 无关）。

##### 依赖

`ext-tool` 为 `api`；`ext-json` 为 `api`（`writeJson` 的序列化口径与全局一致，同层依赖合法）；`spring-web`（`HttpStatus`、`RequestContextHolder`）为 `api`；`jakarta.servlet-api` 为 `compileOnly`，由消费方的 servlet 容器提供。

##### 为什么单独成模块

安全层的拦截器与异常处理器需要 servlet 能力；若这些类型留在 `ext-mvc`（Web 层），安全层就必须反向依赖 Web 层——这是原图唯一一条越层边。把基座放在 `ext-common`，`ext-mvc` 与 `ext-security` 都向下依赖它，依赖方向恢复单向；同时不选"下沉进 `ext-core`"，因为 `ext-core` 被几乎所有模块依赖，让它带上 servlet 语义会把 web 基座变成全库默认前提。

### ext-http

HTTP 客户端模块，提供：

- HTTP 请求封装
- 常用 HTTP 方法支持 (GET, POST, PUT, DELETE 等)
- 请求/响应处理

**包结构**: `ext.library.http`

#### 设计约束

**职责**：一个 `HttpUtil` 门面收敛出站 HTTP 调用：常用方法（GET / POST / PUT / DELETE 等）、请求与响应处理、同步与异步两种调用方式。`HttpProperties` 承载 `ext.http.*`：`timeout`（连接/读取超时，默认 `30s`）、`retry-count`（失败重试次数，默认 `3`，即首次之外最多再试 3 次），两者都可按单次调用覆盖。

**边界**：只依赖 `ext-tool`，不引入额外 HTTP 客户端库也不做服务发现/负载均衡；重试用 `ext-tool` 的 `RetryUtils.doWithRetry`（重试次数与间隔取自 `HttpProperties`），不在这里长出一套重试框架。

**约定**：重试只针对可重试的失败（连接超时 / `5xx` / `IOException`），`4xx` 与非幂等方法（非 GET/HEAD 的默认策略）不重试；超时全局默认 `30s`，业务侧按调用传入 `Duration` 覆盖，不要在每个调用点写魔法值。

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

#### 设计约束

**职责**：把 UA 字符串解析成结构化结果：浏览器与版本、操作系统与版本、设备平台（移动 / 桌面 / iOS / Android）、渲染引擎与版本（`UserAgentParser.parse`，结果含 `isMobile()`）。

**边界**：纯解析，只依赖 `ext-tool`；无自动配置、无 Spring 依赖，可当普通工具库使用。空串输入返回 `null`——调用方必须判空（历史 NPE 修复结论）。

**消费者**：[ext-security](../ext-security/AGENTS.md) 用它记录登录设备，因此解析结果的变化会影响多端登录限制的判定。

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

#### 设计约束

**职责**：基于 Google ZXing 的二维码工具：`QrCode.from(content)` 链式 API（`size`、颜色、Logo、输出 PNG / JPG / `byte[]` / Base64），以及 `QrCode.read(inputStream)` 识别。

**边界**：只依赖 `ext-tool` 与 `javase` 版 ZXing；不提供条码以外的格式、不做图片处理管线。

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

#### 设计约束

**职责**：把脱敏口径搬到日志输出上：`%maskedMsg` 转换器 `LogMaskConverter`、单条规则 `LogMaskRule`（正则 + 遮蔽函数，可只遮蔽指定捕获组）、内置规则集 `LogMaskRules`（手机号、身份证、邮箱、银行卡、IPv4、键值对凭据）、静态注册表 `LogMaskRegistry`、开关每条规则的 `LogMaskProperties`（前缀 `ext.logback.mask`）。

**遮蔽口径**：遮蔽函数直接复用 [ext-json](#ext-json) 的 `DesensitizeStrategy`，所以日志里的手机号和接口返回里的手机号是同一形态——不要在这里另写一套遮蔽逻辑。

**关键约束**：converter 由 logback XML 实例化，拿不到 Spring 容器；规则必须在启动时由 `LogMaskAutoConfiguration` 写入 `LogMaskRegistry`。规则表为空时 converter 直接放行，不报错。

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
