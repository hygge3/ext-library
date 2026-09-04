[根目录](../AGENTS.md) > **ext-security**

# ext-security - 安全层

> 提供认证、授权、权限控制等安全功能

## 模块职责

ext-security 是一个独立的安全模块，提供完整的认证授权框架，包括：

- 用户认证与会话管理
- 基于注解的权限控制
- 角色与权限校验
- 多存储后端支持（基于 ext-cache，支持 Caffeine/Redis/PostgreSQL/L2）

## 主要功能

### 权限注解

- `@RequiresPermissions` - 权限校验注解
- `@RequiresRoles` - 角色校验注解
- `@SecurityIgnore` - 忽略安全校验

### 逻辑运算

- `Logical.AND` - 需要满足所有条件
- `Logical.OR` - 满足任一条件即可

### 存储后端

- `SecurityRepository` - 安全信息存储接口
- `SecurityCacheRepository` - 基于 ext-cache 的存储实现（支持多种缓存后端）

### 核心组件

- `SecurityAuthority` - 权限校验接口（需业务实现）
- `SecurityEventPublishManager` - 安全事件发布管理
- `SecurityService` - 安全服务（持久化编排层，统一负责所有 I/O 操作）

## 依赖关系

```
ext-useragent (UserAgent 解析)
ext-core (核心工具)
ext-json (JSON 处理)
ext-cache (缓存支持，支持 Caffeine/Redis/PostgreSQL/L2)
ext-mvc (MVC 支持)
ext-crypto (加密工具)
spring-boot-starter-web (可选)
```

## 包结构

```
ext.library.security
├── annotation/        # 安全注解
│   ├── RequiresPermissions.java
│   ├── RequiresRoles.java
│   └── SecurityIgnore.java
├── authority/         # 权限校验
│   └── SecurityAuthority.java
├── config/            # 自动配置
│   └── SecurityAutoConfiguration.java
├── domain/            # 领域模型（纯数据，无 Spring 依赖）
│   ├── SecurityLoginParams.java
│   ├── SecuritySession.java
│   └── SecurityToken.java
├── enums/             # 枚举
│   ├── Logical.java
│   └── TokenState.java
├── exception/         # 异常
│   ├── ForbiddenException.java
│   └── UnauthorizedException.java
├── handler/           # 异常处理器
│   └── SecurityExceptionHandler.java
├── interceptor/       # 拦截器
│   └── SecurityInterceptor.java
├── listener/          # 事件监听
│   ├── SecurityEventPublishManager.java
│   ├── SecurityListener.java
│   └── SecurityListenerManager.java
├── properties/        # 配置属性
│   ├── CookieProperties.java
│   └── SecurityProperties.java
├── repository/        # 存储接口
│   ├── SecurityRepository.java
│   └── SecurityCacheRepository.java
├── service/           # 服务（持久化编排层）
│   └── SecurityService.java
└── util/              # 工具类
    ├── PermissionUtil.java
    └── SecurityUtil.java
```

## 配置说明

安全模块配置前缀：`ext.security`

| 配置项 | 默认值 | 说明 |
| ------- | ------- | ------ |
| token-name | Authorization | token 在 HTTP Header / 请求参数中的键名 |
| token-prefix | Bearer | token 前缀，拼接在 token 值之前（含尾部空格） |
| timeout | 30d | 授权有效期，支持 Duration 格式（30d, 720h） |
| activity-timeout | 1h | 最低活跃频率，支持 Duration 格式（1h, 60m） |
| auto-renewal | true | 是否自动续约 |
| auto-renewal-interval | 3m | 自动续约间隔，支持 Duration 格式 |
| is-concurrent-login | true | 是否允许多地同时登录 |
| max-login-limit | -1 | 同一账号最大登录数量，-1 不限制 |
| max-login-device-type-limit | -1 | 同一账号允许同时登录的设备类型数量，-1 不限制 |
| issue-token-max-limit | -1 | 全局颁发 token 最大数量，-1 不限制 |
| enable-cookie | true | 是否开启 cookie |
| cookie-config.cookie-name | Token | cookie 名称 |
| cookie-config.http-only | true | 是否禁止 JS 操作 Cookie |
| cookie-config.domain | | cookie 域设置 |
| cookie-config.path | | cookie 路径设置 |
| cookie-config.secure | | 是否仅在 HTTPS 下发送 |

存储配置通过 ext-cache 模块的 `ext.cache` 前缀进行配置。

## 使用示例

```java
// 登录
SecurityUtil.doLogin(userId.toString());

// 登录并携带自定义数据
SecurityUtil.doLogin(userId.toString(), userInfo);

// 获取当前登录 ID
String loginId = SecurityUtil.getCurrentLoginId();

// 获取当前 Session 挂载的自定义数据
UserInfo userInfo = SecurityUtil.getCurrentSecuritySession().getAttributes(UserInfo.class);

// 退出
SecurityUtil.logout();

// 踢下线 / 顶下线 / 封禁 / 解封
SecurityUtil.kickToken(token);
SecurityUtil.replaceToken(token);
SecurityUtil.bannedToken(token);
SecurityUtil.unsealToken(token);

// 是否已登录
boolean logged = SecurityUtil.isLogin();
```

```java
// 权限校验注解
@RequiresPermissions("user:read")
public User getUser(Long id) { ... }

// 角色校验（满足任一）
@RequiresRoles(value = {"admin", "manager"}, logical = Logical.OR)
public void adminOperation() { ... }

// 忽略安全校验
@SecurityIgnore
public String publicApi() { ... }
```

```java
// 实现权限接口（必须注册为 Spring Bean）
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

## 相关文件

- `src/main/java/ext/library/security/` - 安全模块源码
- `src/test/java/ext/library/security/` - 单元测试
- `pom.xml` - 模块依赖配置

## 变更记录

| 日期 | 变更内容 |
| ------ | ---------- |
| 2026-02-18 | SRP 重构：领域对象移除 Spring 容器依赖，持久化逻辑统一收归 SecurityService；修复多处 bug（tokenIndex 分布式、createLoginByLoginId token 持久化、issueTokenMaxLimit 逻辑反向、PermissionUtil AND 分支）；修复 CookieProperties final 字段、SecurityExceptionHandler 注解、SecurityListenerManager 线程安全 |
| 2026-01-26 | 时间配置改用 Duration 类型，支持 30d、1h、3m 等格式 |
| 2026-01-26 | 重构存储层，改用 ext-cache 替代独立的 RAM/Redis 实现 |
| 2026-01-19 | 初始化 AGENTS.md 文档 |
