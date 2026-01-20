# ext-core

> 核心功能模块 - 提供 SpEL 支持、Spring 工具、异步配置等核心能力

## 简介

`ext-core` 是 ext-library 的核心功能模块，基于 `ext-tool` 构建，提供与 Spring 框架深度集成的功能支持。

## 快速开始

### Maven

```xml
<dependency>
    <groupId>ext.library</groupId>
    <artifactId>ext-core</artifactId>
</dependency>
```

### Gradle

```groovy
implementation("ext.library:ext-core")
```

## 依赖说明

| 依赖 | 说明 |
|------|------|
| ext-tool | 基础工具类 |
| spring-boot-starter-validation | 参数校验 |
| mapstruct-plus-spring-boot-starter | 对象映射 |
| spring-boot-starter-aspectj | AOP 支持 |
| spring-web | Web 工具 |

## 包结构

```
ext.library.core
├── config/         # 配置类
├── properties/     # 配置属性
└── util/           # 工具类
    └── spel/       # SpEL 支持
```

## 功能模块

### SpEL 表达式支持 (util/spel)

| 类名 | 说明 |
|------|------|
| `SpelUtil` | SpEL 表达式工具类 |
| `ExtExpressionEvaluator` | 增强的表达式求值器 |
| `ExtExpressionRootObject` | 表达式根对象 |

### 工具类 (util)

| 类名 | 说明 |
|------|------|
| `SpringUtil` | Spring 上下文工具 |
| `BeanUtil` | Bean 操作工具 (MapStruct Plus 增强) |
| `ServletUtil` | Servlet 请求响应工具 |
| `MethodUtil` | 方法操作工具 |
| `AspectUtil` | AOP 切面工具 |

### 配置类 (config)

| 类名 | 说明 |
|------|------|
| `ThreadPoolConfig` | 线程池配置 |
| `AsyncConfig` | 异步配置 |

### 配置属性 (properties)

| 类名 | 说明 |
|------|------|
| `ThreadPoolProperties` | 线程池配置属性 |

## 使用示例

### Spring 上下文工具

```java
// 获取 Bean
UserService userService = SpringUtil.getBean(UserService.class);

// 获取配置
String value = SpringUtil.getProperty("app.config.key");

// 获取 ApplicationContext
ApplicationContext context = SpringUtil.getApplicationContext();
```

### SpEL 表达式

```java
// 解析 SpEL 表达式
String result = SpelUtil.parse("#user.name", context);

// 在注解中使用 SpEL
@Cache(key = "#user.id")
public User getUser(User user) { ... }
```

### Servlet 工具

```java
// 获取当前请求
HttpServletRequest request = ServletUtil.getRequest();

// 获取客户端 IP
String clientIp = ServletUtil.getClientIp();

// 获取请求参数
String param = ServletUtil.getParameter("key");
```

### Bean 操作 (MapStruct Plus)

```java
// 对象属性复制
UserVO vo = BeanUtil.copy(user, UserVO.class);

// 批量复制
List<UserVO> voList = BeanUtil.copyList(userList, UserVO.class);
```

### 切面工具

```java
// 在切面中获取方法信息
Method method = AspectUtil.getMethod(joinPoint);

// 获取方法参数
Object[] args = AspectUtil.getArgs(joinPoint);
```

## 配置属性

### 线程池配置

```yaml
ext:
  thread-pool:
    core-size: 8
    max-size: 16
    queue-capacity: 100
    keep-alive-seconds: 60
    thread-name-prefix: ext-pool-
```

## 许可证

[Apache License 2.0](../../LICENSE)
