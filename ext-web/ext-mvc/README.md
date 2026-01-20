# ext-mvc

> Spring MVC 增强模块 - 提供统一响应封装、自定义验证器、异常处理等功能

## 简介

`ext-mvc` 是 ext-library 的 MVC 增强模块，提供统一的 Web 开发规范和便捷的开发工具。

## 快速开始

### Maven

```xml
<dependency>
    <groupId>ext.library</groupId>
    <artifactId>ext-mvc</artifactId>
</dependency>
```

### Gradle

```groovy
implementation("ext.library:ext-mvc")
```

## 依赖说明

| 依赖 | 说明 |
|------|------|
| ext-core | 核心工具类 |
| ext-json | JSON 处理 |
| spring-boot-starter-web | Spring MVC |

## 功能特性

- 统一响应封装（R 类）
- 分页查询支持（PageParam、PageResult）
- 全局异常处理
- 参数校验增强
- 请求/响应拦截器
- Trace 追踪
- CORS 跨域配置
- 自定义参数解析器

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
|------|------|
| `R<T>` | 统一响应封装类 |
| `PageResult<T>` | 分页结果封装 |
| `PageParam` | 分页参数 |
| `SelectData` | 下拉选择数据 |

### 处理器

| 类名 | 说明 |
|------|------|
| `GlobalResponseHandler` | 全局响应处理器 |
| `GlobalExceptionHandler` | 全局异常处理器 |
| `BodyParamHandlerMethodArgumentResolver` | 自定义参数解析器 |

## 自定义验证注解

| 注解 | 说明 |
|------|------|
| `@Chinese` | 中文字符校验 |
| `@English` | 英文字符校验 |
| `@Cellphone` | 手机号校验 |
| `@ZipCode` | 邮编校验 |
| `@Username` | 用户名校验 |
| `@Xss` | XSS 攻击校验 |
| `@RangeIn` | 范围校验 |
| `@Exclusion` | 排除值校验 |
| `@Mutual` | 互斥字段校验 |
| `@EnumValue` | 枚举值校验 |
| `@OneOfStrings` | 字符串枚举校验 |
| `@OneOfInts` | 整数枚举校验 |
| `@OneOfClasses` | 类枚举校验 |

## 使用示例

### 统一响应

```java
@RestController
@RequestMapping("/api")
public class DemoController {

    @GetMapping("/success")
    public R<String> success() {
        return R.ok("操作成功");
    }

    @GetMapping("/fail")
    public R<Void> fail() {
        return R.fail("操作失败");
    }

    @GetMapping("/data")
    public R<User> getData() {
        return R.ok(user);
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
public class UserDTO {

    @NotBlank(message = "用户名不能为空")
    @Username(message = "用户名格式不正确")
    private String username;

    @Cellphone(message = "手机号格式不正确")
    private String phone;

    @Chinese(message = "姓名必须是中文")
    private String name;

    @RangeIn(min = 18, max = 120, message = "年龄必须在18-120之间")
    private Integer age;

    @Xss(message = "内容包含非法字符")
    private String content;
}

@PostMapping("/save")
public R<Void> save(@RequestBody @Valid UserDTO dto) {
    userService.save(dto);
    return R.ok();
}
```

### 自定义 Body 参数解析

```java
@PostMapping("/body")
public R<Void> body(@BodyParam("userId") Long userId,
                    @BodyParam("name") String name) {
    // 从 JSON Body 中直接获取指定字段
    return R.ok();
}
```

## 许可证

[Apache License 2.0](../../LICENSE)
