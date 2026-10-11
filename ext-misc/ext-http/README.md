# ext-http

> HTTP 客户端模块 - 基于 JDK HttpClient 的流畅 API 封装

## 简介

`ext-http` 是基于 JDK 11+ `HttpClient` 的 HTTP 客户端工具包，提供流畅的链式调用 API（Fluent API），默认使用虚拟线程执行器，提升并发性能。

## 快速开始

### Maven

```xml
<dependency>
    <groupId>ext.library</groupId>
    <artifactId>ext-http</artifactId>
</dependency>
```

### Gradle

```groovy
implementation("ext.library:ext-http")
```

## 依赖说明

| 依赖 | 说明 |
|------|------|
| ext-tool | 基础工具类 |

## 功能特性

- 链式调用：流畅的 API 设计
- 多种 HTTP 方法：GET、POST、PUT、DELETE、PATCH 等
- 同步/异步：支持同步和异步请求
- 多种数据格式：JSON、表单、文件上传下载
- 虚拟线程：默认使用虚拟线程执行器
- SSL 安全：默认启用 SSL 证书验证，可选禁用

## 核心类说明

| 类名 | 说明 |
|------|------|
| `HttpUtil` | HTTP 请求入口类 |
| `HttpUtil.Request` | 请求构建器 |
| `HttpUtil.Response` | 响应包装器 |
| `HttpUtil.HttpMethod` | HTTP 方法枚举 |

## 使用示例

### GET 请求

```java
// 简单 GET 请求
String result = HttpUtil.get("https://api.example.com/users")
    .execute()
    .asString();

// 带请求头和查询参数
String result = HttpUtil.get("https://api.example.com/users")
    .header("Authorization", "Bearer " + token)
    .query("page", "1")
    .query("size", "20")
    .acceptJson()
    .execute()
    .asString();
```

### POST 请求

```java
// 发送 JSON
String result = HttpUtil.post("https://api.example.com/users")
    .contentTypeJson()
    .body("{\"name\":\"张三\",\"email\":\"zhangsan@example.com\"}")
    .execute()
    .asString();

// 发送表单
String result = HttpUtil.post("https://api.example.com/login")
    .form("username", "admin")
    .form("password", "123456")
    .execute()
    .asString();
```

### PUT/DELETE/PATCH 请求

```java
// PUT 更新资源
HttpUtil.put("https://api.example.com/users/1")
    .contentTypeJson()
    .body("{\"name\":\"李四\"}")
    .execute();

// DELETE 删除资源
HttpUtil.delete("https://api.example.com/users/1")
    .header("Authorization", "Bearer " + token)
    .execute();

// PATCH 部分更新
HttpUtil.patch("https://api.example.com/users/1")
    .contentTypeJson()
    .body("{\"name\":\"王五\"}")
    .execute();
```

### 异步请求

```java
// 异步 GET
HttpUtil.get("https://api.example.com/data")
    .executeAsync()
    .thenAccept(response -> System.out.println(response.asString()))
    .exceptionally(ex -> {
        ex.printStackTrace();
        return null;
    });

// 并发请求
List<CompletableFuture<String>> futures = IntStream.range(0, 10)
    .mapToObj(i -> HttpUtil.get("https://api.example.com/item/" + i)
        .executeAsyncAsString())
    .toList();
```

### 文件下载

```java
// 下载文件
Path filePath = HttpUtil.get("https://example.com/file.zip")
    .timeout(300000)
    .download("/tmp/file.zip");
```

### 响应处理

```java
HttpUtil.Response response = HttpUtil.get(url).execute();

// 状态码判断
boolean success = response.isSuccess();        // 2xx
boolean redirect = response.isRedirect();      // 3xx
boolean clientError = response.isClientError(); // 4xx
boolean serverError = response.isServerError(); // 5xx

// 响应头
String contentType = response.contentType();
long contentLength = response.contentLength();

// 响应体
String body = response.asString();
byte[] bytes = response.asBytes();
InputStream stream = response.asStream();
```

### 错误处理

```java
HttpUtil.get(url)
    .onError(ex -> {
        if (ex instanceof java.net.ConnectException) {
            System.err.println("连接失败: " + ex.getMessage());
        } else if (ex instanceof java.util.concurrent.TimeoutException) {
            System.err.println("请求超时");
        }
    })
    .execute();
```

### 禁用 SSL 验证（仅限开发/测试）

```java
// 对于自签名证书的测试环境，可以禁用 SSL 验证
// ⚠️ 警告：生产环境绝对不要使用！
String result = HttpUtil.get("https://localhost:8443/api")
    .insecure()  // 禁用 SSL 验证
    .execute()
    .asString();

// 也可以直接获取不安全的 HttpClient
HttpClient insecureClient = HttpUtil.getInsecureClient();
```

## API 速查

### 入口方法

| 方法 | 描述 |
|------|------|
| `HttpUtil.get(url)` | 创建 GET 请求 |
| `HttpUtil.post(url)` | 创建 POST 请求 |
| `HttpUtil.put(url)` | 创建 PUT 请求 |
| `HttpUtil.delete(url)` | 创建 DELETE 请求 |
| `HttpUtil.patch(url)` | 创建 PATCH 请求 |
| `HttpUtil.request(url)` | 创建通用请求构建器 |

### 请求配置

| 方法 | 描述 |
|------|------|
| `header(name, value)` | 添加请求头 |
| `headers(map)` | 批量添加请求头 |
| `query(name, value)` | 添加查询参数 |
| `body(content)` | 设置请求体 |
| `form(name, value)` | 添加表单数据 |
| `timeout(ms)` | 设置超时时间 |
| `insecure()` | 禁用 SSL 验证（仅开发/测试） |
| `contentTypeJson()` | 设置 Content-Type: application/json |
| `contentTypeForm()` | 设置 Content-Type: application/x-www-form-urlencoded |
| `acceptJson()` | 设置 Accept: application/json |
| `bearer(token)` | 设置 Authorization: Bearer token |

### 执行方法

| 方法 | 描述 |
|------|------|
| `execute()` | 同步执行，返回 Response |
| `executeAsString()` | 同步执行，返回字符串 |
| `executeAsBytes()` | 同步执行，返回字节数组 |
| `executeAsync()` | 异步执行，返回 CompletableFuture |
| `download(path)` | 下载文件 |

## 注意事项

1. **默认超时**：读取超时 2 分钟，连接超时 10 秒
2. **虚拟线程**：默认使用虚拟线程执行器
3. **自动编码**：查询参数自动进行 URL 编码
4. **响应流**：InputStream 只能读取一次
5. **线程安全**：Request 不是线程安全的，不应在多线程间共享
6. **异常处理**：网络异常会抛出 `ExtException`
7. **SSL 验证**：默认启用，使用 `insecure()` 禁用（仅开发环境）

## 许可证

[Apache License 2.0](../../LICENSE)
