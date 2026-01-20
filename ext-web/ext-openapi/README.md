# ext-openapi

> OpenAPI 文档模块 - 提供 SpringDoc OpenAPI 集成和 Swagger UI 支持

## 简介

`ext-openapi` 是 ext-library 的 API 文档模块，基于 SpringDoc OpenAPI 提供自动化的 API 文档生成功能。

## 快速开始

### Maven

```xml
<dependency>
    <groupId>ext.library</groupId>
    <artifactId>ext-openapi</artifactId>
</dependency>
```

### Gradle

```groovy
implementation("ext.library:ext-openapi")
```

## 依赖说明

| 依赖 | 说明 |
|------|------|
| ext-core | 核心工具类 |
| springdoc-openapi-starter-webmvc-ui | SpringDoc OpenAPI |
| therapi-runtime-javadoc | JavaDoc 运行时读取 |

## 功能特性

- SpringDoc OpenAPI 集成
- 自动 API 文档生成
- Javadoc 注解支持
- Swagger UI 界面
- 自定义文档配置
- 接口分组管理

## 配置项

| 配置项 | 默认值 | 说明 |
|-------|-------|------|
| springdoc.api-docs.path | /v3/api-docs | API 文档路径 |
| springdoc.swagger-ui.path | /swagger-ui.html | Swagger UI 路径 |
| springdoc.swagger-ui.enabled | true | 启用 Swagger UI |
| ext.openapi.title | API Documentation | 文档标题 |
| ext.openapi.description | - | 文档描述 |
| ext.openapi.version | - | API 版本 |
| ext.openapi.contact.name | - | 联系人 |
| ext.openapi.contact.email | - | 联系邮箱 |

## 访问地址

启动应用后，可通过以下地址访问：

| 地址 | 说明 |
|------|------|
| `/swagger-ui.html` | Swagger UI 界面 |
| `/v3/api-docs` | OpenAPI JSON |
| `/v3/api-docs.yaml` | OpenAPI YAML |

## 使用示例

### 配置 API 信息

```yaml
ext:
  openapi:
    title: "用户管理系统 API"
    description: "用户管理系统的 RESTful API 文档"
    version: "1.0.0"
    contact:
      name: "API Support"
      email: "support@example.com"
```

### 接口文档注解

```java
@Tag(name = "用户管理", description = "用户相关接口")
@RestController
@RequestMapping("/api/users")
public class UserController {

    @Operation(summary = "获取用户列表", description = "分页获取所有用户信息")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "成功"),
        @ApiResponse(responseCode = "500", description = "服务器错误")
    })
    @GetMapping
    public R<PageResult<UserDTO>> list(PageParam param) {
        // ...
    }

    @Operation(summary = "获取用户详情")
    @Parameter(name = "id", description = "用户ID", required = true)
    @GetMapping("/{id}")
    public R<UserDTO> getById(@PathVariable Long id) {
        // ...
    }
}
```

### 安全配置

```java
@OpenAPIDefinition(
    info = @Info(
        title = "API 文档",
        version = "1.0.0",
        description = "带认证的 API 文档"
    )
)
@SecuritySchemes({
    @SecurityScheme(
        name = "Authorization",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT"
    )
})
public class OpenApiConfig {
}
```

### 接口分组

```java
@Tag(name = "用户管理", description = "用户相关接口")
@RestController
@RequestMapping("/users")
public class UserController {
    // ...
}

@Tag(name = "订单管理", description = "订单相关接口")
@RestController
@RequestMapping("/orders")
public class OrderController {
    // ...
}
```

## 许可证

[Apache License 2.0](../../LICENSE)
