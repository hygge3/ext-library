# ext-json

> JSON 处理工具 - 提供 Jackson 增强配置、JsonPath 查询、字段翻译与数据脱敏

## 简介

`ext-json` 是 ext-library 的 JSON 处理模块，封装了 Jackson ObjectMapper 的增强配置、JsonPath 查询能力，以及基于 Jackson 的字段翻译 (`ext.library.trans`) 与数据脱敏 (`ext.library.desensitize`) 扩展。

## 快速开始

### Maven

```xml
<dependency>
    <groupId>ext.library</groupId>
    <artifactId>ext-json</artifactId>
</dependency>
```

### Gradle

```groovy
implementation("ext.library:ext-json")
```

## 依赖说明

| 依赖 | 说明 |
|------|------|
| ext-tool | 基础工具类 |
| spring-boot-starter-json | Jackson JSON 支持 |
| json-path | JsonPath 查询支持 |

## 包结构

```
ext.library.json
├── config/         # 自动配置
├── module/         # Jackson 扩展模块
├── serializer/     # 序列化器
└── util/           # 工具类

ext.library.trans          # 字段翻译
ext.library.desensitize    # 数据脱敏
```

## 功能模块

### 工具类 (util)

| 类名 | 说明 |
|------|------|
| `JsonUtil` | JSON 序列化/反序列化工具 |
| `JsonNodeUtil` | JsonNode 操作工具 |
| `JsonPathUtil` | JsonPath 查询工具 |

### 序列化器 (serializer)

| 类名 | 说明 |
|------|------|
| `BigDecimalPlainSerializer` | BigDecimal 无科学计数法序列化 |
| `BigNumberSerializer` | 大数字转字符串序列化 (防止 JS 精度丢失) |

### 扩展模块 (module)

| 类名 | 说明 |
|------|------|
| `ExtJacksonModule` | 扩展 Jackson 模块 |

### 自动配置 (config)

| 类名                                | 说明                    |
|-------------------------------------|-------------------------|
| `JsonAutoConfiguration`          | JSON 自动配置类          |
| `TranslationAutoConfiguration`      | 字段翻译自动配置类，开关 `ext.trans.enabled`（默认 true）|

## 使用示例

### JSON 序列化与反序列化

```java
// 对象转 JSON 字符串
String json = JsonUtil.toJson(user);

// 对象转格式化 JSON 字符串
String prettyJson = JsonUtil.toPrettyJson(user);

// 序列化时排除指定字段（需配合 @JsonFilter 注解）
String filtered = JsonUtil.toJsonExcluding(user, "userFilter", "password", "secret");

// JSON 字符串转对象
User user = JsonUtil.readObj(json, User.class);

// JSON 字符串转 List
List<User> users = JsonUtil.readList(json, User.class);

// JSON 字符串转 Map
Map<String, Object> map = JsonUtil.readMap(json);

// JSON 字符串转指定 Key/Value 类型的 Map
Map<String, User> userMap = JsonUtil.readMap(json, String.class, User.class);

// 复杂泛型类型反序列化
List<Map<String, User>> result = JsonUtil.readGeneric(json,
    new TypeReference<List<Map<String, User>>>() {});

// 对象类型转换
User user = JsonUtil.convert(map, User.class);

// 校验 JSON 格式
boolean valid = JsonUtil.isValidJson(json);
```

### JsonPath 查询

```java
String json = """
    {
        "store": {
            "book": [
                {"title": "Book1", "price": 10},
                {"title": "Book2", "price": 20}
            ]
        }
    }
    """;

// 一次性读取
String title = JsonPathUtil.read(json, "$.store.book[0].title");

// 解析为 ReadContext 进行多次查询（推荐）
ReadContext context = JsonPathUtil.parse(json);
String title1 = context.read("$.store.book[0].title");
Integer price = context.read("$.store.book[0].price");

// 检查路径是否存在
boolean exists = JsonPathUtil.hasPath(json, "$.store.book[0].author");
boolean exists2 = JsonPathUtil.hasPath(context, "$.store.book[0].author");

// 读取值，路径不存在时返回默认值
String author = JsonPathUtil.readOrDefault(json, "$.store.book[0].author", "Unknown");
```

### JsonNode 操作

```java
// 解析 JSON 字符串为 JsonNode
JsonNode node = JsonNodeUtil.parseNode(json);

// 对象转 JsonNode
JsonNode userNode = JsonNodeUtil.toNode(user);

// JsonNode 转对象
User user = JsonNodeUtil.toObj(node, User.class);

// JsonNode 转 List
List<User> users = JsonNodeUtil.toList(arrayNode, User.class);

// 获取字段值（字段不存在时返回 null）
String name = JsonNodeUtil.getFieldValue(node, "name", String.class);
Integer age = JsonNodeUtil.getFieldValue(node, "age", Integer.class);

// 创建空节点
ObjectNode objectNode = JsonNodeUtil.createObjectNode();
ArrayNode arrayNode = JsonNodeUtil.createArrayNode();
```

## 自动配置特性

`JsonUtil` 内部持有的 `JsonMapper` 与 Spring 容器管理的 `JsonMapper` 共用同一份配置（`JsonUtil.customize`），两者序列化行为一致：

- 日期时间格式化（`LocalDateTime` → `yyyy-MM-dd HH:mm:ss`，`LocalDate` → `yyyy-MM-dd`，`LocalTime` → `HH:mm:ss`）；未单独注册序列化器的 `Instant`/`ZonedDateTime`/`Date` 同样输出字符串而非数字时间戳（`WRITE_DATES_AS_TIMESTAMPS` 关闭）
- 不序列化 null/空值/空集合（`NON_EMPTY`）
- 空对象不报错（`FAIL_ON_EMPTY_BEANS` 关闭）
- 未知属性忽略（`FAIL_ON_UNKNOWN_PROPERTIES` 关闭）
- 浮点数反序列化为 `BigDecimal`（`USE_BIG_DECIMAL_FOR_FLOATS`）
- Long/BigInteger 超出 JS 安全整数范围（±9007199254740991）时转为字符串；边界值本身可精确表示，仍输出数字
- BigDecimal 以 JSON 数字输出（不是字符串），使用 `toPlainString()` 避免科学计数法，并保留原始 scale

## 字段翻译

```java
@Component
@TranslationType("dict")
public class DictTranslator implements Translator<String> {
    @Override
    public String translate(Object key, String param) {
        return dictService.getLabel(param, key.toString());
    }
}

// 在 DTO 上标注
public class OrderVO {
    @Translate(type = "dict", param = "user_status")
    private String statusLabel;
}
```

自动配置会扫描容器中所有 `Translator` 实现类，按 `@TranslationType` 注册到 `TranslatorRegistry`。未注册的类型会记警告并原样输出字段值。

## 数据脱敏

```java
public class UserVO {
    // 只写 @Sensitive 即走默认 GENERIC 策略（保留前 1/3 + 后 1/3）
    private String remark;

    @Sensitive(strategy = DesensitizeStrategy.PHONE)
    private String phone;

    @Sensitive(strategy = DesensitizeStrategy.ID_CARD)
    private String idCard;

    // 显式跳过脱敏（仅限确认不敏感的字段）
    @Sensitive(strategy = DesensitizeStrategy.NONE)
    private String nickname;

    // 自定义规则
    @Sensitive(customRule = MyCustomRule.class)
    private String customField;
}
```

内置策略见 `DesensitizeStrategy`（通用、身份证、手机号、地址、邮箱、银行卡、中文姓名、密码、IP）。默认策略为 `GENERIC`：保留前 1/3 和后 1/3，中间按字符掩码，输出长度与输入一致（`13812345678` -> `1381***5678`）。短输入（前后各 1/3 会占满全文）退化为等长全掩码。

规则实例会被缓存复用；脱敏过程抛异常或规则加载失败时输出全掩码 `******`，绝不回退到明文。

`@Sensitive` 仅对 `String` 字段（或其 getter）生效，且**只作用于序列化方向**：脱敏后的值不是合法手机号/身份证，严禁用于持久化、缓存、ES 或消息队列。

## 许可证

[Apache License 2.0](../../LICENSE)
