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
| ext-useragent | UserAgent 解析 |
| ext-core | 核心工具 |
| ext-json | JSON 处理 |
| ext-cache | 缓存支持（支持 Caffeine/Redis/PostgreSQL/L2） |
| ext-mvc | MVC 支持 |
| ext-crypto | 加密支持 |
| spring-boot-starter-web | 可选，Web 支持 |

## 功能特性

- 轻量级安全认证
- 基于路由的权限控制
- 基于注解的权限校验
- 多种缓存后端支持（Caffeine、Redis、PostgreSQL、L2 二级缓存）
- 灵活的认证策略
- 安全事件监听
- 异常统一处理

## 配置项

| 配置项 | 默认值 | 说明 |
|-------|-------|------|
| ext.security.security-name | Authorization | 认证名称 |
| ext.security.timeout | 30d | 授权有效期，支持 Duration 格式（30d, 720h, 2592000s） |
| ext.security.activity-timeout | 1h | 最低活跃频率，支持 Duration 格式（1h, 60m, 3600s） |
| ext.security.auto-renewal | true | 是否自动续约 |
| ext.security.auto-renewal-interval | 3m | 自动续约间隔，支持 Duration 格式 |
| ext.security.is-concurrent-login | true | 是否允许多地同时登录 |
| ext.security.enable-cookie | true | 是否开启 cookie |

存储配置通过 ext-cache 模块进行（参考 [ext-cache 配置](../ext-infra/ext-cache/)）：

| 配置项 | 默认值 | 说明 |
|-------|-------|------|
| ext.cache.cache-storage | L2 | 缓存存储方式：CAFFEINE/REDIS/POSTGRES/L2 |
| ext.cache.l2-backend | REDIS | L2 模式下的分布式后端：REDIS/POSTGRES |

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
| `SecurityCacheRepository` | 基于 ext-cache 的存储实现 |
| `SecurityListener` | 安全事件监听 |
| `SecurityExceptionHandler` | 异常处理器 |

## 使用示例

### 基础配置

```yaml
ext:
  security:
    security-name: Authorization
    timeout: 30d               # 授权有效期，支持 Duration 格式
    activity-timeout: 1h       # 活跃超时时间
    auto-renewal: true
    auto-renewal-interval: 3m  # 自动续约间隔
    is-concurrent-login: true
    enable-cookie: true
  cache:
    cache-storage: L2          # CAFFEINE/REDIS/POSTGRES/L2
    l2-backend: REDIS          # L2 模式下的分布式后端
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
