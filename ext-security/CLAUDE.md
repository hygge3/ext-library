[根目录](../CLAUDE.md) > **ext-security**

# ext-security 模块文档

## 模块职责

ext-security 提供安全认证和授权功能，支持 Token 和 Session 管理、权限验证等。

## 入口与启动

### 自动配置类
- **SecurityAutoConfig**: 安全模块自动配置

## 核心组件

### 1. 配置类 (config/)
- **SecurityAutoConfig**: 安全自动配置

### 2. 配置属性 (properties/)
- **SecurityProperties**: 安全配置参数

### 3. 服务 (service/)
- **SecurityService**: 安全服务接口

### 4. 拦截器 (interceptor/)
- **SecurityInterceptor**: 安全拦截器

### 5. 路由 (router/)
- **SecurityRouter**: 安全路由配置

### 6. 授权 (authority/)
- **SecurityAuthority**: 安全授权

### 7. 仓储 (repository/)
- **SecurityRepository**: 安全仓储接口
- **SecurityRamRepository**: RAM 仓储实现
- **SecurityRedisRepository**: Redis 仓储实现

### 8. 领域对象 (domain/)
- **SecurityToken**: 安全令牌
- **SecuritySession**: 安全会话
- **SecurityLoginParams**: 登录参数

### 9. 注解 (annotion/)
- **@RequiresRoles**: 角色验证注解
- **@RequiresPermissions**: 权限验证注解
- **@SecurityIgnore**: 忽略安全验证注解

### 10. 异常 (exception/)
- **UnauthorizedException**: 未授权异常
- **ForbiddenException**: 禁止访问异常

### 11. 枚举 (enums/)
- **SecurityRepositoryEnum**: 仓储类型枚举
- **Logical**: 逻辑运算符枚举

### 12. 常量 (constants/)
- **SecurityConstant**: 安全常量
- **SecurityRedisConstant**: Redis 常量

### 13. 监听器 (listener/)
- **SecurityListener**: 安全监听器接口
- **SecurityListenerManager**: 监听器管理器
- **SecurityEventPublishManager**: 事件发布管理器

## 关键依赖

- **ext-http**: HTTP 客户端支持
- **ext-json**: JSON 处理支持
- **ext-redis**: Redis 支持（可选）
- **ext-crypto**: 加密支持
- **spring-boot-starter-web**: Web 支持（可选）

## 使用示例

### 登录
```java
@Autowired
private SecurityService securityService;

// 创建登录参数
SecurityLoginParams params = new SecurityLoginParams()
    .setDeviceType("web")
    .setTimeout(3600L)
    .setAttribute("userInfo", userInfo);

// 登录并获取 Token
SecurityToken token = securityService.login("userId", params);
```

### 权限验证
```java
@RequiresPermissions("user:read")
public User getUser(Long id) {
    // 需要用户读取权限
}

@RequiresRoles("admin")
public void deleteUser(Long id) {
    // 需要管理员角色
}
```

### 忽略安全验证
```java
@SecurityIgnore
@GetMapping("/public")
public R<String> publicApi() {
    // 不需要安全验证
}
```

## 常见问题 (FAQ)

### Q: 如何切换仓储实现？
通过 `SecurityProperties` 配置仓储类型（RAM/Redis）。

### Q: 如何自定义权限验证？
实现 `SecurityAuthority` 接口并注册为 Bean。

## 相关文件清单

### 主要源码文件
- `src/main/java/ext/library/security/config/SecurityAutoConfig.java`
- `src/main/java/ext/library/security/service/SecurityService.java`
- `src/main/java/ext/library/security/domain/SecurityLoginParams.java`
- `src/main/java/ext/library/security/domain/SecurityToken.java`
- `src/main/java/ext/library/security/domain/SecuritySession.java`

### 配置文件
- `src/main/resources/META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`

## 变更记录 (Changelog)

### 2025-12-24
- 创建：模块文档
