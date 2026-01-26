[根目录](../CLAUDE.md) > **ext-common**

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
| ext-core | 核心功能 (SpEL表达式、验证、AOP等) | ext-tool, spring-validation, mapstruct-plus |
| ext-json | JSON 处理工具 (Jackson封装、JsonPath) | ext-tool, spring-boot-starter-json, json-path |

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
- **对象映射**: MapStruct Plus 自动配置
- **AOP**: AspectJ 支持

**包结构**: `ext.library.core`

### ext-json

JSON 处理模块，提供：

- Jackson ObjectMapper 增强配置
- JsonPath 查询支持
- JSON 序列化/反序列化工具类

**包结构**: `ext.library.json`

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

## 变更记录

| 日期 | 变更内容 |
|------|----------|
| 2026-01-19 | 移除 ext-pom，BOM 模块已迁移至根目录 ext-bom |
| 2026-01-19 | 初始化 CLAUDE.md 文档 |
