---
id: ext-servlet
type: module-design
title: ext-servlet — servlet 最小基座
status: draft
parent: ext-library-architecture
depends-on: [ext-tool, ext-json]
---

# ext-servlet — servlet 最小基座

**定位**：Web 层之下、任何层都可能需要的那部分"请求 / 响应"能力——弱模型（纯数据）、servlet 工具、错误信封、请求体处理、请求标识。它**不含任何 Spring MVC 语义**：没有 `@RestControllerAdvice`、没有 `ResponseBodyAdvice`、没有 `HandlerMethod`、没有参数解析器。

## 内容

### ① 弱模型

- **统一响应体 `R<T>`**（`code` / `msg` / `data`，状态码取自 `ext-tool` 的 `ResponseCode`）——从 `ext-mvc` 抽出。
- **分页与选项模型** `PageQuery`（`page` / `size` / `total`）、`PageResult<T>`（`records` / `total` / `pages` + `empty(query)`）、`SelectData`（下拉选项）——从 `ext-mvc` 抽出。`ext-jdbc` 里那份同名副本随之删除：它的类注释已经写明"由于 infra 不能反向依赖 web，此处独立定义一份"——基座建成后这个理由不存在了，分页类型只保留一份，Web 层不再需要手工转换。

### ② servlet 工具 `ServletUtil`

- 现有：request / response / session / requestAttributes 取值；param、header、cookie 读写；`getIpAddr()`（走 `X-Forwarded-For` → `X-Real-IP` 链）；`getUA()`。
- 补齐 `getRequestOrNull()` / `getResponseOrNull()`：非 Web 线程返回 `null`，调用方不必再手写 `instanceof ServletRequestAttributes` 样板。
- 补齐 `writeJson(response, status, body)`：让 Filter、WebSocket 握手拦截器这类 servlet 级代码也能输出 `R` 结构——今天只有 MVC 的 advice 能产出统一信封。

### ③ 错误信封与异常映射

把「异常 → (`HttpStatus`, `R`)」的信封组装与统一日志口径收到这里：`ErrorEnvelope`（HTTP 状态与响应体的配对，含业务码不进 HTTP 状态的判定）与 `ErrorLog`（模块标识 + URI + 消息）。`ext-mvc` 的 `GlobalExceptionHandler`、`ext-security` 的 `SecurityExceptionHandler` 只声明「哪种异常归我管」与对外消息，不各写一遍组装。这是安全层为了返回 `R` 而反向依赖 Web 层的根因之一。

### ④ 请求体可重复读

`RepeatableRequestWrapper` + 可选 `Filter`：`ext-api-crypto` 解密要读 body、`ext-audit` 记录入参要读 body、`ext-idempotent` 算指纹要读 body——今天各读各的，一经读取流即空。

### ⑤ 请求标识

`X-Request-Id` 的透传 / 生成与对应的 MDC 字段约定（供 `ext-logback` 的日志 pattern 使用）。**新能力：字段名、是否默认开启尚未确认**，实施前需先定——**当前未实现**，模块里尚无相关代码。

## 归属判定

放进来的条件，缺一不可：

1. Web 层之下的模块确实需要它（否则留在 `ext-mvc`）；
2. 它不携带 MVC 框架语义（否则无论谁需要都留在 `ext-mvc`）。

满足这两条的典型形态就是"弱模型 + servlet 工具 + 信封"，新增内容先对照这两条再决定放哪。

明确**不**在这里：`@RestControllerAdvice` / `ResponseBodyAdvice` / `GlobalExceptionHandler` / `GlobalResponseHandler` / `IgnoreRestWrapper` / `HandlerMethodUtil` / `BodyParam` 与 `PageQueryArgumentResolver`（MVC 绑定机制）/ 校验注解与校验器（含 `@Xss`）/ HTML 转义工具（通用转义在 `ext-tool` 的 `StringUtil`）/ OpenAPI / `WebInvokeTimeInterceptor` / `StartupListener`（容器级，与 servlet 无关）。

## 依赖

`ext-tool` 为 `api`；`ext-json` 为 `api`（`writeJson` 的序列化口径与全局一致，同层依赖合法）；`spring-web`（`HttpStatus`、`RequestContextHolder`）为 `api`；`jakarta.servlet-api` 为 `compileOnly`，由消费方的 servlet 容器提供。

## 为什么单独成模块

安全层的拦截器与异常处理器需要 servlet 能力；若这些类型留在 `ext-mvc`（Web 层），安全层就必须反向依赖 Web 层——这是原图唯一一条越层边。把基座放在 `ext-common`，`ext-mvc` 与 `ext-security` 都向下依赖它，依赖方向恢复单向；同时不选"下沉进 `ext-core`"，因为 `ext-core` 被几乎所有模块依赖，让它带上 servlet 语义会把 web 基座变成全库默认前提。
