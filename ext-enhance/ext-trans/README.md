# ext-trans

> 字段翻译模块 - 提供字典翻译和字段转换功能

## 简介

`ext-trans` 是 ext-library 的翻译模块，通过注解实现字段值的自动翻译，如字典值翻译、枚举翻译等。

## 快速开始

### Maven

```xml
<dependency>
    <groupId>ext.library</groupId>
    <artifactId>ext-trans</artifactId>
</dependency>
```

### Gradle

```groovy
implementation("ext.library:ext-trans")
```

## 依赖说明

| 依赖 | 说明 |
|------|------|
| jackson-databind | JSON 序列化支持 |

## 功能特性

- 字段翻译注解
- 字典值翻译
- 统一翻译接口
- 自动序列化处理
- 灵活的翻译类型

## 核心注解

| 注解 | 说明 |
|------|------|
| `@Translation` | 字段翻译注解 |
| `@TranslationType` | 翻译类型注解 |

## 核心类说明

| 类名 | 说明 |
|------|------|
| `TranslationInterface` | 翻译接口 |
| `TranslationHandler` | 翻译处理器 |
| `TranslationBeanSerializerModifier` | 序列化修改器 |

## 使用示例

### 定义翻译类型

```java
@TranslationType("userStatus")
public enum UserStatus {
    NORMAL(1, "正常"),
    LOCKED(2, "锁定"),
    DELETED(3, "已删除");

    private final Integer code;
    private final String description;
}
```

### 实现翻译接口

```java
@Component
public class DictTranslation implements TranslationInterface {

    @Autowired
    private DictService dictService;

    @Override
    public String translation(Object value, String type) {
        if (value == null) {
            return null;
        }
        return dictService.getLabel(type, value.toString());
    }
}
```

### 使用翻译注解

```java
@Data
public class UserDTO {

    private Long id;

    @Translation(type = "userStatus")
    private Integer status;

    private String statusLabel; // 翻译后的文本

    @Translation(type = "gender")
    private Integer gender;

    private String genderLabel;
}
```

### 序列化结果

```json
{
    "id": 1,
    "status": 1,
    "statusLabel": "正常",
    "gender": 0,
    "genderLabel": "男"
}
```

### 自定义翻译实现

```java
@Component
@TranslationType("orderStatus")
public class OrderStatusTranslation implements TranslationInterface {

    @Override
    public String translation(Object value, String type) {
        Integer status = Convert.toInt(value);
        return switch (status) {
            case 1 -> "待支付";
            case 2 -> "已支付";
            case 3 -> "已完成";
            case 4 -> "已取消";
            default -> "未知";
        };
    }
}
```

## 许可证

[Apache License 2.0](../../LICENSE)
