[根目录](../AGENTS.md) > **ext-infra**

# ext-infra - 基础设施层

> 提供 Redis、PostgreSQL、缓存、邮件等基础设施集成

## 层级职责

ext-infra 负责与外部基础设施的集成，为上层业务模块提供数据缓存、消息通知等能力。

## 子模块列表

| 模块 | 描述 | 主要依赖 |
|------|------|----------|
| ext-redis | Redis 操作封装，提供统一的 Redis 访问接口 | ext-core, ext-json, spring-data-redis |
| ext-lock | 分布式锁，注解式加锁（Redis / PostgreSQL 咨询锁） | ext-core；可选 ext-redis、ext-postgres |
| ext-postgres | PostgreSQL 功能模块（缓存、队列、发布订阅、限流） | ext-core, ext-json, postgresql |
| ext-cache | 多级缓存抽象 (Caffeine + Redis/PostgreSQL) | ext-redis(可选), ext-postgres(可选), caffeine |
| ext-mail | 邮件发送服务封装 | spring-boot-starter-mail |
| ext-jdbc | 基于 JdbcClient 的通用 DAO 与链式条件构造 | ext-core, spring-boot-starter-jdbc |
| ext-job | 基于 PostgreSQL 队列的声明式任务投递与执行 | ext-postgres |
| ext-i18n | 国际化，资源文件与数据库双源翻译 | ext-core, ext-json, spring-boot-starter-jdbc(可选) |
| ext-oss | 对象存储，支持本地文件系统、S3 协议与 SSH/SFTP | ext-core；可选 AWS SDK v2、sshd-sftp |

## 模块详情

### ext-redis

Redis 操作模块，提供：

- RedisTemplate 增强配置
- JSON 序列化支持
- 连接池管理 (commons-pool2)

**包结构**: `ext.library.redis`

**配置前缀**: `spring.data.redis`

### ext-postgres

PostgreSQL 功能模块，使用 PostgreSQL 原生特性实现：

- **缓存**: UNLOGGED 表实现高性能缓存（无 WAL，性能接近内存）
- **队列**: `FOR UPDATE SKIP LOCKED` 实现无锁任务队列
- **发布订阅**: `LISTEN/NOTIFY` 原生消息机制
- **限流**: 计数表 + 固定窗口算法
- **表结构初始化**: 启动时自动建表（`ext.postgres.auto-init-schema`）

**包结构**: `ext.library.postgres`

**配置前缀**: `ext.postgres`

**核心类**:
- `PostgresCacheManager` - 缓存管理器
- `PostgresQueue` - 任务队列
- `PostgresPubSub` - 发布订阅管理器
- `PostgresRateLimiter` - 限流器
- `PostgresSchemaInitializer` - 表结构自动初始化

**表结构**:
- `pg_cache` - 缓存表（UNLOGGED）
- `pg_jobs` - 任务队列表
- `pg_rate_limits` - 限流记录表

### ext-lock

分布式锁模块，在锁存储 SPI 之上包一层声明式入口：

- `@RedisLock` - 锁注解（`key` 支持 SpEL、`timeout` 持有时间、`waitTime` 等待时间）；注解名沿用旧契约，内部不绑定后端
- `RedisLockAspect` - 锁切面，正常返回与抛异常都自动释放
- `LockKeyGenerator` - 锁键生成器（SpEL），可替换为自定义实现
- `LockStore` - 锁存储 SPI；实现 `RedisLockStore`（ext-redis 的 `DistributedLock`，自带 TTL）与 `PostgresLockStore`（`pg_try_advisory_lock` 会话级锁）
- `LockException` - 未抢到锁时抛出，携带锁 key

**包结构**: `ext.library.lock`

后端由 `ext.lock.store-type` 选择（`AUTO` / `REDIS` / `POSTGRES`），两个后端模块均为 `compileOnly`，一个都没引时装配阶段直接报错。

### ext-cache

多级缓存抽象模块，支持：

- **本地缓存**: Caffeine 高性能本地缓存
- **分布式缓存**: Redis 或 PostgreSQL（可配置）
- **二级缓存**: Caffeine + 分布式缓存（后端可切换）
- **缓存策略**: `CaffeineStrategy`, `RedisStrategy`, `PostgresStrategy`, `L2Strategy`
- **注解支持**: `@Cache` 缓存注解

**包结构**: `ext.library.cache`

**配置前缀**: `ext.cache`

**核心类**:
- `CacheAutoConfig` - 自动配置类
- `CacheProperties` - 缓存配置属性
- `Cache` - 缓存注解
- `CacheStorage` - 缓存存储方式枚举
- `L2Backend` - 二级缓存后端枚举

**配置示例**:
```yaml
ext:
  cache:
    cache-storage: L2        # REDIS/POSTGRES/CAFFEINE/L2
    l2-backend: POSTGRES     # L2 模式下的分布式后端：REDIS/POSTGRES
```

### ext-mail

邮件发送模块，提供：

- **邮件发送**: `MailSender`, `MailSenderImpl`
- **邮件模型**: `MailDetails`
- **事件支持**: `MailSendEvent` 邮件发送事件

**包结构**: `ext.library.mail`

**配置前缀**: `spring.mail`

### ext-jdbc

JdbcClient 增强模块，在 Spring `JdbcClient` 之上补一层注解驱动的轻量 DAO，不引入 ORM：

- **注解**: `@Table`（表名）、`@Id`（主键字段）
- **DAO 工厂**: `JdbcDaoFactory#of(Class)` 按实体类型产出 `JdbcDao<T>`
- **元数据**: `EntityMetaData` 在首次使用时反射解析表名、主键与列名
- **行映射**: `EntityRowMapper` 按列名（驼峰↔下划线）映射实体字段
- **链式条件**: `QueryWrapper<T>` 提供 eq/ne/lt/le/gt/ge/in/notIn/between/isNull/like/groupBy/having/orderBy/limit/last
- **分页**: `PageQuery`（页码/页大小）与 `PageResult<T>`（记录、总条数、总页数）
- **防护**: 无条件 `update`/`delete` 直接拒绝执行，必须带 `QueryWrapper` 或主键

**包结构**: `ext.library.jdbc`

所有值均以 `?` 占位符绑定，不会拼接进 SQL 文本。

**核心类**:
- `JdbcDao` - 通用 DAO（`selectById`/`selectList`/`selectCount`/`selectPage`/`insert`/`update`/`deleteById`/`delete`）
- `JdbcDaoFactory` - DAO 工厂
- `QueryWrapper` - 链式条件构造
- `EntityMetaData` / `EntityRowMapper` - 元数据解析与行映射
- `PageQuery` / `PageResult` - 分页入参与结果
- `JdbcAutoConfiguration` - 自动装配 `JdbcDaoFactory`

**使用示例**:
```java
@Table("sys_user")
public class User {
    @Id
    private Long id;
    private String name;
}

JdbcDao<User> userDao = jdbcDaoFactory.of(User.class);
Optional<User> user = userDao.selectById(1L);

PageQuery page = new PageQuery();
page.setPage(1);
page.setSize(20);
PageResult<User> result = userDao.selectPage(page,
        QueryWrapper.<User>of().like("name", "张").orderByAsc("id"));

// 防护：无条件全表操作会被拒绝
userDao.delete(QueryWrapper.of());   // 抛 ExtException
```

### ext-job

任务调度模块，直接复用 ext-postgres 的 `PostgresQueue`，不引入独立调度框架：

- **处理器**: `JobHandler<T>` 声明负载类型、队列名与处理逻辑，`handle` 抛异常即视为本次尝试失败
- **投递器**: `JobDispatcher` 按队列投递任务，支持延迟投递、重试次数上限与队列统计
- **执行器**: `JobExecutor` 每轮按队列出队处理，由 `@Scheduled` 轮询触发
- **运维接口**: `countPending`/`stats`/`cleanup`/`resetStuck`/`queues`，用于积压观测与卡死任务回收

**包结构**: `ext.library.job`

**配置前缀**: `ext.job`

**核心类**:
- `JobHandler` - 处理器接口（含 `of(Class, Consumer)` 便捷工厂）
- `JobDispatcher` - 任务投递器
- `JobExecutor` - 轮询执行器
- `JobProperties` - 任务执行配置
- `JobAutoConfiguration` - 自动配置类

**使用示例**:
```java
@Component
public class OrderJobHandler implements JobHandler<Order> {
    @Override
    public Class<Order> payloadType() { return Order.class; }

    @Override
    public void handle(Order order) { orderService.process(order); }
}

// 投递
long jobId = jobDispatcher.dispatch("default", new Order(1L));
```

**配置示例**:
```yaml
ext:
  job:
    enabled: true                 # 是否启用任务轮询
    batch-size: 10                # 每轮每队列最大出队条数
    poll-interval-millis: 1000    # 轮询间隔
    stuck-timeout: 10m            # 卡死任务回收阈值
    completed-retention: 1d       # 已完成任务保留时长
    recover-on-startup: true      # 启动时回收卡死任务
    cleanup-on-startup: true      # 启动时清理过期完成任务
```

> 未注册任何 `JobHandler` 时只装配投递器与执行器，不启动轮询。

### ext-i18n

国际化模块，把翻译文案拆成多个来源，查不到就回退到下一个：

- **管理器**: `I18nMessageManager` 按 `Locale` 顺序查询各翻译源，支持占位符参数、`find` 返回 `Optional`、`contains` 判定与 `clear` 清缓存
- **来源 SPI**: `TranslationSource` 接口（`load(Locale)` / `name()` / `order()`），`order()` 越小越先查询
- **资源文件源**: `ResourceBundleTranslationSource` 读 classpath 下的 `ResourceBundle`
- **数据库源**: `JdbcTranslationSource` 读翻译表，查询走语言降级链 `zh-CN` → `zh` → `ROOT`，按降级顺序合并
- **缓存**: 按语言缓存翻译表，`ext.i18n.cache-ttl` 到期重载，便于翻译变更后自动��效

**包结构**: `ext.library.i18n`

**配置前缀**: `ext.i18n`

**核心类**:
- `I18nMessageManager` - 翻译管理器
- `TranslationSource` - 翻译来源 SPI
- `ResourceBundleTranslationSource` / `JdbcTranslationSource` - 两个翻译源
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
ext-common (ext-core, ext-json, ext-servlet)
    |
    +---> ext-redis (独立)
    |         |
    |         +---> ext-lock
    |
    +---> ext-postgres (独立)
    |         |
    |         +---> ext-job
    |
    +---> ext-jdbc (独立)
    |
    +---> ext-cache
              |
              +---> ext-redis (可选，用于 Redis 缓存)
              +---> ext-postgres (可选，用于 PostgreSQL 缓存)
              +---> caffeine (必需，用于本地缓存)

ext-mail (独立，仅依赖 spring-boot-starter-mail)

ext-core + ext-json
    |
    +---> ext-i18n (+ spring-boot-starter-jdbc 可选)

ext-core (+ software.amazon.awssdk:s3 或 sshd-sftp，按需 compileOnly)
    |
    +---> ext-oss
```

**注意**: ext-postgres 和 ext-cache 之间没有循环依赖。ext-cache 通过可选依赖引用 ext-postgres。

## 相关文件

- `ext-redis/src/main/java/ext/library/redis/` - Redis 相关源码
- `ext-lock/src/main/java/ext/library/lock/` - 分布式锁源码
- `ext-postgres/src/main/java/ext/library/postgres/` - PostgreSQL 相关源码
- `ext-cache/src/main/java/ext/library/cache/` - 缓存相关源码
- `ext-mail/src/main/java/ext/library/mail/` - 邮件相关源码
- `ext-jdbc/src/main/java/ext/library/jdbc/` - JdbcClient DAO 相关源码
- `ext-job/src/main/java/ext/library/job/` - 任务调度源码
- `ext-i18n/src/main/java/ext/library/i18n/` - 国际化源码
- `ext-oss/src/main/java/ext/library/oss/` - 对象存储源码

## 变更记录

| 日期 | 变更内容 |
|------|----------|
| 2026-10-11 | 收编原 ext-misc 层的 ext-i18n / ext-oss，ext-misc 层解散 |
| 2026-10-10 | 新增 ext-job 模块（基于 PostgreSQL 队列的任务调度） |
| 2026-10-10 | 新增 ext-jdbc 模块（JdbcClient 增强与轻量 DAO） |
| 2026-01-26 | ext-cache 支持 PostgreSQL 缓存后端，解除循环依赖 |
| 2026-01-22 | 添加 ext-postgres 模块 |
| 2026-01-19 | 初始化 AGENTS.md 文档 |
