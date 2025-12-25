# ext-security（安全框架）

## 功能

- 轻量级安全认证
- 基于路由的权限控制
- 内存和 Redis 两种存储模式
- 灵活的认证策略
- 安全事件监听
- 异常统一处理

## 依赖引用

### Maven

```xml
<dependency>
    <groupId>ext.library</groupId>
    <artifactId>ext-security</artifactId>
    <version>${version}</version>
</dependency>
```

### Gradle

```groovy
compile("ext.library:ext-security:${version}")
```

## 依赖模块

ext-security 依赖以下模块：
- ext-http：HTTP 客户端
- ext-json：JSON 处理
- ext-redis：可选，Redis 存储模式
- ext-crypto：加密支持

## 配置项

| 配置项 | 默认值 | 说明 |
|-------|-------|------|
| ext.security.repository | RAM | 存储方式：RAM、REDIS |
| ext.security.exclude-path | - | 排除路径 |
| ext.security.token-header | Authorization | Token 请求头 |
| ext.security.token-prefix | Bearer | Token 前缀 |

## 核心类说明

| 类名 | 说明 |
|-----|------|
| SecurityService | 安全服务 |
| SecurityRouter | 路由安全配置 |
| SecurityAuthority | 权限 authority |
| SecurityInterceptor | 安全拦截器 |
| SecurityRepository | 存储接口 |
| SecurityRamRepository | 内存存储实现 |
| SecurityRedisRepository | Redis 存储实现 |
| SecurityListener | 安全事件监听 |
| SecurityExceptionHandler | 异常处理器 |

## 使用示例

### 基础配置

```java
@Configuration
@EnableSecurity
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
    return securityService.getCurrentUser();
}

public boolean hasRole(String role) {
    return securityService.hasRole(role);
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
