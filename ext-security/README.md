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

| 依赖            | 说明                                    |
|---------------|---------------------------------------|
| ext-useragent | UserAgent 解析                          |
| ext-core      | 核心工具                                  |
| ext-json      | JSON 处理                               |
| ext-cache     | 缓存支持（支持 Caffeine/Redis/PostgreSQL/L2） |
| ext-mvc       | MVC 支持                                |
| ext-crypto    | 加密支持                                  |

## 功能特性

- 轻量级安全认证，基于 Token + Session 双层模型
- 基于注解的权限校验（`@RequiresPermissions`、`@RequiresRoles`）
- 多设备登录控制（并发登录数、设备类型数限制）
- 多种缓存后端支持（Caffeine、Redis、PostgreSQL、L2 二级缓存）
- Token 自动续约
- 安全事件监听
- 异常统一处理（401/403）

## 配置项

```yaml
ext:
  security:
    token-name: Authorization          # token 在 Header/参数中的键名
    token-prefix: "Bearer "            # token 前缀（含尾部空格）
    timeout: 30d                       # 授权有效期，支持 Duration 格式
    activity-timeout: 1h               # 最低活跃频率
    auto-renewal: true                 # 是否自动续约
    auto-renewal-interval: 3m          # 自动续约间隔
    is-concurrent-login: true          # 是否允许多地同时登录
    max-login-limit: -1                # 同一账号最大登录数，-1 不限制
    max-login-device-type-limit: -1    # 同一账号允许的设备类型数，-1 不限制
    issue-token-max-limit: -1          # 全局颁发 token 最大数，-1 不限制
    enable-cookie: true                # 是否开启 cookie
    cookie-config:
      cookie-name: Token               # cookie 名称
      http-only: true                  # 是否禁止 JS 操作
      domain:                          # cookie 域
      path:                            # cookie 路径
      secure:                          # 是否仅 HTTPS 发送
  cache:
    cache-storage: L2                  # CAFFEINE/REDIS/POSTGRES/L2
    l2-backend: REDIS                  # L2 模式下的分布式后端
```

## 包结构

```
ext.library.security
├── annotation/        # 安全注解
├── authority/         # 权限校验接口
├── config/            # 自动配置
├── domain/            # 领域模型（纯数据，无 Spring 依赖）
├── enums/             # 枚举
├── exception/         # 异常
├── handler/           # 异常处理器
├── interceptor/       # 拦截器
├── listener/          # 事件监听
├── properties/        # 配置属性
├── repository/        # 存储接口与实现
├── service/           # 服务（持久化编排层）
└── util/              # 工具类
```

## 权限注解

| 注解                     | 说明                 |
|------------------------|--------------------|
| `@RequiresPermissions` | 权限码校验，支持 AND/OR 逻辑 |
| `@RequiresRoles`       | 角色码校验，支持 AND/OR 逻辑 |
| `@SecurityIgnore`      | 忽略安全校验，直接放行        |

## 核心类说明

| 类名                         | 说明                                     |
|----------------------------|----------------------------------------|
| `SecurityService`          | 安全服务，统一负责所有持久化编排                       |
| `SecurityUtil`             | 静态工具类，封装常用操作                           |
| `SecurityAuthority`        | 权限接口，业务方实现以提供角色/权限数据                   |
| `SecurityInterceptor`      | 安全拦截器，校验 token 及注解权限                   |
| `SecurityRepository`       | 存储接口                                   |
| `SecurityCacheRepository`  | 基于 ext-cache 的存储实现，tokenIndex 持久化支持分布式 |
| `SecurityListener`         | 安全事件监听接口                               |
| `SecurityExceptionHandler` | 统一处理 401/403 异常                        |

## 使用示例

### 实现权限接口

```java

@Component
public class MySecurityAuthority implements SecurityAuthority {

    @Override
    public List<String> getPermissionCodeList(String loginId) {
        return permissionService.listByUserId(loginId);
    }

    @Override
    public List<String> getRoleCodeList(String loginId) {
        return roleService.listByUserId(loginId);
    }
}
```

### 登录 / 退出

```java
// 登录（自动识别设备类型）
SecurityUtil.doLogin(userId.toString());

// 登录并挂载自定义数据到 Session
        SecurityUtil.

doLogin(userId.toString(),userInfo);

// 退出
        SecurityUtil.

logout();

// 踢下线 / 顶下线 / 封禁 / 解封
SecurityUtil.

kickToken(token);
SecurityUtil.

replaceToken(token);
SecurityUtil.

bannedToken(token);
SecurityUtil.

unsealToken(token);
```

### 获取当前用户信息

```java
// 获取当前登录 ID
String loginId = SecurityUtil.getCurrentLoginId();

// 获取当前 Session
SecuritySession session = SecurityUtil.getCurrentSecuritySession();

// 获取 Session 挂载的自定义数据
UserInfo userInfo = session.getAttributes(UserInfo.class);

// 获取当前 token 值
String token = SecurityUtil.getCurrentTokenValue();

// 是否已登录
boolean logged = SecurityUtil.isLogin();
```

### 注解权限控制

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

### 事件监听

```java

@Component
public class MySecurityListener implements SecurityListener {

    @Override
    public void doLogin(String loginId, String token, SecurityLoginParams loginModel) {
        log.info("登录成功：loginId={}, deviceType={}", loginId, loginModel.getDeviceType());
    }

    @Override
    public void doLogout(String loginId, String token, String deviceType) {
        log.info("退出登录：loginId={}", loginId);
    }
}
```

> 注册监听器：`SecurityListenerManager.registerListener(new MySecurityListener());`

## 许可证

[Apache License 2.0](../LICENSE)
