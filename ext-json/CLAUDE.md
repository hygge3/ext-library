[根目录](../CLAUDE.md) > **ext-json**

# ext-json 模块文档

## 模块职责

ext-json 提供 JSON 处理增强功能，包括 Jackson 定制配置、JsonPath 支持和大数值处理等。

## 入口与启动

### 自动配置类
- **CustomJacksonAutoConfig**: Jackson 自动配置

## 核心组件

### 1. 配置类 (config/)
- **CustomJacksonAutoConfig**: Jackson 自定义配置

### 2. 自定义模块 (module/)
- **CustomModule**: 自定义 Jackson 模块
  - 注册自定义序列化器

### 3. 序列化器 (serializer/)
- **BigNumberSerializer**: 大数值序列化器
- **BigDecimalPlainSerializer**: BigDecimal 保留精度序列化器

### 4. 工具类 (util/)
- **JsonUtil**: JSON 工具类
- **JsonNodeUtil**: JsonNode 操作工具
- **JsonPathUtil**: JsonPath 查询工具

## 关键依赖

- **ext-tool**: 基础工具类
- **spring-boot-starter-json**: JSON 支持
- **json-path**: JsonPath 查询支持

## 使用示例

### 基本使用
```java
// 对象转 JSON
String json = JsonUtil.toJson(object);

// JSON 转对象
User user = JsonUtil.parse(json, User.class);

// JSON 转集合
List<User> users = JsonUtil.parseList(json, User.class);

// JSON 转 Map
Map<String, Object> map = JsonUtil.parseMap(json);
```

### JsonPath 查询
```java
// 使用 JsonPath 查询
Object result = JsonPathUtil.read(json, "$.store.book[*].author");
```

### BigDecimal 处理
```java
// BigDecimal 保留精度序列化
BigDecimal amount = new BigDecimal("123.45");
String json = JsonUtil.toJson(amount); // "123.45" (不带科学计数法)
```

## 常见问题 (FAQ)

### Q: 如何处理大数值？
使用 `BigNumberSerializer` 自动处理大数值，避免精度丢失。

### Q: BigDecimal 如何保留精度？
使用 `BigDecimalPlainSerializer` 序列化时保留原始格式。

## 相关文件清单

### 主要源码文件
- `src/main/java/ext/library/json/config/CustomJacksonAutoConfig.java`
- `src/main/java/ext/library/json/util/JsonUtil.java`
- `src/main/java/ext/library/json/util/JsonPathUtil.java`
- `src/main/java/ext/library/json/serializer/BigDecimalPlainSerializer.java`

### 配置文件
- `src/main/resources/META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`

## 变更记录 (Changelog)

### 2025-12-24
- 创建：模块文档
