[根目录](../CLAUDE.md) > **ext-web**

# ext-web 模块文档

## 模块职责

ext-web 提供 Web 层增强功能，包括全局异常处理、统一响应格式、自定义验证注解等。

## 入口与启动

### 自动配置类
- **WebMvcAutoConfig**: Web MVC 自动配置
- **ValidationAutoConfig**: 验证自动配置

## 核心组件

### 1. 配置类 (config/)
- **WebMvcAutoConfig**: Web MVC 配置
- **ValidationAutoConfig**: 验证配置

### 2. 配置属性 (properties/)
- **WebMvcProperties**: Web MVC 配置参数

### 3. 响应处理 (response/)
- **R**: 统一响应格式
- **PageResult**: 分页响应格式
- **PageParam**: 分页请求参数
- **SelectData**: 选择数据响应格式

### 4. 处理器 (handler/)
- **GlobalExceptionHandler**: 全局异常处理器
- **GlobalResponseHandler**: 全局响应处理器

### 5. 拦截器 (interceptor/)
- **ExtWebInvokeTimeInterceptor**: 请求耗时拦截器

### 6. 过滤器 (filter/)
- **TraceFilter**: 链路追踪过滤器

### 7. 注解 (annotation/)
- **IgnoreRestWrapper**: 忽略响应包装注解

### 8. 验证 (validation/)
- **ValidationGroups**: 验证分组
- **EmptyCurlyToDefaultMessageInterpolator**: 自定义消息插值器
- **constraints/**: 自定义验证注解
  - `@Cellphone`: 手机号验证
  - `@Username`: 用户名验证
  - `@Chinese`: 中文验证
  - `@English`: 英文验证
  - `@ZipCode`: 邮编验证
  - `@Xss`: XSS 防护
  - `@RangeIn`: 范围验证
  - `@Mutual`: 互相验证
  - `@Exclusion`: 排除验证
  - `@OneOfStrings`: 字符串枚举验证
  - `@OneOfInts`: 整数枚举验证
  - `@OneOfClasses`: 类枚举验证
- **validator/**: 验证器实现

### 9. 启动监听 (launch/)
- **StartupListener**: 启动监听器

### 10. 请求参数解析 (body/resolver/)
- **BodyParam**: 自定义请求参数注解
- **BodyParamHandlerMethodArgumentResolver**: 请求参数解析器

## 关键依赖

- **ext-core**: Spring 基础支持
- **ext-json**: JSON 处理支持
- **spring-boot-starter-web**: Web 支持
- **swagger-annotations**: OpenAPI 注解支持

## 使用示例

### 统一响应格式
```java
@GetMapping("/user/{id}")
public R<User> getUser(@PathVariable Long id) {
    User user = userService.findById(id);
    return R.ok(user);
}
```

### 全局异常处理
```java
// 自动捕获异常并返回统一格式
// 无需手动处理
```

### 自定义验证注解
```java
public class UserRequest {
    @Cellphone
    private String phone;

    @Username
    private String username;
}
```

### 分页查询
```java
@GetMapping("/users")
public R<PageResult<User>> listUsers(PageParam pageParam) {
    return R.ok(userService.list(pageParam));
}
```

### 忽略响应包装
```java
@IgnoreRestWrapper
@GetMapping("/export")
public void export(HttpServletResponse response) {
    // 直接输出，不包装响应
}
```

## 常见问题 (FAQ)

### Q: 如何禁用响应包装？
使用 `@IgnoreRestWrapper` 注解。

### Q: 如何自定义验证消息？
在验证注解中指定 message 参数。

### Q: 如何获取请求耗时？
通过 `ExtWebInvokeTimeInterceptor` 自动记录。

## 相关文件清单

### 主要源码文件
- `src/main/java/ext/library/web/config/WebMvcAutoConfig.java`
- `src/main/java/ext/library/web/handler/GlobalExceptionHandler.java`
- `src/main/java/ext/library/web/response/R.java`
- `src/main/java/ext/library/web/validation/ValidationAutoConfig.java`

### 配置文件
- `src/main/resources/META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`

## 变更记录 (Changelog)

### 2025-12-24
- 创建：模块文档
