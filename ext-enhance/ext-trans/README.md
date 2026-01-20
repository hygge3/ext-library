# ext-trans

> 字段翻译模块 - 提供字段值的自动翻译功能

## 简介

`ext-trans` 是 ext-library 的翻译模块，通过注解实现字段值的自动翻译，如字典值翻译、用户名翻译等。

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

## 核心组件

| 组件 | 说明 |
|------|------|
| `@Translate` | 字段翻译注解 |
| `@TranslationType` | 翻译类型注解 |
| `Translator` | 翻译器接口 |
| `TranslatorRegistry` | 翻译器注册表 |
| `TranslationHandler` | Jackson 序列化处理器 |

## 使用示例

### 1. 实现翻译器

```java
@Component
@TranslationType("dict")
public class DictTranslator implements Translator<String> {

    @Autowired
    private DictService dictService;

    @Override
    public String translate(Object key, String param) {
        if (key == null) {
            return null;
        }
        // param 为字典类型，key 为字典值
        return dictService.getLabel(param, key.toString());
    }
}
```

### 2. 使用翻译注解

```java
@Data
public class UserVO {

    private Long id;

    private Integer status;

    /**
     * 状态标签（自动翻译）
     */
    @Translate(type = "dict", mapper = "status", param = "user_status")
    private String statusLabel;

    private Long deptId;

    /**
     * 部门名称（自动翻译）
     */
    @Translate(type = "dept", mapper = "deptId")
    private String deptName;
}
```

### 3. 序列化结果

```json
{
    "id": 1,
    "status": 1,
    "statusLabel": "正常",
    "deptId": 100,
    "deptName": "研发部"
}
```

## 注解说明

### @Translate

| 属性 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `type` | String | 是 | 翻译类型，对应 `@TranslationType` 的值 |
| `mapper` | String | 否 | 映射字段名，指定从哪个字段获取待翻译的值 |
| `param` | String | 否 | 附加参数，传递给翻译器 |

### @TranslationType

| 属性 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `value` | String | 是 | 翻译类型标识 |

## 自定义翻译器示例

### 用户翻译器

```java
@Component
@TranslationType("user")
public class UserTranslator implements Translator<String> {

    @Autowired
    private UserService userService;

    @Override
    public String translate(Object key, String param) {
        if (key == null) return null;
        User user = userService.getById(Long.valueOf(key.toString()));
        return user != null ? user.getNickname() : null;
    }
}
```

### 使用

```java
@Data
public class OrderVO {
    private Long creatorId;

    @Translate(type = "user", mapper = "creatorId")
    private String creatorName;
}
```

## 许可证

[Apache License 2.0](../../LICENSE)
