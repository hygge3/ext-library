# ext-idempotent

> 幂等性控制模块 - 提供接口幂等性保障

## 简介

`ext-idempotent` 是 ext-library 的幂等性控制模块，通过注解和 AOP 实现接口的幂等性保障，防止重复请求。

## 快速开始

### Maven

```xml
<dependency>
    <groupId>ext.library</groupId>
    <artifactId>ext-idempotent</artifactId>
</dependency>
```

### Gradle

```groovy
implementation("ext.library:ext-idempotent")
```

## 依赖说明

| 依赖 | 说明 |
|------|------|
| ext-redis | Redis 存储模式 |
| caffeine | 可选，内存存储模式 |

## 功能特性

- 基于注解的幂等性控制
- 支持 SpEL 表达式
- 内存和 Redis 两种存储模式
- 可自定义幂等 Key 生成器
- 灵活的过期策略

## 配置项

| 配置项 | 默认值 | 说明 |
|-------|-------|------|
| ext.idempotent.key-store-type | MEMORY | MEMORY (内存) / REDIS |

## 注解属性

| 属性 | 默认值 | 说明 |
|------|-------|------|
| prefix | idem | 幂等标识前缀 |
| uniqueExpression | - | SpEL 表达式，提取唯一标识 |
| duration | 10 | 幂等控制时长 |
| timeUnit | SECONDS | 时间单位 |
| message | 重复请求，请稍后重试 | 重复请求提示信息 |
| removeKeyWhenFinished | false | 业务完成后是否清除 Key |
| removeKeyWhenError | false | 异常时是否清除 Key |

## 使用示例

### 基本使用

```java
@PostMapping("/order")
@Idempotent(
    prefix = "order",
    uniqueExpression = "#dto.orderNo",
    duration = 60,
    timeUnit = TimeUnit.SECONDS
)
public R<Order> createOrder(@RequestBody OrderDTO dto) {
    return R.ok(orderService.create(dto));
}
```

### 组合多个参数

```java
@PostMapping("/payment")
@Idempotent(
    prefix = "payment",
    uniqueExpression = "#userId + ':' + #dto.orderId"
)
public R<Void> pay(@RequestParam Long userId, @RequestBody PaymentDTO dto) {
    paymentService.pay(userId, dto);
    return R.ok();
}
```

### 自定义 Key 生成器

```java
@Component
public class IPKeyGenerator extends DefaultIdempotentKeyGenerator {
    @Override
    public String generate(JoinPoint joinPoint, Idempotent annotation) {
        String clientIP = ServletUtil.getClientIp();
        return clientIP + ":" + super.generate(joinPoint, annotation);
    }
}

@Configuration
public class IdempotentConfig {
    @Bean
    public IdempotentKeyGenerator keyGenerator() {
        return new IPKeyGenerator();
    }
}
```

### 业务完成后清除

```java
@PostMapping("/submit")
@Idempotent(
    uniqueExpression = "#dto.formId",
    removeKeyWhenFinished = true
)
public R<Void> submit(@RequestBody FormDTO dto) {
    formService.submit(dto);
    return R.ok();
}
```

## 许可证

[Apache License 2.0](../../LICENSE)
