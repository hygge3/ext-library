# ext-web（Web 增强）

## 功能

- 统一响应封装（R 类）
- 分页查询支持（PageParam、PageResult）
- 全局异常处理
- 参数校验增强
- 请求/响应拦截器
- Trace 追踪
- CORS 跨域配置
- 自定义参数解析器

## 依赖引用

### Maven

```xml
<dependency>
    <groupId>ext.library</groupId>
    <artifactId>ext-web</artifactId>
    <version>${version}</version>
</dependency>
```

### Gradle

```groovy
compile("ext.library:ext-web:${version}")
```

## 依赖模块

ext-web 依赖以下模块：
- ext-core：核心工具
- ext-json：JSON 处理

## 配置项

| 配置项 | 默认值 | 说明 |
|-------|-------|------|
| ext.web.trace-enabled | true | 是否启用 Trace 追踪 |
| ext.web.invoke-time-enabled | true | 是否启用调用时间统计 |
| ext.web.cors.allowed-origins | * | 允许的跨域源 |
| ext.web.cors.allowed-methods | GET,POST | 允许的 HTTP 方法 |

## 核心类说明

### 响应类

| 类名 | 说明 |
|-----|------|
| R | 统一响应封装类 |
| PageResult | 分页结果封装 |
| PageParam | 分页参数 |
| SelectData | 下拉选择数据 |

### 处理器

| 类名 | 说明 |
|-----|------|
| GlobalResponseHandler | 全局响应处理器 |
| GlobalExceptionHandler | 全局异常处理器 |
| BodyParamHandlerMethodArgumentResolver | 自定义参数解析器 |

### 校验注解

| 注解 | 说明 |
|-----|------|
| @Chinese | 中文校验 |
| @English | 英文校验 |
| @Cellphone | 手机号校验 |
| @ZipCode | 邮编校验 |
| @Username | 用户名校验 |
| @EnumValue | 枚举值校验 |
| @RangeIn | 范围校验 |
| @Exclusion | 排除校验 |
| @Mutual | 互斥校验 |
| @OneOfStrings | 字符串枚举校验 |
| @OneOfInts | 整数枚举校验 |
| @OneOfClasses | 类枚举校验 |
| @Xss | XSS 校验 |

## 使用示例

### 统一响应

```java
@RestController
@RequestMapping("/api")
public class DemoController {

    @GetMapping("/success") {
        return R.ok("操作成功");
    }

    @GetMapping("/fail") {
        return R.fail("操作失败");
    }

    @GetMapping("/data") {
        return R.ok(data);
    }
}
```

### 分页查询

```java
@PostMapping("/list")
public R<PageResult<UserDTO>> list(@RequestBody PageParam param) {
    PageResult<UserDTO> result = userService.pageQuery(param);
    return R.ok(result);
}
```

### 参数校验

```java
@PostMapping("/save")
public R<Void> save(@RequestBody @Valid UserDTO dto) {
    userService.save(dto);
    return R.ok();
}
```

### 自定义 Body 参数

```java
@PostMapping("/body")
public R<Void> body(@BodyParam("userId") Long userId,
                    @BodyParam("name") String name) {
    // 处理请求
}
```
