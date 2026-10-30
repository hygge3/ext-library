[根目录](../AGENTS.md) > **ext-common**

# ext-common - 通用基础层

> 提供项目基础设施，包括工具类、核心功能和 JSON 处理

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

## 依赖关系

```
ext-tool (基础工具)
    |
    +---> ext-core (核心功能)
    |
    +---> ext-json (JSON处理)
```

## 相关文件

- `ext-tool/src/main/java/ext/library/tool/` - 工具类源码
- `ext-core/src/main/java/ext/library/core/` - 核心功能源码
- `ext-json/src/main/java/ext/library/json/` - JSON 处理源码
- `ext-json/src/main/java/ext/library/trans/` - 字段翻译源码
- `ext-json/src/main/java/ext/library/desensitize/` - 数据脱敏源码

## 变更记录

| 日期 | 变更内容 |
|------|----------|
| 2026-01-19 | 移除 ext-pom，BOM 模块已迁移至根目录 ext-bom |
| 2026-01-19 | 初始化 AGENTS.md 文档 |
