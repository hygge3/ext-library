# ext-json

> JSON 处理工具 - 提供 Jackson 增强配置和 JsonPath 查询支持

## 简介

`ext-json` 是 ext-library 的 JSON 处理模块，封装了 Jackson ObjectMapper 的增强配置和 JsonPath 查询能力。

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

| 类名 | 说明 |
|------|------|
| `JsonAutoConfig` | JSON 自动配置类 |

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

模块自动配置了以下 Jackson 特性：

- 日期时间格式化 (`yyyy-MM-dd HH:mm:ss`)
- 空值处理策略
- 未知属性忽略
- Long/BigInteger 超出 JS 安全整数范围时转为字符串
- BigDecimal 无科学计数法输出

## 许可证

[Apache License 2.0](../../LICENSE)
