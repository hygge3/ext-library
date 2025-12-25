[根目录](../CLAUDE.md) > **ext-openapi**

# ext-openapi 模块文档

## 模块职责

ext-openapi 提供 OpenAPI 文档生成功能，基于 SpringDoc 进行定制增强。

## 入口与启动

该模块通过 SpringDoc 自动配置，无需显式配置。

## 核心组件

### 1. 处理器 (handler/)
- **OpenApiHandler**: 自定义 OpenAPI 处理器
  - 继承自 SpringDoc 的 OpenAPIService
  - 自定义 Tag 生成逻辑
  - 使用 Java 注释作为 Tag 名称

### 2. 配置类 (config/)
- **OpenApiAutoConfig**: OpenAPI 自动配置

## 关键依赖

- **ext-core**: Spring 基础支持
- **springdoc-openapi-starter-webmvc-ui**: SpringDoc 支持
- **therapi-runtime-javadoc**: Javadoc 注释读取支持

## 使用示例

### 访问文档
启动应用后访问：
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

### 自定义 Tag
```java
/**
 * 用户管理
 *
 * 用户相关操作接口
 */
@RestController
@RequestMapping("/api/users")
public class UserController {
    // 接口方法
}
```

## 常见问题 (FAQ)

### Q: Tag 名称如何生成？
使用 Java 类注释的第一行作为 Tag 名称。

### Q: 如何自定义文档？
通过 SpringDoc 的注解（@Tag、@Operation）自定义。

## 相关文件清单

### 主要源码文件
- `src/main/java/ext/library/handler/OpenApiHandler.java`
- `src/main/java/ext/library/openapi/config/OpenApiAutoConfig.java`

### 配置文件
- `src/main/resources/META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`

## 变更记录 (Changelog)

### 2025-12-24
- 创建：模块文档
