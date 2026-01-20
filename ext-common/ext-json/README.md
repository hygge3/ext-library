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
├── module/         # 自定义模块
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

### 自定义模块 (module)

| 类名 | 说明 |
|------|------|
| `CustomModule` | 自定义 Jackson 模块 |

### 自动配置 (config)

| 类名 | 说明 |
|------|------|
| `CustomJacksonAutoConfig` | Jackson 自动配置类 |

## 使用示例

### JSON 序列化与反序列化

```java
// 对象转 JSON 字符串
String json = JsonUtil.toJson(user);

// JSON 字符串转对象
User user = JsonUtil.parse(json, User.class);

// JSON 字符串转 List
List<User> users = JsonUtil.parseList(json, User.class);

// JSON 字符串转 Map
Map<String, Object> map = JsonUtil.parseMap(json);
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

// 查询单个值
String title = JsonPathUtil.read(json, "$.store.book[0].title");

// 查询列表
List<String> titles = JsonPathUtil.readList(json, "$.store.book[*].title");
```

### JsonNode 操作

```java
// 解析为 JsonNode
JsonNode node = JsonNodeUtil.parse(json);

// 获取字段值
String name = JsonNodeUtil.getString(node, "name");
int age = JsonNodeUtil.getInt(node, "age");

// 遍历数组
JsonNodeUtil.forEach(node.get("items"), item -> {
    // 处理每个元素
});
```

## 自动配置特性

模块自动配置了以下 Jackson 特性：

- 日期时间格式化 (ISO 8601)
- 空值处理策略
- 未知属性忽略
- Long/BigInteger 转字符串 (防止 JS 精度丢失)
- BigDecimal 无科学计数法输出

## 许可证

[Apache License 2.0](../../LICENSE)
