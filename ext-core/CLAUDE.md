[根目录](../../CLAUDE.md) > **ext-core**

# ext-core 模块文档

## 模块职责

ext-core 是 Spring Boot 集成的基础模块，提供 Spring 生态的增强功能和工具类。它依赖 ext-tool，并为其他模块提供 Spring 集成支持。

## 入口与启动

### 自动配置类
- **ThreadPoolConfig**: 线程池自动配置
- **AsyncConfig**: 异步任务配置

## 核心组件

### 1. 配置类 (config/)
- **ThreadPoolConfig**:
  - 配置异步任务线程池 (`threadPoolTaskExecutor`)
  - 配置定时任务线程池 (`scheduledExecutorService`)
  - 支持 graceful shutdown

### 2. 配置属性 (config/properties/)
- **ThreadPoolProperties**: 线程池配置参数绑定
  - `core-pool-size`: 核心线程数
  - `max-pool-size`: 最大线程数
  - `queue-capacity`: 队列容量
  - `keep-alive-seconds`: 线程存活时间

### 3. 工具类 (util/)
- **BeanUtil**: Bean 操作工具
  - 集成 MapStruct Plus
  - 提供对象转换功能
  - CGLIB BeanCopier 作为回退
- **SpelUtil**: Spring 表达式语言工具
  - 支持动态表达式解析
  - 可访问方法参数、返回值、Bean 属性
- **AspectUtil**: AOP 切面工具
- **SpringUtil**: Spring 上下文工具
- **ServletUtil**: Servlet 相关工具
- **ValidatorUtil**: 验证工具
- **MethodUtil**: 方法操作工具

### 4. SpEL 支持 (util/spel/)
- **SpelUtil**: SpEL 表达式解析器
- **ExtExpressionEvaluator**: 自定义表达式求值器
- **ExtExpressionRootObject**: 表达式根对象

### 5. 常量 (constant/)
- **PatternPool**: 常用正则表达式集合

## 关键依赖

- **ext-tool**: 基础工具类
- **spring-boot-starter-validation**: 验证支持
- **mapstruct-plus**: 对象映射
- **spring-boot-starter-aop**: AOP 支持
- **jakarta.servlet-api**: Servlet API

## 测试与质量

- 当前状态：缺少单元测试
- 建议：补充工具类和配置的单元测试

## 使用示例

### 线程池配置
```yaml
thread-pool:
  enabled: true
  core-pool-size: 8
  max-pool-size: 20
  queue-capacity: 1000
  keep-alive-seconds: 60
```

### Bean 转换
```java
// 使用 BeanUtil 进行对象转换
TargetDTO dto = BeanUtil.convert(entity, TargetDTO.class);

// 列表转换
List<TargetDTO> dtos = BeanUtil.convert(entities, TargetDTO.class);
```

### SpEL 表达式
```java
// 解析 SpEL 表达式
String value = SpelUtil.parseValueToString("#user.name", context);
```

## 常见问题 (FAQ)

### Q: 如何禁用默认线程池？
```yaml
thread-pool:
  enabled: false
```

### Q: SpEL 如何访问方法参数？
```java
// 在注解中使用
@Cache(key = "#user.id + ':' + #operation")
public Result someMethod(User user, String operation) { ... }
```

## 相关文件清单

### 主要源码文件
- `src/main/java/ext/library/core/config/ThreadPoolConfig.java`
- `src/main/java/ext/library/core/util/BeanUtil.java`
- `src/main/java/ext/library/core/util/SpelUtil.java`
- `src/main/java/ext/library/core/config/properties/ThreadPoolProperties.java`

### 配置文件
- `src/main/resources/META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`

## 变更记录 (Changelog)

### 2025-12-19
- 📝 创建模块文档
- 🔧 适配 Spring Boot 4.0.0