[根目录](../AGENTS.md) > **ext-web**

# ext-web - Web 层

> 提供 Web 开发相关功能，包括 MVC 增强、访问日志、链路追踪与实时通信

## 层级职责

ext-web 负责 Web 层的功能增强，提供统一的请求处理、API 文档、访问观测、实时通信等能力。

## 子模块列表

| 模块 | 描述 | 主要依赖 |
|------|------|----------|
| ext-accesslog | 结构化 HTTP 访问日志（耗时、状态码、IP、UA、traceId、慢请求） | ext-core, ext-json, ext-servlet, spring-web, servlet-api |
| ext-mvc | Spring MVC 增强与 OpenAPI 文档，自定义验证器、异常处理 | ext-core, ext-json, ext-servlet, springdoc-openapi, spring-webmvc |
| ext-realtime | 实时通信，SSE 服务端推送与 WebSocket 双向通信 | ext-security, spring-webmvc, spring-websocket, ext-redis |
| ext-trace | 链路追踪（traceId/requestId 透传、MDC、响应头）与异步上下文传播 | ext-core, ext-servlet, spring-web, servlet-api |

## 模块详情

### ext-accesslog

结构化访问日志模块，提供：

- `AccessLogFilter` - `OncePerRequestFilter`，记录方法、URI、查询串、状态码、耗时、traceId（MDC）、客户端 IP（`ServletUtil.getIpAddr`）、UA、可选请求头（敏感头自动掩码）
- `AccessLogProperties` - 配置属性（前缀 `ext.accesslog`）
- 慢请求阈值（默认 `3000ms`）以 `WARN` 输出；`4xx`/`5xx` 分级日志

**包结构**: `ext.library.accesslog`

#### 设计约束

**职责**：统一 HTTP 访问观测，不替代业务审计（`ext-audit`）。默认只记元信息，不读请求/响应 body；排除路径（actuator/swagger 等）可配。

**边界**：依赖 `ext-servlet` 的 `ServletUtil` 获取 IP；traceId 从 MDC 读取（与 `ext-trace` 配合，trace 过滤器 order 更靠前）。`jakarta.servlet-api` 为 `compileOnly`。

### ext-mvc

Spring MVC 增强模块，提供：

**自定义验证约束**:
- `@Chinese` - 中文字符校验
- `@English` - 英文字符校验
- `@Cellphone` - 手机号校验
- `@ZipCode` - 邮编校验
- `@Username` - 用户名校验
- `@Xss` - XSS 攻击校验
- `@RangeIn` - 范围校验
- `@Exclusion` - 排除值校验
- `@Mutual` - 互斥字段校验

**验证分组**:
- `ValidationGroups` - 预定义验证分组

**消息插值**:
- `EmptyCurlyToDefaultMessageInterpolator` - 空大括号转默认消息

**包结构**: `ext.library.web`

#### 设计约束

**职责**：Web 层的公共增强，被所有 HTTP 侧模块与业务应用复用：

- **校验**：自定义约束 `@Chinese` / `@English` / `@Cellphone` / `@ZipCode` / `@Username` / `@Xss` / `@RangeIn` / `@Exclusion` / `@Mutual`，预定义分组 `ValidationGroups`，消息插值 `EmptyCurlyToDefaultMessageInterpolator`。
- **统一出参**：异常处理与 REST 响应包装，**包装范围由包名列表控制**（不要退回布尔开关）。
- **OpenAPI**：接口文档自动配置，只生成 `/v3/api-docs`，不带 swagger-ui；需要页面时消费方自行加 `springdoc-openapi-starter-webmvc-ui`（版本不写）。**Tag 命名只在 `ExtOpenAPIService` 一处决定**（JavaDoc 第一行作名、整段作描述，无 JavaDoc 退化为 `splitCamelCase`），`OpenApiCustomizer` 只负责补 contextPath，不要再在 customizer 里改名——两套改名逻辑口径不一致，且 `Tag.equals` 连 description 一起比，同名会生成两条。
- **启动**：`StartupListener`。

**边界**：依赖 `ext-core`、`ext-json`、`ext-servlet`、`swagger-core-jakarta`、`springdoc-openapi-starter-webmvc-api`、`starter-webmvc`；它不实现认证，认证在 [ext-security](../ext-security/AGENTS.md)。统一响应体 `R`、分页与选项模型（`PageQuery` / `PageResult` / `SelectData`）、`ServletUtil`、错误信封都属 [ext-servlet](../ext-common/AGENTS.md#ext-servlet)，本模块不重复定义，**也不要再放回同名类**。本模块只负责包装范围、异常归类、校验与文档：`GlobalExceptionHandler` 只声明「哪种异常归我管、对外说什么」，信封组装与日志口径用基座的 `ErrorEnvelope` / `ErrorLog`。

**转义归属**：不另开 HTML 模块——纯字符串转义（`escapeHtml`）用 `ext-tool` 的 `StringUtil`，`@Xss` 校验器留在本模块（它是校验语义，`ext-servlet` 明确不收校验器）。

### ext-realtime

实时通信模块，包含两套实时通道实现，均支持分布式环境——跨实例广播注入 ext-core 的 `PubSub`（ext-redis 的 `RedisPubSub` 或 ext-postgres 的 `PostgresPubSub`），本模块不再自带 pub/sub 实现，没有该 Bean 时退化为单实例模式：

**SSE** (`ext.library.sse`)：
- `SseController` - SSE 控制器
- `SseEmitterManager` / `SseConnectionManager` - 连接管理
- `SseTopicListener` - 主题监听器
- `SseProperties` - 配置属性 (`ext.sse.*`)

**WebSocket** (`ext.library.websocket`)：
- `ExtWebSocketHandler` - WebSocket 处理器
- `WebSocketConnectionManager` - 会话管理
- `WebSocketTopicListener` - 主题监听器
- `ExtWebSocketInterceptor` - 握手拦截器（认证集成）
- `WebSocketProperties` - 配置属性 (`ext.websocket.*`)

#### 设计约束

**职责**：两套实时通道，语义一致、可按需选一套或并用：

- **SSE**（`ext.library.sse`）：`SseController` 建流、`SseEmitterManager` / `SseConnectionManager` 管连接、`SseTopicListener` 订阅主题；配置前缀 `ext.sse`。
- **WebSocket**（`ext.library.websocket`）：`ExtWebSocketHandler` 收发、`WebSocketConnectionManager` 管会话、`WebSocketTopicListener` 订阅主题、`ExtWebSocketInterceptor` 握手期接入认证；配置前缀 `ext.websocket`。

**边界**：广播用的 `ext-redis` / `ext-postgres` 不在本模块声明依赖，由消费方按需引入（各自的自动配置会提供 `PubSub` Bean）——不引后端时只有**单实例**语义（同 JVM 内广播），多实例部署必须引其中之一。广播实现不自带：注入 [ext-core](../ext-common/AGENTS.md#ext-core) 的 `PubSub`，实际是 [ext-redis](../ext-infra/AGENTS.md#ext-redis) 的 `RedisPubSub` 或 [ext-postgres](../ext-infra/AGENTS.md#ext-postgres) 的 `PostgresPubSub`，两套通道共用同一份，不各自复制 pub/sub 实现。登录态与 token 校验交给 [ext-security](../ext-security/AGENTS.md)（`api` 依赖），本模块不定义会话模型。

**约束**：两套通道共用同一套连接管理/主题监听形状，新增第三方推送方式应沿用同一形状，不要在某一侧另造抽象。

## 依赖关系

```
ext-common (ext-core, ext-json, ext-servlet)
    |
    +---> ext-accesslog
    +---> ext-mvc
    +---> ext-trace
    +---> ext-security ---> ext-realtime (依赖 ext-security, spring-webmvc；广播后端 ext-redis / ext-postgres 由消费方引入)
```

### ext-trace

链路追踪与上下文透传模块，提供：

- `TraceFilter` - 从请求头（`X-Request-Id` / `X-Trace-Id` / `traceparent`）提取或生成 traceId，写入 MDC 与响应头
- `MdcTaskDecorator` - 提交异步任务时捕获/恢复 MDC，避免线程复用串线
- `TraceIdGenerator` - 追踪 ID 生成（短 hex / UUID）
- `TraceProperties` - 配置属性（前缀 `ext.trace`）
- `BeanPostProcessor` 为 `ThreadPoolTaskExecutor` 自动挂载 MDC 装饰器（可关 `ext.trace.propagate-mdc`）

**包结构**: `ext.library.trace`

#### 设计约束

**职责**：补齐跨请求/跨线程的追踪上下文，供日志（`ext-logback`/访问日志）关联；不自造分布式 tracer，对接 Micrometer Tracing 时由消费方引入桥接（本模块 `compileOnly` 声明 `micrometer-tracing`/`context-propagation` 以便后续扩展）。

**边界**：Web 过滤器依赖 `ext-servlet`；MDC 传播是通用能力。与 `ext-accesslog` 配合时 trace 过滤器 order（`+5`）早于 accesslog（`+20`）。

**注意**：`ThreadPoolTaskExecutor` 装饰器通过 `BeanPostProcessor` 在初始化前设置；若用户需自定义 `TaskDecorator`，应关闭 `ext.trace.propagate-mdc` 并自行组合装饰器。

## 相关文件

- `ext-accesslog/src/main/java/ext/library/accesslog/` - 访问日志源码
- `ext-mvc/src/main/java/ext/library/web/` - MVC 增强源码
- `ext-mvc/src/main/java/ext/library/openapi/` - OpenAPI 源码
- `ext-realtime/src/main/java/ext/library/sse/` - SSE 源码
- `ext-realtime/src/main/java/ext/library/websocket/` - WebSocket 源码
- `ext-trace/src/main/java/ext/library/trace/` - 链路追踪源码

## 变更记录

| 日期 | 变更内容 |
|------|----------|
| 2026-01-19 | 初始化 AGENTS.md 文档 |
| 2026-10-10 | ext-openapi 并入 ext-mvc，ext-sse 与 ext-websocket 合并为 ext-realtime |
| 2026-11-04 | 新增 ext-accesslog（结构化访问日志）与 ext-trace（链路追踪/MDC 透传） |
