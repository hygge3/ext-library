# ext-trace

链路追踪：traceId 的生成、MDC 绑定与跨服务透传。不采样、不建 span 树，要完整的分布式追踪请接 Micrometer Tracing / OTel。

## 引入

```groovy
implementation 'ext.library:ext-trace'
```

自动配置随依赖生效，`ext.trace.enabled=false` 关掉入站过滤器与出站头注入（日志里的 traceId、`R.traceId` 也随之没有值）。

## 自动生效的部分

| 场景 | 实现 |
|------|------|
| HTTP 入站 | `TraceFilter` 按 `X-Request-Id` → `X-Trace-Id` → `traceparent` 取 traceId，写 MDC + 响应头 `X-Request-Id` |
| 日志 / 响应体 | MDC 键 `traceId`；`R` 的 `traceId` 字段自动取自 MDC |
| 出站 HTTP | `HttpUtil` 默认带 `X-Request-Id`（traceId 为 W3C 32 位 hex 时同时带 `traceparent`） |
| `RestTemplate` Bean | 自动追加 `TraceInterceptor` |
| `RestClient` / `@HttpExchange` | 自动注册 `RestClientCustomizer`，从注入的 `RestClient.Builder` 建出的客户端都带追踪头（需 classpath 有 `spring-boot-restclient`） |
| `@Async` / 线程池 | `ThreadPoolTaskExecutor` 挂 `ContextTaskDecorator`，MDC 与数据权限进池线程 |
| `@Scheduled` | `ThreadPoolTaskScheduler` 挂 `TraceContext::wrap`，每次执行生成独立 traceId |
| WebSocket | 握手时存 traceId 到会话属性，处理每条消息时绑定 MDC |

## 需要手工接的

消息队列、gRPC 客户端、自己 `new` 的 `RestClient`（没经过容器里的 Builder）没有通用钩子，用 `TraceContext` 自己带：

```java
// 出站：拿到要写的头
Map<String, String> headers = TraceContext.outboundHeaders();
headers.forEach(builder::header);

// 消费端 / 裸线程入口：包一条新链路，内部的日志与出站请求都会带上它
executor.submit(TraceContext.wrap(() -> handle(message)));
```

`TraceContext` 在 `ext-tool` 里（`ext.library.tool.trace.TraceContext`），没有 Servlet 依赖，非 Web 应用可以直接用。

## 配置

```yaml
ext:
  trace:
    enabled: true
    trace-id-mdc-key: traceId
    request-id-mdc-key: requestId
    request-id-headers: [X-Request-Id, X-Trace-Id, traceparent]
    response-header-enabled: true
    response-header-name: X-Request-Id
    exclude-patterns: [/actuator, /favicon.ico, /error, /swagger, /v3/api-docs, /webjars]
```

日志格式里加 `%X{traceId}` 即可关联：

```xml
<pattern>%d{HH:mm:ss.SSS} [%X{traceId}] %-5level %logger{36} - %msg%n</pattern>
```

## 边界

- 上游传的 traceId 必须匹配 `[A-Za-z0-9._-]{1,128}`，否则丢弃并重新生成——外部输入会进每条日志和响应头，不校验就能注入假日志行。
- traceId 是 32 位小写十六进制时才会写 `traceparent`；上游给的是别的形状（比如短 ID）时只透传 `X-Request-Id`，不伪造 W3C 头。
- `CacheAspect` 的单飞、线程池里的 MDC 传播都是**单实例内**语义。
