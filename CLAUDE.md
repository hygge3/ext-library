# CLAUDE.md

本文件为 Claude Code (claude.ai/code) 在处理此代码库时提供指导。

## 概述

**ext-library** 是一个全面的 Spring Boot 扩展库（当前版本 4.0.0），基于 **Spring Boot 4.0.0** 和 **JDK 25** 构建。它提供模块化组件来解决常见的开发挑战，遵循自动配置和注解驱动的设计模式。

### 技术栈
- **Java**: 25
- **Spring Boot**: 4.0.0
- **Spring AI**: 1.1.0
- **构建工具**: Maven（多模块）
- **主要依赖**: Google Guava、MapStruct Plus、MyBatis-Flex、SpringDoc 3.0.0

## 架构与模块结构

代码库采用 **Maven 多模块架构**，每个 `ext-*` 模块都可以独立部署，专注于特定领域：

### 核心模块（基础）

#### `ext-tool` - 基础工具类
- **包路径**: `ext.library.tool.*`
- **核心组件**:
  - `core/`: `Threads`, `Systems`, `Runtimes`, `Exceptions`, `VirtualThreadPools`
  - `util/`: `DateUtil`, `CalcUtil`, `String/Collection/Map 工具类`, `IDUtil` (雪花算法ID)
  - `holder/`: `Lazy`（延迟加载）, `Once`, `Unchecked`（异常包装）
  - `domain/`: `SnowflakeId`, `Version`, `MongoObjectId`
  - **依赖**: Google Guava（主要）
- **使用场景**: 作为基础工具始终包含

#### `ext-core` - Spring 基础
- **包路径**: `ext.library.core.*`
- **核心组件**:
  - `config/`: `ThreadPoolConfig` - 自动配置的线程池（异步 & 定时任务）
  - `util/`: `BeanUtil`, `SpelUtil`, `AspectUtil`, `SpringUtil`, `ServletUtil`, `ValidatorUtil`
  - `constant/`: `PatternPool` - 常用正则表达式
- **架构模式**:
  - 使用 `@AutoConfiguration` 配合 `@EnableConfigurationProperties`
  - 基于 Spring 的 `ThreadPoolTaskExecutor` 和 `ScheduledExecutorService`
  - 集成 `MapStruct`（通过 `BeanUtil`）和 CGLIB `BeanCopier` 优化 bean 操作

### 功能模块（特性）

#### `ext-crypto` - 加密操作
- **算法支持**: AES、RSA、SM2、SM4、DES、Digest
- **工具类**: `PasswordEncoder`, `DigestUtil`
- **结构**: 纯工具类，无自动配置
- **测试覆盖**: 每种算法都有 `*-UtilTest.java` 测试类

#### `ext-cache` - 缓存解决方案
- **架构**: 注解驱动的 AOP 缓存（`@Around` 通知）
- **核心类**:
  - `@Cache` 注解: `cacheName`, `key`（SpEL）, `timeout`, `timeUnit`, `type`（FULL/PUT/DELETE）
  - `CacheAspect`: 处理缓存操作的中心切面
  - **策略模式**: `CacheStrategy` 接口及实现：
    - `CaffeineStrategy`: 本地缓存
    - `RedisStrategy`: 分布式缓存（依赖 `ext-redis`）
    - `L2Strategy`: 混合缓存
- **集成**: 支持 Spring Boot 自动配置，配合 `CacheProperties`

#### `ext-captcha` - 验证码生成
- **组件**:
  - `CaptchaService`: 主服务接口
  - `CaptchaDraw`: 不同验证码类型的基类
  - `MathCaptchaDraw`, `RandomCaptchaDraw`: 具体实现
  - `BackgroundDraw`, `InterferenceDraw`: 视觉增强
- **特性**: 数学表达式、随机字符、可自定义字体/背景
- **缓存集成**: 需要 `ICaptchaCache` bean（支持内存/Redis）

#### `ext-http` - HTTP 客户端增强
- **核心**: `HttpUtil` - JDK 11+ `HttpClient` 包装器
- **子包**: `useragent/` - 浏览器/OS 检测解析器
  - `UserAgentUtil`, `UserAgentParser`
  - 模型: `Browser`, `OS`, `Engine`, `Platform`

#### `ext-interface-crypto` - 请求/响应加密
- **基于 AOP**: 面向切面的加密/解密
- **处理器**: `RequestDecryptHandler`, `ResponseEncryptHandler`
- **策略**: `Base64Strategy`, `RSAStrategy`
- **注解**: `@RequestDecrypt`, `@ResponseEncrypt`
- **配置**: 需要在配置中提供 RSA 公钥/私钥

#### `ext-idempotent` - 幂等性支持
- **目的**: 防止重复请求
- **组件**:
  - `@Idempotent` 注解
  - `IdempotentAspect`: AOP 强制执行
  - `KeyGenerator`: `DefaultIdempotentKeyGenerator`（基于 SpEL）
  - `KeyStore`: `RedisIdempotentKeyStore`, `InMemoryIdempotentKeyStore`

#### `ext-monitor` - 系统监控
- **依赖**: OSHI（操作系统和硬件信息）
- **使用场景**: 运行时指标、系统资源监控

#### `ext-mybatis` - MyBatis 集成
- **依赖**: MyBatis-Flex
- **配置**: 基于 YAML（参见 `application-ext-mybatis.yml`）

#### `ext-json` - JSON 处理
- **工具类**: `JsonUtils`, `JsonPathUtils`

#### `ext-web` - Web 层工具
- **配置**: `application-ext-web.yml`
- 包含 Web 特定的自动配置

#### `ext-websocket` - WebSocket 支持
- 实时双向通信

#### `ext-sse` - Server-Sent Events
- 流式响应支持

#### `ext-security` - 安全组件
- 认证/授权工具

#### `ext-ratelimiter` - 限流
- API 限流实现

#### `ext-qrcode` - 二维码生成
- 使用 ZXing 库

#### `ext-trans` - 分布式事务
- 事务管理工具

#### `ext-openapi` - API 文档
- SpringDoc 集成

#### `ext-mail` - 邮件发送
- 邮件工具服务

#### `ext-desensitize` - 数据脱敏
- `@Sensitive` 注解用于数据脱敏
- 不同脱敏规则的策略模式

#### `ext-ai` - AI 集成（开发中）
- **依赖**: Spring AI 1.1.0
- **目标**: AI 模型集成、提示工程、向量搜索等
- **状态**: 适配 Spring Boot 4.0.0 中

## 核心设计模式与原则

### 1. **自动配置模式**
每个 Spring 集成模块都遵循：
```java
@AutoConfiguration
@EnableConfigurationProperties(Properties.class)
@ConditionalOnProperty(prefix = "module", name = "enabled", havingValue = "true")
public class ModuleConfig {
    @Bean
    public SomeComponent someComponent(Properties props) { ... }
}
```

### 2. **AOP 驱动的注解**
- `@Cache` - 方法级缓存
- `@Idempotent` - 幂等性强制
- `@RequestDecrypt/@ResponseEncrypt` - 加密处理
- 切面处理所有横切关注点

### 3. **策略模式**
广泛用于扩展性：
- 缓存策略
- 加密策略
- 验证码绘制策略

### 4. **SpEL（Spring 表达式语言）集成**
- 动态键生成: `SpelUtil.parseValueToString()`
- 支持方法参数、返回值、bean 属性

### 5. **函数式编程**
- `ext-tool.holder.function`: 函数式接口的异常包装
- `Lazy`, `Once` 用于延迟/一次性初始化

## 构建与开发命令

```bash
# 安装依赖并构建所有模块
mvn clean install

# 构建特定模块
mvn clean install -pl ext-core -am

# 运行测试（所有模块）
mvn test

# 运行特定模块的测试
mvn test -pl ext-crypto

# 生成源码 JAR
mvn clean package source:jar

# 查看依赖树
mvn dependency:tree
```

### 属性配置
模块支持 YAML/Properties 配置：
```yaml
# 线程池
thread-pool:
  enabled: true
  core-pool-size: 8
  max-pool-size: 20
  queue-capacity: 1000

# 缓存
ext:
  cache:
    type: L2  # FULL, PARTIAL, PUT, DELETE

# 验证码
ext:
  captcha:
    captcha-type: RANDOM  # 或 MATH
    cache-name: captcha:cache#5m

# 接口加密
ext:
  crypto:
    public-key: <RSA公钥>
    secret-key: <RSA私钥>
```

## 测试方法
- **单元测试**: 使用 JUnit（位于 `src/test/java`）
- **控制台输出**: 许多测试使用 `System.out.println` 进行验证
- **测试分组**: 加密模块有完整的算法测试

## 代码风格与约定

### 包结构
```
ext.<模块名>
  ├── config           // 自动配置类
  ├── config/properties // 配置属性类
  ├── annotation       // 自定义注解
  ├── aspect           // AOP 切面
  ├── util             // 静态工具类
  ├── core             // 核心业务逻辑
  ├── strategy         // 策略实现
  ├── enums            // 枚举
  └── vo/dto           // 视图对象
```

### 常见实践
1. **使用 Spring 工具类**: `BeanUtils`, `ObjectUtils`, `StringUtils`
2. **空安全**: `@Nullable`, `@Nonnull`（来自 JSpecify）
3. **异常处理**: `ext-tool.exception` 中的自定义异常
   - `ExtException`: 通用运行时异常
   - `BizException`: 带业务码的异常
4. **装饰器模式**: 日志使用 `[🌊]` emoji 前缀
5. **延迟初始化**: `Lazy.of()` 模式

### 模块依赖关系（关键）
```
ext-parent (BOM)
├── ext-tool (基础)
├── ext-core (依赖 tool)
├── ext-redis (依赖 core + json)
├── ext-cache (依赖 core，可选 redis)
├── ext-captcha (可选 redis 用于缓存)
└── 其他模块根据需要依赖 core/tool
```

## 常见开发任务

### 添加新模块
1. 创建 `ext-newmodule/pom.xml` 继承父 POM
2. 在父 `pom.xml` 的 `<modules>` 中添加模块
3. 遵循包命名: `ext.library.newmodule.*`
4. 如需 Spring 集成，提供自动配置
5. 添加 `README.md` 说明依赖配置

### 实现缓存策略
1. 实现 `CacheStrategy` 接口
2. 如需添加策略枚举
3. 在 `CacheConfig` 中用 `@ConditionalOnProperty` 配置
4. 使用 `@Cache` 注解测试

### 使用 BeanUtil 转换
```java
// Map Struct + Cglib 回退
Target target = BeanUtil.convert(source, Target.class);

// 列表转换
List<Target> targets = BeanUtil.convert(sourceList, Target.class);

// 配合 MapStruct Plus 的方法引用映射
@Mapper
public interface CustomMapper {
    Target toTarget(Source source);
}
```

## 重要注意事项

### JDK 25 兼容性
- 使用最新的 Java 特性
- 定期检查已弃用的 API
- OSHI 依赖需要 `oshi-core-java25`

### Spring Boot 3.x/4.x 迁移
- 使用 Jakarta EE 命名空间（`jakarta.*`）
- 使用 `@AutoConfiguration` 替代旧的 `@Configuration`
- Spring Boot 4.0.0 适配中，部分 API 可能发生变化

### Maven 构建警告说明
构建时可能出现以下警告：
```
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::staticFieldBase has been called by ...
```

**原因**: Guice (Google Inject) 库内部使用了已弃用的 `sun.misc.Unsafe` API。这是一个第三方依赖问题，不影响项目功能。

**解决方案**:
- 忽略此警告（当前阶段可接受）
- 等待 Guice 发布兼容 JDK 25+ 的版本
- 或使用 `--add-opens` JVM 参数（不推荐）

### Spring AI 适配状态
- **依赖**: Spring AI 1.1.0
- **目标**: 集成 AI 模型、提示工程、向量搜索
- **状态**: 适配 Spring Boot 4.0.0 进行中

### 模块独立性
每个模块都可以独立使用 - 无需包含所有模块。根据需求选择。

## 资源
- **文档**: 每个模块都有 `README.md` 说明具体用法
- **额外文档**: 查看 `docs/` 目录获取图片/参考
- **父 POM**: 版本和依赖的唯一事实来源
