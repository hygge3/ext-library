# ext-http

`ext-http` 是基于 JDK 11+ `HttpClient` 的 HTTP 客户端工具包，提供流畅的链式调用 API（Fluent API）。

## 特性

- **链式调用**：流畅的 API 设计，支持方法链式调用
- **多种 HTTP 方法**：支持 GET、POST、PUT、DELETE、PATCH 等
- **同步/异步**：支持同步和异步请求
- **多种数据格式**：支持 JSON、表单、文件上传下载
- **虚拟线程**：默认使用虚拟线程执行器，提升并发性能
- **内聚设计**：所有功能集中在一个类中，降低复杂度

## Maven 依赖

```xml
<dependency>
  <groupId>ext.library</groupId>
  <artifactId>ext-http</artifactId>
  <version>${version}</version>
</dependency>
```

## 快速开始

### 链式调用（推荐）

```java
// 同步 GET 请求
String result = HttpUtil.get("https://api.example.com/data")
    .header("Authorization", "Bearer token")
    .query("page", "1")
    .query("size", "10")
    .execute()
    .asString();
```

## API 文档

### 入口方法

| 方法 | 描述 |
|------|------|
| `HttpUtil.request(url)` | 创建通用请求构建器 |
| `HttpUtil.get(url)` | 创建 GET 请求 |
| `HttpUtil.post(url)` | 创建 POST 请求 |
| `HttpUtil.put(url)` | 创建 PUT 请求 |
| `HttpUtil.delete(url)` | 创建 DELETE 请求 |
| `HttpUtil.patch(url)` | 创建 PATCH 请求 |

### 请求配置方法

所有请求配置方法都返回 `HttpUtil.Request` 对象，支持链式调用。

#### HTTP 方法

```java
// 方式1：使用入口方法
HttpUtil.get(url)
HttpUtil.post(url)
HttpUtil.put(url)
HttpUtil.delete(url)
HttpUtil.patch(url)

// 方式2：使用 method() 方法
HttpUtil.request(url).method(HttpUtil.HttpMethod.POST)
HttpUtil.request(url).post()  // 快捷方法
HttpUtil.request(url).get()
HttpUtil.request(url).put()
HttpUtil.request(url).delete()
HttpUtil.request(url).patch()
```

#### 请求头

```java
// 添加单个请求头
HttpUtil.get(url)
    .header("Content-Type", "application/json")
    .header("Authorization", "Bearer token")

// 批量添加请求头
HttpUtil.get(url)
    .headers(Map.of(
        "Content-Type", "application/json",
        "Accept", "application/json"
    ))

// 常用快捷方法
HttpUtil.get(url)
    .contentType("application/json")           // Content-Type
    .accept("application/json")                // Accept
    .authorization("Bearer token")             // Authorization: Bearer token
    .bearer("token")                           // Authorization: Bearer token
    .contentTypeJson()                         // Content-Type: application/json; charset=UTF-8
    .contentTypeForm()                         // Content-Type: application/x-www-form-urlencoded
    .acceptJson()                              // Accept: application/json
    .acceptXml()                               // Accept: application/xml
```

#### 查询参数

```java
// 添加单个查询参数
HttpUtil.get(url)
    .query("page", "1")
    .query("size", "20")

// 批量添加查询参数
HttpUtil.get(url)
    .query(Map.of(
        "page", "1",
        "size", "20",
        "sort", "createdAt"
    ))

// 自动 URL 编码
HttpUtil.get(url)
    .query("name", "张三")  // 自动编码为 %E5%BC%A0%E4%B8%89
```

#### 请求体

```java
// JSON 字符串
HttpUtil.post(url)
    .contentTypeJson()
    .body("{\"name\":\"test\",\"age\":18}")

// 对象（自动调用 toString）
HttpUtil.post(url)
    .body(myObject)

// 表单数据（单值）
HttpUtil.post(url)
    .form("username", "admin")
    .form("password", "123456")

// 表单数据（批量）
HttpUtil.post(url)
    .form(Map.of(
        "username", "admin",
        "password", "123456"
    ))

// 字节数组
HttpUtil.post(url)
    .body(byteArray)

// 自定义 BodyPublisher
HttpUtil.post(url)
    .body(HttpRequest.BodyPublishers.ofFile(Path.of("file.txt")))
```

#### 超时配置

```java
// 毫秒为单位
HttpUtil.get(url)
    .timeout(5000)  // 5秒

// Duration 为单位
HttpUtil.get(url)
    .timeout(Duration.ofSeconds(5))
```

### 执行请求

#### 同步执行

```java
// 返回 HttpUtil.Response
HttpUtil.Response response = HttpUtil.get(url)
    .execute();

// 获取响应数据
String body = response.asString();
byte[] bytes = response.asBytes();
InputStream stream = response.asStream();
int statusCode = response.statusCode();
boolean isSuccess = response.isSuccess();

// 直接获取指定类型
String result = HttpUtil.get(url).executeAsString();
byte[] bytes = HttpUtil.get(url).executeAsBytes();
InputStream stream = HttpUtil.get(url).executeAsStream();
MyObject obj = HttpUtil.get(url).executeAs(MyObject.class);
```

#### 异步执行

```java
// 返回 CompletableFuture<HttpUtil.Response>
CompletableFuture<HttpUtil.Response> future = HttpUtil.get(url)
    .executeAsync();

// 链式调用
HttpUtil.get(url)
    .executeAsync()
    .thenAccept(response -> System.out.println(response.asString()))
    .exceptionally(ex -> {
        ex.printStackTrace();
        return null;
    });

// 直接获取指定类型
CompletableFuture<String> future = HttpUtil.get(url).executeAsyncAsString();
CompletableFuture<byte[]> bytesFuture = HttpUtil.get(url).executeAsyncAsBytes();
```

#### 下载文件

```java
// 同步下载
Path filePath = HttpUtil.get(url)
    .execute()
    .download("/path/to/save/file.pdf");

// 或使用下载方法
Path filePath = HttpUtil.get(url).download("/path/to/save/file.pdf");
```

### 响应处理

```java
HttpUtil.Response response = HttpUtil.get(url).execute();

// 状态码
int statusCode = response.statusCode();

// 判断状态
boolean success = response.isSuccess();        // 2xx
boolean redirect = response.isRedirect();      // 3xx
boolean clientError = response.isClientError(); // 4xx
boolean serverError = response.isServerError(); // 5xx

// 响应头
String contentType = response.contentType();
String header = response.header("X-Custom-Header");
long contentLength = response.contentLength();

// 响应体
String body = response.asString();
byte[] bytes = response.asBytes();
InputStream stream = response.asStream();

// 类型转换
MyObject obj = response.body(MyObject.class);
```

### 错误处理

```java
// 全局错误处理器
HttpUtil.get(url)
    .onError(ex -> {
        System.err.println("请求失败: " + ex.getMessage());
    })
    .execute();

// 响应处理器
HttpUtil.get(url)
    .onResponse((request, httpResponse) -> {
        System.out.println("响应状态: " + httpResponse.statusCode());
    })
    .execute();
```

## 完整示例

### GET 请求示例

```java
// 简单 GET 请求
String result = HttpUtil.get("https://api.example.com/users")
    .execute()
    .asString();

// 带请求头和查询参数
String result = HttpUtil.get("https://api.example.com/users")
    .header("Authorization", "Bearer " + token)
    .header("Accept", "application/json")
    .query("page", "1")
    .query("size", "20")
    .query("sort", "createdAt")
    .acceptJson()
    .execute()
    .asString();
```

### POST 请求示例

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

### PUT 请求示例

```java
// 更新资源
String result = HttpUtil.put("https://api.example.com/users/1")
    .contentTypeJson()
    .body("{\"name\":\"李四\"}")
    .execute()
    .asString();
```

### DELETE 请求示例

```java
// 删除资源
HttpUtil.delete("https://api.example.com/users/1")
    .execute();

// 带请求头的删除
HttpUtil.delete("https://api.example.com/users/1")
    .header("Authorization", "Bearer " + token)
    .execute();
```

### PATCH 请求示例

```java
// 部分更新
String result = HttpUtil.patch("https://api.example.com/users/1")
    .contentTypeJson()
    .body("{\"name\":\"王五\"}")
    .execute()
    .asString();
```

### 异步请求示例

```java
// 异步 GET
HttpUtil.get("https://api.example.com/data")
    .executeAsync()
    .thenAccept(response -> {
        System.out.println(response.asString());
    })
    .exceptionally(ex -> {
        ex.printStackTrace();
        return null;
    });

// 异步 POST
HttpUtil.post("https://api.example.com/users")
    .contentTypeJson()
    .body("{\"name\":\"test\"}")
    .executeAsync()
    .thenApply(HttpUtil.Response::asString)
    .thenAccept(System.out::println);

// 并发请求
List<CompletableFuture<String>> futures = IntStream.range(0, 10)
    .mapToObj(i -> HttpUtil.get("https://api.example.com/item/" + i)
        .executeAsyncAsString())
    .collect(Collectors.toList());

CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
    .thenAccept(v -> {
        List<String> results = futures.stream()
            .map(CompletableFuture::join)
            .collect(Collectors.toList());
        System.out.println("完成 " + results.size() + " 个请求");
    });
```

### 文件下载示例

```java
// 下载图片
Path imagePath = HttpUtil.get("https://example.com/image.jpg")
    .execute()
    .download("/tmp/image.jpg");

// 下载大文件
Path zipPath = HttpUtil.get("https://example.com/large-file.zip")
    .timeout(300000)  // 5分钟超时
    .download("/tmp/large-file.zip");
```

### 错误处理示例

```java
// 方式1：使用 onError
String result = HttpUtil.get("https://api.example.com/users")
    .onError(ex -> {
        if (ex instanceof java.net.ConnectException) {
            System.err.println("连接失败: " + ex.getMessage());
        } else if (ex instanceof java.util.concurrent.TimeoutException) {
            System.err.println("请求超时");
        } else {
            ex.printStackTrace();
        }
    })
    .execute()
    .asString();

// 方式2：使用 try-catch
try {
    String result = HttpUtil.get("https://api.example.com/users")
        .execute()
        .asString();
} catch (ExtException e) {
    System.err.println("HTTP 请求失败: " + e.getMessage());
}
```

## 类结构

```
HttpUtil
├── HttpMethod (枚举)
│   ├── GET
│   ├── POST
│   ├── PUT
│   ├── DELETE
│   ├── PATCH
│   ├── HEAD
│   └── OPTIONS
│
├── Request (请求构建器)
│   ├── method(), get(), post(), put(), delete(), patch()
│   ├── header(), headers(), contentType(), accept(), authorization(), bearer()
│   ├── query()
│   ├── body(), form(), multipartForm()
│   ├── timeout()
│   ├── execute(), executeAs(), executeAsString(), executeAsBytes(), executeAsStream()
│   ├── executeAsync(), executeAsyncAs(), executeAsyncAsString()
│   ├── download()
│   ├── onError(), onResponse()
│   └── contentTypeJson(), contentTypeForm(), acceptJson(), acceptXml()
│
├── Response (响应包装器)
│   ├── asString(), asBytes(), asStream()
│   ├── statusCode(), isSuccess(), isRedirect(), isClientError(), isServerError()
│   ├── header(), contentType(), contentLength()
│   ├── body(), getRawResponse()
│
└── HttpClientProps (客户端配置)
```

## 注意事项

1. **默认超时**：默认读取超时为 1200000 毫秒（20分钟）
2. **默认连接超时**：默认连接超时为 10000 毫秒（10秒）
3. **虚拟线程**：默认使用虚拟线程执行器，无需额外配置
4. **自动编码**：查询参数会自动进行 URL 编码
5. **Content-Type**：未显式设置时，GET/DELETE 请求不设置 Content-Type，其他请求默认使用 `application/json`
6. **请求体**：GET/DELETE 请求不支持请求体
7. **响应流**：InputStream 只能读取一次，读取后流会被关闭
8. **线程安全**：`HttpUtil.Request` 不是线程安全的，不应在多线程间共享
9. **异常处理**：网络异常会抛出 `ExtException` 异常
