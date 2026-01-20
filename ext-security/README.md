# ext-security

> 安全框架模块 - 提供轻量级认证授权和权限控制

## 简介

`ext-security` 是 ext-library 的安全模块，提供完整的认证授权框架，包括用户认证、会话管理、基于注解的权限控制等功能。

## 快速开始

### Maven

```xml
<dependency>
    <groupId>ext.library</groupId>
    <artifactId>ext-security</artifactId>
</dependency>
```

### Gradle

```groovy
implementation("ext.library:ext-security")
```

## 依赖说明

| 依赖 | 说明 |
|------|------|
| ext-http | HTTP 客户端 |
| ext-json | JSON 处理 |
| ext-crypto | 加密支持 |
| ext-redis | 可选，Redis 存储模式 |
| spring-boot-starter-web | 可选，Web 支持 |

## 功能特性

- 轻量级安全认证
- 基于路由的权限控制
- 基于注解的权限校验
- 内存和 Redis 两种存储模式
- 灵活的认证策略
- 安全事件监听
- 异常统一处理

## 配置项

| 配置项 | 默认值 | 说明 |
|-------|-------|------|
| ext.security.repository | RAM | 存储方式：RAM、REDIS |
| ext.security.exclude-path | - | 排除路径 |
| ext.security.token-header | Authorization | Token 请求头 |
| ext.security.token-prefix | Bearer | Token 前缀 |

## 包结构

```
ext.library.security
├── annotion/          # 安全注解
├── authority/         # 权限校验
├── config/            # 自动配置
├── constants/         # 常量定义
├── domain/            # 领域模型
├── enums/             # 枚举
├── exception/         # 异常
├── handler/           # 异常处理器
├── interceptor/       # 拦截器
├── listener/          # 事件监听
├── properties/        # 配置属性
├── repository/        # 存储接口
├── router/            # 路由配置
├── service/           # 服务接口
└── util/              # 工具类
```

## 权限注解

| 注解 | 说明 |
|------|------|
| `@RequiresPermissions` | 权限校验 |
| `@RequiresRoles` | 角色校验 |
| `@SecurityIgnore` | 忽略安全校验 |

## 核心类说明

| 类名 | 说明 |
|------|------|
| `SecurityService` | 安全服务 |
| `SecurityRouter` | 路由安全配置 |
| `SecurityAuthority` | 权限校验核心 |
| `SecurityInterceptor` | 安全拦截器 |
| `SecurityRepository` | 存储接口 |
| `SecurityRamRepository` | 内存存储实现 |
| `SecurityRedisRepository` | Redis 存储实现 |
| `SecurityListener` | 安全事件监听 |
| `SecurityExceptionHandler` | 异常处理器 |

## 使用示例

### 基础配置

```yaml
ext:
  security:
    repository: RAM  # 或 REDIS
    token-header: Authorization
    token-prefix: Bearer
    exclude-path:
      - /api/public/**
      - /health
```

### 路由权限配置

```java
@Configuration
public class SecurityConfig {

    @Bean
    public SecurityRouter securityRouter() {
        return SecurityRouter.builder()
            .request("/api/admin/**").hasRole("ADMIN")
            .request("/api/user/**").hasAnyRole("USER", "ADMIN")
            .request("/api/public/**").permitAll()
            .build();
    }
}
```

### 使用注解控制权限

```java
@RestController
@RequestMapping("/api/user")
public class UserController {

    @RequiresPermissions("user:read")
    @GetMapping("/{id}")
    public User getUser(@PathVariable Long id) {
        return userService.getById(id);
    }

    @RequiresRoles(value = {"admin", "manager"}, logical = Logical.OR)
    @PostMapping
    public User createUser(@RequestBody User user) {
        return userService.create(user);
    }

    @SecurityIgnore
    @GetMapping("/public/info")
    public String publicInfo() {
        return "Public information";
    }
}
```

### 自定义认证

```java
@Service
public class AuthService {

    @Autowired
    private SecurityService securityService;

    public String login(String username, String password) {
        // 验证用户
        User user = userService.validate(username, password);
        // 生成 Token
        return securityService.generateToken(user);
    }

    public void logout(String token) {
        securityService.invalidate(token);
    }
}
```

### 获取当前用户

```java
public User getCurrentUser() {
    return SecurityUtil.getCurrentUser();
}

public boolean hasRole(String role) {
    return SecurityUtil.hasRole(role);
}

public boolean hasPermission(String permission) {
    return SecurityUtil.hasPermission(permission);
}
```

### 事件监听

```java
@Component
public class MySecurityListener implements SecurityListener {

    @Override
    public void onLoginSuccess(String userId) {
        log.info("用户登录成功: {}", userId);
    }

    @Override
    public void onLoginFailed(String username) {
        log.warn("用户登录失败: {}", username);
    }

    @Override
    public void onLogout(String userId) {
        log.info("用户登出: {}", userId);
    }
}
```

## 许可证

[Apache License 2.0](../LICENSE)
