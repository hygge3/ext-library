---
id: ext-tool
type: module-design
title: ext-tool — 通用工具类库
status: draft
parent: ext-library-architecture
---

# ext-tool — 通用工具类库

**职责**：不含业务语义的基础工具集，供上层任意模块使用：ID 生成（`SnowflakeId`、`MongoObjectId`）、树构建 `TreeBuilder`、版本比较 `Version`、重试 `RetryUtils`（`doWithRetry`，可配最大重试次数与间隔）、支持受检异常的函数式接口（`CheckedFunction`/`CheckedConsumer`/`CheckedSupplier` 等）、延迟初始化 `Lazy`/`Once`、枚举字典 `Dict`/`DictUtil`、字符串工具 `StringUtil`（含 `underlineToCamelCase`、`escapeHtml`）。

**边界**：不依赖任何其他 ext 模块；除根构建统一施加的 `slf4j-api` 与 `spring-boot-starter` 外不引入第三方依赖——需要第三方库的工具应落到别的模块，而不是这里。

**约定**：包 `ext.library.tool`，无自动配置、无 Spring 容器依赖，可被非 Spring 代码使用。

**归属**：本模块**不是** Spring / Spring Boot 工具类的家——`SpringUtil`（静态取 Bean）、`BeanUtil`（属性拷贝）、`ExtExpressionEvaluator`（SpEL）都在 [ext-core](../ext-core/SPEC.md)，因为它们需要 Spring 容器上下文，而本模块要保持「非 Spring 代码可用」。同理，`@Xss` 这类 Web 校验器在 [ext-mvc](../../ext-web/ext-mvc/SPEC.md)，纯字符串转义（`StringUtil.escapeHtml`）留在本模块，不需要单独开一个 HTML 模块。
