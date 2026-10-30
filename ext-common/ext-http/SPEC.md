---
id: ext-http
type: module-design
title: ext-http — HTTP 客户端封装
status: draft
parent: ext-library-architecture
depends-on: [ext-tool]
---

# ext-http — HTTP 客户端封装

**职责**：一个 `HttpUtil` 门面收敛出站 HTTP 调用：常用方法（GET / POST / PUT / DELETE 等）、请求与响应处理、同步与异步两种调用方式。`HttpProperties` 承载 `ext.http.*`：`timeout`（连接/读取超时，默认 `30s`）、`retry-count`（失败重试次数，默认 `3`，即首次之外最多再试 3 次），两者都可按单次调用覆盖。

**边界**：只依赖 `ext-tool`，不引入额外 HTTP 客户端库也不做服务发现/负载均衡；重试用 `ext-tool` 的 `RetryUtils.doWithRetry`（重试次数与间隔取自 `HttpProperties`），不在这里长出一套重试框架。

**约定**：重试只针对可重试的失败（连接超时 / `5xx` / `IOException`），`4xx` 与非幂等方法（非 GET/HEAD 的默认策略）不重试；超时全局默认 `30s`，业务侧按调用传入 `Duration` 覆盖，不要在每个调用点写魔法值。
