[根目录](../CLAUDE.md) > **ext-desensitize**

# ext-desensitize 模块文档

## 模块职责

ext-desensitize 提供敏感数据脱敏功能，通过注解方式自动脱敏返回数据中的敏感字段。

## 入口与启动

该模块通过注解和 Jackson 序列化器实现，无需显式配置。

## 核心组件

### 1. 注解 (annotation/)
- **@Sensitive**: 敏感数据脱敏注解

### 2. 处理器 (handler/)
- **SensitiveHandler**: 敏感数据处理接口

### 3. 策略 (strategy/)
- **IDesensitizeRule**: 脱敏规则接口
- **UnknownDesensitizeRule**: 未知脱敏规则

## 关键依赖

- **ext-tool**: 基础工具类
- **jackson-databind**: Jackson 数据绑定

## 使用示例

### 基本使用
```java
public class UserVO {
    private Long id;
    private String username;

    @Sensitive
    private String phone;

    @Sensitive
    private String email;

    @Sensitive
    private String idCard;
}
```

### 自定义脱敏规则
```java
@Component
public class CustomDesensitizeRule implements IDesensitizeRule {
    @Override
    public String desensitize(String value) {
        // 自定义脱敏逻辑
        return "***";
    }
}
```

## 常见问题 (FAQ)

### Q: 如何自定义脱敏规则？
实现 `IDesensitizeRule` 接口并注册为 Bean。

### Q: 支持哪些数据类型？
支持 String 类型的字段脱敏。

## 相关文件清单

### 主要源码文件
- `src/main/java/ext/library/desensitize/annotation/Sensitive.java`
- `src/main/java/ext/library/desensitize/strategy/IDesensitizeRule.java`
- `src/main/java/ext/library/desensitize/handler/SensitiveHandler.java`

## 变更记录 (Changelog)

### 2025-12-24
- 创建：模块文档
