# ext-library

> Spring Boot 扩展库 - 提供企业级开发所需的通用功能模块

## 项目概述

ext-library 是一个基于 Spring Boot 的扩展库，采用 Gradle 多模块分层结构，为企业级应用开发提供安全、缓存、Web、加密等通用功能支持。

## 目标与定位

为同一技术栈（JDK 25 / Spring Boot 4）上的自有后端项目提供一套**可单模块取用**的通用技术能力——安全会话、加解密、缓存、限流、幂等、审计、实时通道、对象存储等——把这些样板代码从业务工程中移出，一次实现、多个项目复用。

**要解决的问题**：每个新服务都要重写同一批横切能力，且各服务口径不一（脱敏规则、token 会话、限流算法、缓存后端各不相同）。没有统一实现就只能复制粘贴，改进无法回传。

**受众**：作者及其团队在同一技术栈上的自有项目。库以 Apache 2.0 开源形式公开，公开是副产品，不代表对外承诺 API 稳定性。

## 非目标

- **不重复造 Spring Boot / Spring Security 已提供的能力**：能复用其实现就复用（AES 与密码编码走 Spring Security Crypto），本库只做扩展与收敛。
- **不含业务领域模型**：只做技术基础能力；权限与角色数据由业务方实现 `SecurityAuthority` 提供，库不定义用户、订单等领域对象。
- **不支持旧版本运行时**：只跟进 JDK 25 与 Spring Boot 4，不为 JDK 17/21 或 Spring Boot 3 保留兼容分支。
- **不提供全量引入的聚合 starter**：每个模块必须能单独依赖；重后端（Redis / PostgreSQL）一律 `compileOnly` + 类路径探测，不把中间件强加给消费方。

## 设计原则

- 横切能力以**注解 + 切面**接入业务，业务代码不写调用样板。
- 同一口径只实现一次：脱敏策略由 `ext-json` 定义，日志脱敏与接口返回复用同一套策略。
- 可选实现统一用 **SPI + 自动探测**（存储、策略、处理器），配置只选行为，不选类。
- **需要持久化的键值数据统一走 `ext-cache`**（会话、凭据、token、nonce、幂等键、配置快照等）。模块直接依赖 `CacheStrategy`，不直接注入 `RedisUtil` / `PostgresUtil`，也不自建 `*-storage` 选型开关——后端只有 `ext.cache.cache-storage` 一处选。缺 `CacheStrategy` Bean 时启动失败，不静默降级到内存。

## 技术栈

| 技术              | 版本               |
|-------------------|--------------------|
| Java              | 25                 |
| Spring Boot       | 4.1.1              |
| Gradle            | 9.8.1 (Groovy DSL) |
| Bouncy Castle     | 1.86               |
| SpringDoc OpenAPI | 3.1.1              |

## 模块结构图

```mermaid
graph TD
    A["ext-library<br/>(根项目)"] --> BOM["ext-bom<br/>BOM 依赖管理"]
    A --> B["ext-common<br/>通用基础层"]
    A --> C["ext-infra<br/>基础设施层"]
    A --> D["ext-security<br/>安全层"]
    A --> E["ext-crypto<br/>加密层"]
    A --> F["ext-web<br/>Web 层"]
    A --> G["ext-enhance<br/>业务增强层"]
    B --> B2["ext-tool<br/>工具类"]
    B --> B3["ext-core<br/>核心功能"]
    B --> B4["ext-json<br/>JSON 处理"]
    B --> B5["ext-servlet<br/>servlet 基座"]
    B --> B6["ext-http<br/>HTTP 客户端"]
    B --> B7["ext-useragent<br/>User-Agent 解析"]
    B --> B8["ext-qrcode<br/>二维码"]
    B --> B10["ext-trace<br/>链路追踪/上下文透传"]
    C --> C1["ext-redis<br/>Redis 支持"]
    C --> C1b["ext-postgres<br/>PostgreSQL 支持"]
    C --> C2["ext-cache<br/>缓存支持"]
    C --> C3["ext-mail<br/>邮件支持"]
    C --> C1e["ext-job<br/>任务调度"]
    C1 --> C1c["ext-lock<br/>分布式锁"]
    C --> C1f["ext-i18n<br/>国际化"]
    C --> C1g["ext-oss<br/>对象存储"]
    C --> C1h["ext-metrics<br/>运行时指标与告警"]
    F --> F1["ext-accesslog<br/>结构化访问日志"]
    F --> F2["ext-mvc<br/>MVC 支持 (含 OpenAPI)"]
    F --> F3["ext-realtime<br/>SSE + WebSocket"]
    G --> G1["ext-captcha<br/>验证码"]
    G --> G2["ext-idempotent<br/>幂等性"]
    G --> G3["ext-api-crypto<br/>API 加密"]
    G --> G4["ext-resilience<br/>限流与降级"]
    G --> G5["ext-audit<br/>操作审计"]
    click B "./ext-common/AGENTS.md" "查看 ext-common 模块文档"
    click C "./ext-infra/AGENTS.md" "查看 ext-infra 模块文档"
    click D "./ext-security/AGENTS.md" "查看 ext-security 模块文档"
    click E "./ext-crypto/AGENTS.md" "查看 ext-crypto 模块文档"
    click F "./ext-web/AGENTS.md" "查看 ext-web 模块文档"
    click G "./ext-enhance/AGENTS.md" "查看 ext-enhance 模块文档"
```

## 模块索引

| 模块                                     | 类型     | 描述                                           |
|------------------------------------------|----------|------------------------------------------------|
| ext-bom                                  | 独立模块 | BOM 依赖管理，统一管理所有 ext 模块版本        |
| [ext-common](./ext-common/AGENTS.md)     | 分层     | 通用基础层，包含工具类、核心功能、JSON 处理、servlet 基座、HTTP 客户端、UA 解析、二维码、链路追踪 |
| [ext-infra](./ext-infra/AGENTS.md)       | 分层     | 基础设施层，包含 Redis、PostgreSQL、缓存、邮件、锁、任务调度、国际化、对象存储、指标与告警 |
| [ext-security](./ext-security/AGENTS.md) | 独立模块 | 安全认证与授权框架（会话、权限、两步验证、通行密钥） |
| [ext-crypto](./ext-crypto/AGENTS.md)     | 独立模块 | 加密算法工具库 (AES/RSA/SM2/SM3/SM4)           |
| [ext-web](./ext-web/AGENTS.md)           | 分层     | Web 层，包含 MVC、访问日志、链路追踪、实时通信    |
| [ext-enhance](./ext-enhance/AGENTS.md)   | 分层     | 业务增强层，包含验证码、幂等性、API加密、限流与降级、审计  |

## ext-bom

**职责**：唯一的版本约束来源（`java-platform`）。导入 `spring-boot-dependencies` 覆盖 Spring 全家桶，导入 `software.amazon.awssdk:bom` 覆盖 [ext-oss](ext-infra/AGENTS.md#ext-oss) 的 S3 客户端，逐个约束不在任何 BOM 的第三方（bouncycastle、zxing、springdoc、webauthn4j、webp-imageio），并把 27 个 ext 模块的版本一并约束。swagger 三件套（`swagger-core-jakarta` / `swagger-annotations-jakarta` / `swagger-models-jakarta`）**不由本 BOM 约束**，版本必须与 springdoc 传递的完全一致，否则同名注解类会出现两份。消费方只需 `platform('ext.library:ext-bom:<version>')`，再按需引模块、不写版本号。

**优先复用官方 BOM**：第三方自己带 BOM（如 `software.amazon.awssdk:bom`）就 `platform(...)` 导入，不要把它的几十个 artifact 拆成 constraint——逐个约束跟不上官方拆包节奏，还会诱使模块混引不同版本的 service client。

**边界**：不含任何代码，不套 `java-library`；不是聚合 starter——导 BOM 不等于引入模块（见 [非目标](#非目标)）。

**不变量**：

- 第三方版本值只出现在 `gradle.properties`，约束只出现在这里。新增第三方依赖必须先加 `gradle.properties` 键，再加 constraint，否则版本无处收敛。
- 新增模块必须同时登记在 `settings.gradle` 与这里的 constraints，否则不进依赖管理。
- 所有叶子通过根 `build.gradle` 的 `api platform(project(':ext-bom'))` 继承约束；模块 `build.gradle` 不得自写版本号。

**分发**：构建侧只配置了 `publishToMavenLocal`。

## 分层架构

```
+------------------------------------------------------------+
|                       ext-bom                             |  BOM 依赖管理
+------------------------------------------------------------+
|                       ext-enhance                         |  业务增强层
|          (captcha, idempotent, api-crypto, resilience, audit)|
+------------------------------------------------------------+
|                       ext-web                             |  Web 层
|              (accesslog, mvc, realtime, trace)            |
+------------------------------------------------------------+
|     ext-security                 ext-crypto               |  安全/加密层
+------------------------------------------------------------+
|                       ext-infra                            |  基础设施层
|      (redis, postgres, cache, mail, lock, jdbc, job, i18n, oss) |
+------------------------------------------------------------+
|                       ext-common                           |  通用基础层
|     (tool, core, json, servlet, http, useragent, qrcode) |
+------------------------------------------------------------+
```

**依赖流向**: 上层模块可依赖下层模块，同层模块可相互依赖，下层模块不得依赖上层模块。

## 构建与测试

```bash
# 完整构建（编译 + 测试 + 安装到本地仓库）
./gradlew clean build

# 跳过测试
./gradlew clean build -x test

# 运行测试
./gradlew test

# 构建单个模块（含依赖）
./gradlew :ext-common:ext-tool:build

# 只发布到本地仓库
./gradlew publishToMavenLocal

# 生成源码包
./gradlew sourcesJar

# 启动示例应用「轻笔记」（27 个模块全部引入并真实用到；需本地 PostgreSQL + Redis）
./gradlew :example:run
```

示例应用是一个可用的最小项目（验证码登录 / 笔记增删改 / 附件 / 分享 / 看板），每个模块的使用位置见 [example/README.md](./example/README.md)。账号密钥写在 `example/.env`（git 已忽略）。

## 关键约定

### 包命名规范

- 基础包名: `ext.library`
- 模块包名: `ext.library.{module}`
- 示例: `ext.library.crypto`, `ext.library.security`, `ext.library.web`

### 自动配置

- 各模块通过 `*AutoConfig` 类提供 Spring Boot 自动配置
- 配置属性前缀: `ext.{module}`

### 版本管理

所有依赖版本集中在 `gradle.properties`：

- `version` — 项目自身版本，所有 ext 模块共用
- `springBootVersion` — Spring Boot 全家桶版本，通过 `spring-boot-dependencies` BOM 管理所有 `org.springframework.*` 等依赖
- `bouncyCastleVersion` / `zxingVersion` / `springdocVersion` / `awsSdkVersion` /
  `webauthn4jVersion` / `s3mockVersion`（仅测试）— 不在其他 BOM 里单独约束的第三方依赖

约束声明在 `ext-bom/build.gradle`，与 ext 模块版本一起发布给消费方。

### 代码风格

- 使用 Java 25 特性
- 遵循 Spring Boot 编码规范
- 对象映射使用 Spring `BeanUtils`（见 `ext-common/ext-core` 的 `BeanUtil`）
- 使用 Lombok 简化代码 (通过 Spring Boot starter 引入)

## AI 使用指引

1. **修改模块代码前**: 先阅读对应模块的 AGENTS.md 了解模块职责和依赖关系
2. **添加新功能**: 根据功能类型选择合适的层级模块
3. **添加新模块**: 在对应层目录下创建，并更新 settings.gradle 的 include 列表
4. **版本更新**: 修改 gradle.properties 的 `version` 属性

## 变更记录

| 日期       | 变更内容                                                              |
|------------|-----------------------------------------------------------------------|
| 2026-01-19 | 初始化 AGENTS.md 文档 |
| 2026-10-10 | 构建方式从 Maven 切换为 Gradle (Groovy DSL)；依赖版本统一收敛到 gradle.properties，第三方依赖移入 ext-bom；AI 编程工具从 Claude Code 切换为 pi-agent，CLAUDE.md 更名为 AGENTS.md；新增 ext-jdbc、ext-job、ext-i18n、ext-oss；新增 ext-postgres、ext-lock、ext-ratelimit、ext-audit、ext-logback |
| 2026-10-11 | 解散 ext-misc 层：ext-http / ext-qrcode / ext-useragent / ext-logback 归入 ext-common，ext-i18n / ext-oss 归入 ext-infra |
| 2026-10-31 | 27 个 SPEC.md 正文并入各层 AGENTS.md 的「设计约束」节，SPEC 文档删除（AI 读 AGENTS.md 即可，不再两处维护）；`goal-and-requirements.md` 的目标 / 非目标 / 设计原则并入根 AGENTS.md，原文档删除 |
| 2026-11-01 | ext-oss 移除 SFTP 后端（presign 无法实现，属接口违约），只留 local + S3；sshd 依赖与 BOM 约束删除；ext-security 新增两步验证（TOTP）与通行密钥（WebAuthn，webauthn4j-core）；ext-bom 新增 webauthn4j 与 org.jetbrains:annotations 约束 |
| 2026-11-03 | 依赖收敛：ext-mvc 改声明 `swagger-core-jakarta`（原 `swagger-annotations:3.0.0` 与 springdoc 传递的 `-jakarta:2.2.55` 同名类冲突，且 3.x 需 Jackson 3/EE11，springdoc 3.2.0 尚未跟进）；BOM 不再约束 swagger；删除用不到的 `org.jetbrains:annotations`；webauthn4j 升 0.32.0；ext-qrcode 排除 jcommander；ext-mvc 改用 `springdoc-openapi-starter-webmvc-api`：只生成 `/v3/api-docs`，不再拖 swagger-ui webjars；`-ui` 留给消费方自加（BOM 仍有约束） |
| 2026-11-11 | 新增设计原则：需要持久化的键值数据统一走 ext-cache（`CacheStrategy`），模块不直接注入 `RedisUtil` / `PostgresUtil`，不自建 `*-storage` 开关；ext-api-crypto 的 nonce 存储随之改为 ext-cache 实现，删除三个自建后端 |
| 2026-11-12 | ext-ratelimit 改名 ext-resilience：限流改令牌桶（`CacheStrategy#tryAcquire`，多实例共用一个桶），新增熔断降级（`@Degrade` + 进程内 `CircuitBreaker`，不共享熔断态）、隔离线程池与超时（`IsolationTimeoutException`→504）、失败重试（`@Retry`，指数退避 + 抖动，强制幂等） |
| 2026-11-13 | ext-cache 覆盖穿透/击穿/雪崩：`@Cache(cacheNull, nullTimeout)` 写 `key + ":null"` 负缓存且 `DELETE` 一并清理；`CacheAspect` 对同 `cacheName:key` 单飞合并并发回源（线程重入直通）；新增 `ext.cache.expire-jitter`（默认 0.1），各策略 `put` 经 `CacheStrategy#withJitter` 只缩短不延长 TTL；ext-trace 从 ext-web 移到 ext-common（出站透传、@Scheduled 传播与 traceparent 一并补齐）；新增 ext-infra/ext-metrics（自建指标采集、即时告警、Web 大屏），并新增 `ext-core` 的 `MetricsRecorder` SPI 供各模块埋点 |
| 2026-11-14 | ext-oss 新增缩略图输出（默认 webp，长边 320，`ThumbnailEncoder` SPI 可注入自定义格式）与 `presignDownload`（带 `Content-Disposition` 文件名）；BOM 引入 `com.github.usefulness:webp-imageio`；ext-trace 从 ext-web 移入 ext-common，补齐全链路透传：`ext-tool` 新增 `TraceContext`（32 位 W3C hex traceId、出站头、`wrap`）；`HttpUtil` 与 `RestTemplate` 默认带追踪头；`@Scheduled` 任务由 `ThreadPoolTaskScheduler` 装饰器生成 traceId；WebSocket 消息处理绑定握手 traceId |
| 2026-11-15 | `ImageUtil` 从 ext-tool 移到 ext-oss（`ext.library.oss.util`），图片能力集中到它：读写/缩放/格式探测/缩略图编码器注册表；`Thumbnails` 并入 `ImageUtil`，`ext.library.oss.thumbnail` 包删除；ext-qrcode 改用 ImageIO 自带读写 |
| 2026-11-17 | 移除 ext-jdbc 模块：注解驱动 DAO 与 `QueryWrapper` 删除，ORM 选型交回业务方；分页类型仍由 ext-servlet 提供；模块总数 29 → 28 |
| 2026-11-16 | ImageIO 薄封装抽到 `ext-common/ext-tool` 的 `ImageIOUtil`（读写 + 格式探测 + 守卫），ext-qrcode 直接用；`ImageUtil` 只留缩略图/缩放/质量控制/编码器注册表，其纯转发的 12 个读写与格式探测方法删除（约 70 行）；补 webp 透明度与缩略图 alpha 测试 |
| 2026-11-18 | 已知异常不打堆栈：`Logs.error(module, throwable, ...)` 仅当整条 cause 链都是 `BizException` 才降 WARN、只记类型与消息；包着 `ConnectException`/`SQLException` 这类真实故障时仍是 ERROR + 完整堆栈（堆栈才是排查入口）。`GlobalExceptionHandler#bizException` 改为把异常交给 `Logs` 分类，原先不透传异常导致邮件/存储失败无堆栈可查；示例应用 `logback-spring.xml` 补回 `%ex`（原模式只有 `%maskedMsg`，任何堆栈都不输出）。另修示例页：`[hidden]` 被 `.row{display:flex}` 覆盖，密码登录页仍显示图形验证码栏。 |
| 2026-11-18 | 示例三处修正：① 自定义 `logback-spring.xml` 不会自动带上 Spring Boot 的 `%clr`，需显式注册 `org.springframework.boot.logging.logback.ColorConverter` 才能在控制台看到级别配色；② 登录时未设置 `SecurityLoginParams#deviceType` 导致会话恒为 `Unknown`，改为按 UA 解析出平台名；③ 示例缺 `SecurityAuthority` 实现，默认实现返回空角色，`@RequiresRoles("ADMIN")` 一律拒绝，补 `AppSecurityAuthority` 从用户表取角色；`/admin` 模板用 `#temporals` 格式化 `java.sql.Timestamp` 会 500，改在 SQL 里 `to_char`。 |
| 2026-11-18 | 删除 ext-logback（日志脱敏转换器 `%maskedMsg`）：脱敏只覆盖消息正文，堆栈/MDC 本就不脱敏，排查价值有限；IP 规则还会把 UA 版本号 `Chrome/141.0.0.0` 打成 `Chrome/141.*.*.*`，制造假象。示例日志恢复 `%msg`；`DesensitizeStrategy` 仍在 ext-json，接口返回脱敏不受影响。 |
| 2026-11-18 | 启动日志收敛：删掉 21 处只有模块名的「载入模块：xxx」回执（线程池、JSON、链路追踪、请求解密/响应加密/接口签名、验证码、降级、重试、邮件、运行时指标、Redis 两项、访问日志、OpenAPI、调用时间统计、CORS、校验扩展、PostgreSQL 三项），只保留带运行时事实的行——后端选型、连接路径、定时任务、队列恢复、表结构、审计处理器数；示例关掉 xbatis ASCII banner，压掉 Spring Data / Tomcat / Web 上下文 / DispatcherServlet / Hikari / SpringDoc 的框架自述，启动输出 57 行降到 23 行。另删掉失去被测方法的 `AccessLogFilterTest`（`maskQuery` 已从 `AccessLogFilter` 移除）。 |
| 2026-11-18 | 示例加一行 banner（`example/src/main/resources/banner.txt`）：版本／Spring Boot／JDK／OS 压成一行；`Version` 由 `run { systemProperty 'APP_VERSION', project.version }` 传入，模板里写 `${app.version}`（`application.yml` 的 `app.version: ${APP_VERSION:dev}` 兜底）。xbatis 的 ASCII banner 仍关闭。 |
