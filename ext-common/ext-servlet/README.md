# ext-servlet

> servlet 基座 - 统一响应体、分页模型、servlet 工具、错误信封与请求体重读

## 简介

`ext-servlet` 是 ext-library 的 servlet 基座模块，基于 `ext-tool` 与 `ext-json` 构建，
只依赖 `spring-web` 与编译期可见的 `jakarta.servlet-api`，不依赖 Spring Boot、MVC 或任意模板引擎。

把「接口返回什么形状」「分页参数怎么传」「多个组件都要读 body 怎么办」这类与框架版本无关的契约集中在一处，
使 Servlet 栈、WebFlux 栈（SSE）以及最底层的 Filter 复用同一套信封与工具，而不是各自持有副本。

## 快速开始

### Maven

```xml

<dependency>
    <groupId>ext.library</groupId>
    <artifactId>ext-servlet</artifactId>
</dependency>
```

### Gradle

```groovy
implementation("ext.library:ext-servlet")
```

## 依赖说明

| 依赖                          | 说明                    |
|-----------------------------|-----------------------|
| ext-tool                    | 基础工具类                 |
| ext-json                    | Jackson 序列化口径（`writeJson`） |
| spring-web                  | `RequestContextHolder` 等 |
| jakarta.servlet-api         | 编译期可见，由运行容器提供         |

## 包结构

```
ext.library.servlet
├── config/       # 自动配置（请求体缓存过滤器）
├── exception/    # 错误信封 ErrorEnvelope、错误日志 ErrorLog
├── filter/       # RepeatableRequestFilter
├── properties/   # ServletProperties
├── response/     # R、PageQuery、PageResult、SelectData
└── util/         # ServletUtil、RepeatableRequestWrapper
```

## 功能模块

### 统一响应体 (response)

| 类名            | 说明                                    |
|---------------|---------------------------------------|
| `R`           | 统一响应体（`code`/`msg`/`data`），HTTP 语义与业务码共用 `code` |
| `PageQuery`   | 分页入参（`page`/`size`，1 基），`offset()` 直接供 SQL 使用 |
| `PageResult`  | 分页出参（`page`/`size`/`total`/`pages`/`records`） |
| `SelectData`  | 下拉选项数据（`label`/`value`/`children`）       |

### Servlet 工具 (util)

| 类名                        | 说明                                            |
|---------------------------|-----------------------------------------------|
| `ServletUtil`             | 请求/响应、参数、头、Cookie、客户端 IP、JSON 写出              |
| `RepeatableRequestWrapper` | 请求体可重复读包装                                     |

### 请求体重读

servlet 的请求体只能读一次。解密、审计记入参、幂等指纹等多个读取方共存时，
开启 `ext.servlet.repeatable-request.enabled=true` 由 `RepeatableRequestFilter` 统一缓存 body；
默认关闭——整段 body 驻留内存，只在确实有多个读取方的应用上才值得。

### 错误信封 (exception)

| 类名             | 说明                                            |
|----------------|-----------------------------------------------|
| `ErrorEnvelope`  | 异常 → (HTTP 状态, `R`) 的统一组装；业务码保持 HTTP 200     |
| `ErrorLog`       | 错误日志统一口径（模块标识 + URI + 消息）                     |

## 配置项

| 配置项                                        | 默认值     | 说明          |
|--------------------------------------------|---------|-------------|
| `ext.servlet.repeatable-request.enabled`   | `false` | 是否开启请求体缓存过滤器 |
