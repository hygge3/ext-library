# ext-desensitize

> 数据脱敏模块 - 提供敏感数据脱敏功能

## 简介

`ext-desensitize` 是 ext-library 的数据脱敏模块，通过注解实现敏感数据的自动脱敏，保护用户隐私。

## 快速开始

### Maven

```xml
<dependency>
    <groupId>ext.library</groupId>
    <artifactId>ext-desensitize</artifactId>
</dependency>
```

### Gradle

```groovy
implementation("ext.library:ext-desensitize")
```

## 依赖说明

| 依赖 | 说明 |
|------|------|
| ext-tool | 基础工具类 |
| jackson-databind | JSON 序列化支持 |

## 功能特性

- 基于 Jackson 的自动脱敏
- 内置多种脱敏策略
- 支持自定义脱敏规则
- 注解驱动，无侵入

## 脱敏策略

| 策略 | 说明 | 示例 |
|------|------|------|
| PHONE | 手机号脱敏 | 138****8888 |
| ID_CARD | 身份证脱敏 | 110***********1234 |
| BANK_CARD | 银行卡脱敏 | 6222***********1234 |
| EMAIL | 邮箱脱敏 | t***@example.com |
| NAME | 姓名脱敏 | 张** |
| ADDRESS | 地址脱敏 | 北京市****** |
| PASSWORD | 密码脱敏 | ****** |
| CUSTOM | 自定义脱敏 | - |

## 使用示例

### 基本使用

```java
@Data
public class UserVO {

    private Long id;

    @Sensitive(strategy = SensitiveStrategy.PHONE)
    private String phone;

    @Sensitive(strategy = SensitiveStrategy.ID_CARD)
    private String idCard;

    @Sensitive(strategy = SensitiveStrategy.EMAIL)
    private String email;

    @Sensitive(strategy = SensitiveStrategy.NAME)
    private String name;

    @Sensitive(strategy = SensitiveStrategy.BANK_CARD)
    private String bankCard;

    @Sensitive(strategy = SensitiveStrategy.ADDRESS)
    private String address;
}
```

### 脱敏结果

```json
{
    "id": 1,
    "phone": "138****8888",
    "idCard": "110***********1234",
    "email": "t***@example.com",
    "name": "张**",
    "bankCard": "6222***********1234",
    "address": "北京市******"
}
```

### 自定义脱敏规则

```java
@Data
public class OrderVO {

    @Sensitive(
        strategy = SensitiveStrategy.CUSTOM,
        startInclude = 3,
        endExclude = 7,
        replacer = "*"
    )
    private String customField;
}
```

### 工具类脱敏

```java
// 手机号脱敏
String phone = DesensitizationUtil.mobilePhone("13888888888");
// 结果: 138****8888

// 身份证脱敏
String idCard = DesensitizationUtil.idCard("110101199001011234");
// 结果: 110***********1234

// 自定义脱敏
String custom = DesensitizationUtil.desensitize("abcdefgh", 2, 6, "*");
// 结果: ab****gh
```

## 许可证

[Apache License 2.0](../../LICENSE)
