[根目录](../CLAUDE.md) > **ext-security**

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

- `SecurityAuthority` - 权限校验核心类
- `SecurityEventPublishManager` - 安全事件发布管理
- `SecurityConstant` - 安全相关常量
- `SecurityService` - 安全服务接口

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
├── constants/         # 常量定义
│   └── SecurityConstant.java
├── domain/            # 领域模型
│   ├── SecuritySession.java
│   └── SecurityToken.java
├── enums/             # 枚举
│   ├── Logical.java
│   └── TokenState.java
├── interceptor/       # 拦截器
├── listener/          # 事件监听
│   └── SecurityEventPublishManager.java
├── properties/        # 配置属性
│   └── SecurityProperties.java
├── repository/        # 存储接口
│   ├── SecurityRepository.java
│   └── SecurityCacheRepository.java
├── router/            # 路由配置
├── service/           # 服务接口
│   └── SecurityService.java
└── util/              # 工具类
    └── SecurityUtil.java
```

## 配置说明

安全模块配置前缀：`ext.security`

| 配置项 | 默认值 | 说明 |
|-------|-------|------|
| security-name | Authorization | 认证名称 |
| timeout | 30d | 授权有效期，支持 Duration 格式（30d, 720h） |
| activity-timeout | 1h | 最低活跃频率，支持 Duration 格式（1h, 60m） |
| auto-renewal | true | 是否自动续约 |
| auto-renewal-interval | 3m | 自动续约间隔，支持 Duration 格式 |
| is-concurrent-login | true | 是否允许多地同时登录 |
| enable-cookie | true | 是否开启 cookie |

存储配置通过 ext-cache 模块的 `ext.cache` 前缀进行配置。

## 使用示例

```java
// 权限校验
@RequiresPermissions("user:read")
public User getUser(Long id) { ... }

// 角色校验
@RequiresRoles(value = {"admin", "manager"}, logical = Logical.OR)
public void adminOperation() { ... }

// 忽略校验
@SecurityIgnore
public String publicApi() { ... }
```

## 相关文件

- `src/main/java/ext/library/security/` - 安全模块源码
- `src/test/java/ext/library/security/` - 单元测试
- `pom.xml` - 模块依赖配置

## 变更记录

| 日期 | 变更内容 |
|------|----------|
| 2026-01-26 | 时间配置改用 Duration 类型，支持 30d、1h、3m 等格式 |
| 2026-01-26 | 重构存储层，改用 ext-cache 替代独立的 RAM/Redis 实现 |
| 2026-01-19 | 初始化 CLAUDE.md 文档 |
