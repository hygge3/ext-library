---
id: ext-mvc
type: module-design
title: ext-mvc — Spring MVC 增强与 OpenAPI
status: draft
parent: ext-library-architecture
depends-on: [ext-core, ext-json, ext-servlet]
---

# ext-mvc — Spring MVC 增强与 OpenAPI

**职责**：Web 层的公共增强，被所有 HTTP 侧模块与业务应用复用：

- **校验**：自定义约束 `@Chinese` / `@English` / `@Cellphone` / `@ZipCode` / `@Username` / `@Xss` / `@RangeIn` / `@Exclusion` / `@Mutual`，预定义分组 `ValidationGroups`，消息插值 `EmptyCurlyToDefaultMessageInterpolator`。
- **统一出参**：异常处理与 REST 响应包装，**包装范围由包名列表控制**（不要退回布尔开关）。
- **OpenAPI**：接口文档自动配置。
- **启动**：`StartupListener`。

**边界**：依赖 `ext-core`、`ext-json`、`ext-servlet`、`swagger-annotations`、`springdoc`、`starter-webmvc`；它不实现认证，认证在 [ext-security](../../ext-security/SPEC.md)。统一响应体 `R`、分页与选项模型（`PageQuery` / `PageResult` / `SelectData`）、`ServletUtil`、错误信封都属 [ext-servlet](../../ext-common/ext-servlet/SPEC.md)，本模块不重复定义，**也不要再放回同名类**。本模块只负责包装范围、异常归类、校验与文档：`GlobalExceptionHandler` 只声明「哪种异常归我管、对外说什么」，信封组装与日志口径用基座的 `ErrorEnvelope` / `ErrorLog`。

**转义归属**：不另开 HTML 模块——纯字符串转义（`escapeHtml`）用 `ext-tool` 的 `StringUtil`，`@Xss` 校验器留在本模块（它是校验语义，`ext-servlet` 明确不收校验器）。
