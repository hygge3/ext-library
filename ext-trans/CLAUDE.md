[根目录](../CLAUDE.md) > **ext-trans**

# ext-trans 模块文档

## 模块职责

ext-trans 提供数据翻译和转换功能，支持字典翻译等场景。

## 入口与启动

### 自动配置类
- **TranslationAutoConfig**: 翻译自动配置

## 核心组件

### 1. 配置类 (config/)
- **TranslationAutoConfig**: 翻译自动配置

### 2. 注解 (annotation/)
- **@Translation**: 翻译注解
- **@TranslationType**: 翻译类型注解

### 3. 服务 (service/)
- **TranslationInterface**: 翻译服务接口
- **DictTypeTranslationImpl**: 字典类型翻译实现

### 4. 处理器 (handler/)
- **TranslationHandler**: 翻译处理器
- **TranslationBeanSerializerModifier**: Bean 序列化修饰器

### 5. 常量 (constant/)
- **TransConstant**: 翻译常量

## 关键依赖

- **jackson-databind**: Jackson 数据绑定

## 使用示例

### 字典翻译
```java
public class UserVO {
    private Long id;
    private String name;

    @Translation(type = "gender")
    private Integer genderType;

    @Translation(type = "status")
    private Integer status;
}
```

### 自定义翻译
```java
@Component
public class CustomTranslationImpl implements TranslationInterface {
    @Override
    public void translation(Object field, Object bean) {
        // 自定义翻译逻辑
    }
}
```

## 常见问题 (FAQ)

### Q: 如何自定义翻译实现？
实现 `TranslationInterface` 接口并注册为 Bean。

## 相关文件清单

### 主要源码文件
- `src/main/java/ext/library/translation/config/TranslationAutoConfig.java`
- `src/main/java/ext/library/translation/annotation/Translation.java`
- `src/main/java/ext/library/translation/service/TranslationInterface.java`
- `src/main/java/ext/library/translation/handler/TranslationHandler.java`

### 配置文件
- `src/main/resources/META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`

## 变更记录 (Changelog)

### 2025-12-24
- 创建：模块文档
