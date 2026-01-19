# ext-openapi（OpenAPI 文档）

## 功能

- SpringDoc OpenAPI 集成
- 自动 API 文档生成
- Javadoc 注解支持
- Swagger UI 界面
- 自定义文档配置
- 接口分组管理

## 依赖引用

### Maven

```xml
<parameter name="groupId">ext.library</groupId>
<artifactId>ext-openapi</artifactId>
<version>${version}</version>
</dependency>
```

### Gradle

```groovy
compile("ext.library:ext-openapi:${version}")
```

## 依赖模块

ext-openapi 依赖以下模块：
- ext-core：核心工具

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

## 使用示例

### 访问 Swagger UI

启动应用后，访问：`http://localhost:8080/swagger-ui.html`

### API 文档路径

- Swagger UI：`http://localhost:8080/swagger-ui.html`
- OpenAPI JSON：`http://localhost:8080/v3/api-docs`
- OpenAPI YAML：`http://localhost:8080/v3/api-docs.yaml`

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

### 启用/禁用特定接口

```java
@RestController
@RequestMapping("/api")
public class UserController {

    @Operation(summary = "获取用户列表", description = "分页获取所有用户信息")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "成功"),
        @ApiResponse(responseCode = "500", description = "服务器错误")
    })
    @GetMapping("/users")
    public R<PageResult<UserDTO>> list() {
        // ...
    }
}
```

### 分组配置

```java
@OpenAPIDefinition(
    info = @Info(
        title = "API 文档",
        version = "1.0.0",
        description = "多组API文档"
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

### 接口分组显示

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
