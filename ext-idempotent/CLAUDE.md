[根目录](../CLAUDE.md) > **ext-idempotent**

# ext-idempotent 模块文档

## 模块职责

ext-idempotent 提供请求幂等性支持，通过注解方式防止重复提交。

## 入口与启动

### 自动配置类
- **IdempotentAutoConfig**: 幂等性自动配置

## 核心组件

### 1. 配置类 (config/)
- **IdempotentAutoConfig**: 幂等性自动配置

### 2. 配置属性 (properties/)
- **IdempotentProperties**: 幂等性配置参数

### 3. 注解 (annotation/)
- **@Idempotent**: 幂等性注解

### 4. 切面 (aspect/)
- **IdempotentAspect**: 幂等性切面处理器

### 5. 键生成器 (key/generator/)
- **IdempotentKeyGenerator**: 键生成器接口
- **DefaultIdempotentKeyGenerator**: 默认键生成器

### 6. 键存储 (key/store/)
- **IdempotentKeyStore**: 键存储接口
- **RedisIdempotentKeyStore**: Redis 键存储实现
- **InMemoryIdempotentKeyStore**: 内存键存储实现

## 关键依赖

- **ext-redis**: Redis 支持
- **caffeine**: 内存缓存（可选）

## 使用示例

### 基本使用
```java
@Idempotent(timeout = 60, timeUnit = TimeUnit.SECONDS)
public R<String> submitOrder(OrderRequest request) {
    // 处理订单提交
    return R.ok("订单提交成功");
}
```

### 自定义键生成
```java
@Component
public class CustomKeyGenerator implements IdempotentKeyGenerator {
    @Override
    public String generate(Idempotent idempotent, MethodInvocation invocation) {
        // 自定义键生成逻辑
        return "custom:" + invocation.getArgs()[0];
    }
}
```

## 常见问题 (FAQ)

### Q: 如何切换存储实现？
通过 `IdempotentProperties` 配置存储类型（Redis/Memory）。

### Q: 如何设置过期时间？
在 `@Idempotent` 注解中配置 timeout 和 timeUnit。

## 相关文件清单

### 主要源码文件
- `src/main/java/ext/library/idempotent/config/IdempotentAutoConfig.java`
- `src/main/java/ext/library/idempotent/annotation/Idempotent.java`
- `src/main/java/ext/library/idempotent/aspect/IdempotentAspect.java`
- `src/main/java/ext/library/idempotent/key/store/IdempotentKeyStore.java`

### 配置文件
- `src/main/resources/META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`

## 变更记录 (Changelog)

### 2025-12-24
- 创建：模块文档
