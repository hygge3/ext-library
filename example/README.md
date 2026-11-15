# 轻笔记 —— ext-library 示例应用

一个能跑的最小项目：邮箱验证码登录、笔记增删改查、附件上传与缩略图、分享链接、后台看板。
27 个模块全部引入，每个都在业务代码里真的被执行到，没有「只写不跑」的演示页。

## 每个模块在哪用到

| 模块 | 用到的东西 | 在哪 |
|---|---|---|
| ext-tool | `IdUtil`、`StringUtil`、`Logs`、`BizException`、`TraceContext` | 各 service |
| ext-core | `PubSub` SPI（笔记变更跨实例广播） | `support/NoteBroadcaster.java` |
| ext-json | `JsonUtil` | `NoteBroadcaster` |
| ext-servlet | `PageQuery`、`PageResult` | `NoteService#page` |
| ext-http | `HttpUtil` | `StatsService`（带追踪头发出站请求） |
| ext-useragent | `UserAgentParser` | `AuthService` 记登录设备 |
| ext-qrcode | `QrCode` | `ShareService#qrPng` |
| ext-trace | `TraceFilter` + `TraceInterceptor` | 页面右下角显示 traceId |
| ext-redis | `RedisUtil`、`RedisPubSub`、`RedisLock` | `StatsService`、`NoteService` |
| ext-postgres | `PostgresUtil` | 阅读量计数（`StatsService`） |
| ext-cache | `CacheStrategy`、`@Cache` | 笔记列表 30s、详情 60s |
| ext-mail | `MailSender`、`MailDetails` | 验证码、分享通知 |
| ext-lock | `@RedisLock` | 并发保存同一篇笔记 |
| ext-job | `CronJob`、`JobDispatcher`、`JobHandler` | `support/JobConfig.java`：`share-cleanup` 每天 4 点清过期分享，`share-mail` 异步发分享通知（一队列一处理器） |
| ext-i18n | `I18nMessageManager` | 邮件标题按语言取值 |
| ext-oss | `OssTemplate`、`ImageUtil` | 附件上传、webp 缩略图 |
| ext-metrics | 指标大屏 + 慢 SQL / 错误率告警 | `/metrics` |
| ext-security | `SecurityService`、`SecuritySession`、`@RequiresRoles`、`@SecurityIgnore` | 登录、会话、`/admin` |
| ext-crypto | `AESUtil`、`DigestUtil`、`PasswordEncoder` | 密码、接口报文解密、token 摘要 |
| ext-accesslog | `AccessLogFilter` | 结构化访问日志（慢请求阈值 3s） |
| ext-mvc | `PageQueryArgumentResolver`、`GlobalExceptionHandler`、OpenAPI | 分页参数、错误转 JSON、`/v3/api-docs` |
| ext-realtime | `SseUtil`、`WebSocketUtil` | 顶栏实时提示、`/websocket` |
| ext-captcha | `CaptchaService` | 注册页、登录页发码前的图形验证码 |
| ext-idempotent | `@Idempotent` | 新建笔记防重复提交 |
| ext-api-crypto | `@ApiSign`、`@ResponseEncrypt` | `/api/open/share/{token}` |
| ext-resilience | `@RateLimit`、`@Degrade`、`@Retry` | 发码限流、开放接口降级重试 |
| ext-audit | `@OperationLog`、`AuditHandler` | 笔记变更落 PostgreSQL |

数据访问用 [xbatis](https://xbatis.cn/)（MyBatis 封装）：`domain/` 上标 `@Table` / `@TableId`，主键由内置 `UUID_V7` 生成器填；
查询走 `QueryChain`，分页是 `Pager` + ext-servlet 的 `PageResult`。`XbatisMappingTest` 用 H2 跑真实 SQL 验证映射与分页。

## 跑起来

需要本机 PostgreSQL 与 Redis（邮件用一个本地 SMTP 调试服务，默认 1025）：

```bash
psql -U postgres -f example/db/init.sql  # 建库 ext_example
redis-server                           # 默认 localhost:6379

cp example/.env.example example/.env   # 账号密钥，已被 git 忽略
./gradlew :example:run
```

建表与初始数据由应用启动时执行 `src/main/resources/db/schema.sql`（幂等，每次都跑），不用手动导入。
自带账号 `admin@example.com` / `admin123`（ADMIN 角色），用密码方式登录不需要收发件；其它邮箱注册时若等于 `ADMIN_EMAIL` 也就是管理员。

## 数据库

| 文件 | 做什么 |
|---|---|
| `db/init.sql` | 首次建库，只需执行一次 |
| `src/main/resources/db/schema.sql` | 建表 + 初始数据，应用每次启动都执行，全部 `IF NOT EXISTS` / `ON CONFLICT DO NOTHING` |

所有主键都是代码生成的 UUIDv7（`IdUtil.getUUIDv7()`，32 位十六进制），按时间有序，不用数据库自增；`note_share.token` 例外，它是 72 位随机串（12 字符 Base64URL），短到能塞进二维码。

## 页面

| 路径 | 说明 |
|---|---|
| `/login` | 验证码或密码登录（发邮件的「获取验证码」须先填图形验证码，一次性、点图可换） |
| `/register` | 注册：图形验证码 + 密码强度 |
| `/notes` | 笔记列表与搜索，侧栏是最近浏览 |
| `/notes/{id}` | 编辑、上传附件、生成分享链接 |
| `/profile` | 当前会话与登录历史 |
| `/admin` | 管理员看板：用户统计、审计、访问日志 |
| `/share/{token}` | 免登录访问的分享页 |
| `/metrics` | ext-metrics 指标大屏；令牌为空时**只允许本机直连**（`http://127.0.0.1:8080/metrics`），经代理或远程访问需在 `.env` 设 `METRICS_TOKEN` 后带 `?token=` |

## 演示顺序

1. 注册 → 邮箱收到验证码 → 验证码登录（另开标签页，看顶栏 SSE 实时提示）；
2. 新建笔记 → 编辑 → 传图（自动生成 webp 缩略图）；
3. 生成分享链接 → 页面给出二维码 → 无痕窗口打开；
4. 点「第三方调用」，看 `@ApiSign` 签名 + `@ResponseEncrypt` 密文往返；
5. 用 `admin@example.com` 进 `/admin`，看审计与访问日志。

配置键见 `src/main/resources/application.yml`，可由 `.env` 覆盖（properties 语法）。
`ext.oss.type` 改成 `S3` 并填 `OSS_ENDPOINT` / `OSS_ACCESS_KEY` / `OSS_SECRET_KEY` 即可换到对象存储，代码不用动。
