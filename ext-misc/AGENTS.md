[根目录](../AGENTS.md) > **ext-misc**

# ext-misc - 杂项工具层

> 提供 HTTP 客户端、二维码生成等独立工具

## 层级职责

ext-misc 包含相对独立的工具模块，不属于特定业务领域，可按需引入。

## 子模块列表

| 模块 | 描述 | 主要依赖 |
|------|------|----------|
| ext-http | HTTP 客户端封装 | ext-tool |
| ext-useragent | User-Agent 解析器 | ext-tool |
| ext-qrcode | 二维码生成工具 | ext-tool, zxing |
| ext-logback | 日志脱敏，Logback 脱敏转换器 | ext-json, logback-classic |
| ext-i18n | 国际化，资源文件与数据库双源翻译 | ext-core, ext-json, spring-boot-starter-jdbc(可选) |
| ext-oss | 对象存储，支持本地文件系统、S3 协议与 SSH/SFTP | ext-core；可选 AWS SDK v2、sshd-sftp |

## 模块详情

### ext-http

HTTP 客户端模块，提供：

- HTTP 请求封装
- 常用 HTTP 方法支持 (GET, POST, PUT, DELETE 等)
- 请求/响应处理
- 同步和异步请求支持

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

- `QrCode` - 二维码生成工具类
- 支持自定义尺寸、颜色、Logo
- 多种输出格式 (PNG, JPG, Base64 等)

**包结构**: `ext.library.qrcode`

**使用示例**:
```java
// 生成二维码
byte[] qrImage = QrCode.from("https://example.com")
    .size(300)
    .toBytes();

// 生成 Base64（可直接用于 HTML img 标签）
String base64 = QrCode.from("内容").toBase64();

// 识别二维码
String content = QrCode.read(inputStream);
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

<appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
    <encoder>
        <pattern>%d{HH:mm:ss.SSS} %-5level [%thread] %logger{36} - %maskedMsg%n</pattern>
    </encoder>
</appender>
```

> converter 由 logback XML 实例化，拿不到 Spring 容器，所以规则由 `LogMaskAutoConfiguration` 启动时写入 `LogMaskRegistry`。规则表为空时 converter 直接放行。

### ext-i18n

国际化模块，把翻译文案拆成多个来源，查不到就回退到下一个：

- **管理器**: `I18nMessageManager` 按 `Locale` 顺序查询各翻译源，支持占位符参数、`find` 返回 `Optional`、`contains` 判定与 `clear` 清缓存
- **来源 SPI**: `TranslationSource` 接口（`load(Locale)` / `name()` / `order()`），`order()` 越小越先查询
- **资源文件源**: `ResourceBundleTranslationSource` 读 classpath 下的 `ResourceBundle`
- **数据库源**: `JdbcTranslationSource` 读翻译表，查询走语言降级链 `zh-CN` → `zh` → `ROOT`，按降级顺序合并
- **缓存**: 按语言缓存翻译表，`ext.i18n.cache-ttl` 到期重载，便于翻译变更后自动生效

**包结构**: `ext.library.i18n`

**配置前缀**: `ext.i18n`

**核心类**:
- `I18nMessageManager` - 翻译管理器
- `TranslationSource` - 翻译来源 SPI
- `ResourceBundleTranslationSource` - 资源文件翻译源
- `JdbcTranslationSource` - 数据库翻译源
- `I18nProperties` / `I18nAutoConfiguration` - 配置属性与自动配置

**使用示例**:
```java
String msg = i18nMessageManager.get("user.notfound");                 // 默认语言
String en = i18nMessageManager.get("user.notfound", Locale.US);
String hi = i18nMessageManager.get("order.count", Locale.US, 3);       // 带参数
```

**配置示例**:
```yaml
ext:
  i18n:
    enabled: true
    default-locale: zh-CN
    resource-bundle-base-name: i18n/messages
    jdbc-enabled: true            # 需要容器中存在 JdbcClient，未引入时静默关闭
    table-name: ext_i18n_message
    schema-init: true             # 启动时自动建表
    cache-ttl: 5m
```

> 翻译表结构：`ext_i18n_message(id, message_key, locale, message)`，`(message_key, locale)` 唯一。

### ext-oss

对象存储模块，本地文件系统、S3 协议与 SSH/SFTP 各一份实现，业务方只依赖门面类：

- **门面**: `OssTemplate` - put/putBytes/putFile/copy/move/get/getString/getBytes/stat/delete/exists/list/presign/presignPut/generateKey
- **SPI**: `ObjectStorage` 接口，三种后端各实现一份；`copy` / `move` 带跨桶重载，无桶概念的实现抛 `UnsupportedOperationException`
- **本地实现**: `LocalObjectStorage` 写本地目录，可配置对外访问前缀
- **S3 实现**: `S3ObjectStorage` 基于 AWS SDK for Java v2（`S3Client` + `S3Presigner`），超过 `part-size` 走分片上传，复制走服务端 `CopyObject`
- **SFTP 实现**: `SftpObjectStorage` 基于 Apache MINA SSHD，远端目录当桶用；上传写临时文件再 `rename`，连接池复用会话
- **返回值**: `OssObject`（bucket/key/contentType/size/content 流，实现 `Closeable`）、`ObjectMeta`（stat 结果，不含内容）、`ObjectItem`（列表项）
- **实现选择**: 按 `ext.oss.type` 选 `local` / `s3` / `sftp`；厂商类型只出现在自动配置类方法体内部，且各方 SDK 都是 `compileOnly`，容器里没有对应 SDK 时其他模式照样能启动
- **不在范围**: 跨实现复制（local → s3 属于数据迁移）、CDN 回源、图片处理

**包结构**: `ext.library.oss`

**配置前缀**: `ext.oss`

**核心类**:
- `OssTemplate` - 对象存储门面
- `ObjectStorage` - 存储 SPI
- `LocalObjectStorage` / `S3ObjectStorage` / `SftpObjectStorage` - 三种实现
- `OssObject` / `ObjectMeta` / `ObjectItem` - 返回模型
- `OssProperties` / `OssAutoConfiguration` - 配置属性与自动配置

**使用示例**:
```java
String key = ossTemplate.generateKey("avatar", "logo.png");
String url = ossTemplate.put(key, Files.newInputStream(file), size, "image/png");

ossTemplate.copy(key, "backup/" + key);                  // 服务端复制，数据不过进程
ossTemplate.move(key, "archive/" + key);                 // copy + delete
String url2 = ossTemplate.presign(key);                  // 预签名链接
ObjectMeta meta = ossTemplate.stat(key);
byte[] bytes = ossTemplate.getBytes(key);
```

**配置示例**:
```yaml
ext:
  oss:
    enabled: true
    type: LOCAL                    # LOCAL / S3 / SFTP
    path: ./data/oss               # 本地存储根目录
    url-prefix: ""                 # 本地存储对外访问前缀
    bucket: ext-library
    # 以下为 type=s3 时必填
    endpoint: https://s3.example.com
    access-key: ${OSS_ACCESS_KEY}
    secret-key: ${OSS_SECRET_KEY}
    region: us-east-1
    auto-create-bucket: true
    part-size: 10485760            # 分片大小，默认 10MB
    presign-expiry: 1h             # 预签名链接有效期
    path-style-access: true        # 自建端点几乎都要；AWS 官方端点关掉
    chunked-encoding: true         # 旧网关不认 aws-chunked 时关掉
    # 以下为 type=sftp 时使用
    sftp:
      host: localhost
      port: 22
      username: root
      password: ${SFTP_PASSWORD}   # 与 private-key 二选一
      base-path: /data/oss
      access-prefix: https://static.example.com   # 配了 presign 才可用
      pool-size: 5
```

## 依赖关系

```
ext-tool
    |
    +---> ext-http
    |
    +---> ext-useragent
    |
    +---> ext-qrcode (+ zxing)

ext-json + logback-classic
    |
    +---> ext-logback

ext-core + ext-json
    |
    +---> ext-i18n (+ spring-boot-starter-jdbc 可选)

ext-core (+ software.amazon.awssdk:s3 或 sshd-sftp，按需 compileOnly)
    |
    +---> ext-oss
```

## 相关文件

- `ext-http/src/main/java/ext/library/http/` - HTTP 客户端源码
- `ext-useragent/src/main/java/ext/library/useragent/` - User-Agent 解析源码
- `ext-qrcode/src/main/java/ext/library/qrcode/` - 二维码源码
- `ext-logback/src/main/java/ext/library/logback/` - 日志脱敏源码
- `ext-i18n/src/main/java/ext/library/i18n/` - 国际化源码
- `ext-oss/src/main/java/ext/library/oss/` - 对象存储源码

## 变更记录

| 日期 | 变更内容 |
|------|----------|
| 2026-10-10 | 新增 ext-oss 模块（对象存储，本地文件系统与 S3 双实现） |
| 2026-10-11 | ext-oss 换 AWS SDK v2 并新增 SFTP 后端、copy/move/stat/prepresignPut |
| 2026-10-10 | 新增 ext-i18n 模块（国际化，资源文件与数据库双源翻译） |
| 2026-10-10 | 新增 ext-logback 模块（日志脱敏） |
| 2026-01-26 | 重构 ext-qrcode：修复 API 命名(form→from)、NPE 问题、MIME 类型、添加参数验证 |
| 2026-01-19 | 初始化 AGENTS.md 文档 |
