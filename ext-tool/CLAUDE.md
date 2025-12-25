[根目录](../../CLAUDE.md) > **ext-tool**

# ext-tool 模块文档

## 模块职责

ext-tool 是整个 ext-library 的基础工具模块，提供通用的工具类和功能组件。它不依赖 Spring，可以独立使用。

## 入口与启动

该模块是纯工具类集合，无需自动配置或启动入口。

## 核心组件

### 1. 核心类 (core/)
- **Threads**: 线程工具类，提供线程相关的便捷方法
- **Systems**: 系统工具类，获取系统信息和环境变量
- **Runtimes**: 运行时工具类，JVM 相关操作
- **Exceptions**: 异常处理工具类
- **Logs**: 日志输出工具类
- **VirtualThreadPools**: 虚拟线程池支持

### 2. 工具类 (util/)
- **DateUtil**: 日期时间处理工具
- **CalcUtil**: 数学计算工具
- **StringUtil**: 字符串操作工具
- **CollUtil**: 集合操作工具
- **MapUtil**: Map 操作工具
- **IDUtil**: ID 生成器（雪花算法、UUIDv7、ObjectId）
- **ImageUtil**: 图片处理工具
- **IOUtil**: IO 流处理工具
- **ReflectionUtil**: 反射工具
- **StreamUtil**: Stream 流处理工具
- **ClassUtil**: 类操作工具
- **ObjectUtil**: 对象工具
- **HexUtil**: 十六进制转换工具
- **Base64Util**: Base64 编码工具
- **ValidatorUtil**: 验证器工具
- **INetUtil**: 网络工具（需要实现）
- **LatchUtil**: 并发工具

### 3. 持有器 (holder/)
- **Lazy**: 延迟加载工具
- **Once**: 一次性执行工具
- **Unchecked**: 函数式接口异常包装
- **retry/**: 重试机制支持

### 4. 领域对象 (domain/)
- **SnowflakeId**: 雪花算法 ID 实现
- **Version**: 版本号处理
- **MongoObjectId**: MongoDB ObjectId 生成
- **TreeBuilder**: 树形结构构建器

### 5. 异常 (exception/)
- **ExtException**: 通用运行时异常
- **BizException**: 业务异常（带错误码）
- **ToolException**: 工具类异常
- **BizCode**: 业务错误码枚举
- **ResponseCode**: 响应状态码枚举

### 6. 常量 (constant/)
- **Holder**: 常量持有类（CPU 核心数等）
- **PatternPool**: 常用正则表达式集合
- **EmojiSymbol**: Emoji 符号常量

### 7. 字典工具 (util/dict/)
- **IDict**: 字典接口
- **DictUtil**: 字典工具类

## 关键依赖

- **Google Guava 33.5.0**: 主要依赖，提供丰富的工具类
- 无 Spring 依赖，可独立使用

## 测试与质量

- 测试位置：`src/test/java/ext/library/tool/`
- 测试覆盖：6 个测试类
- 主要测试类：
  - `RuntimesTest`: 运行时工具测试
  - `SystemsTest`: 系统工具测试
  - `Base64UtilTest`: Base64 编码测试
  - `CalcUtilTest`: 计算工具测试
  - `MongoObjectIdTest`: MongoDB ObjectId 测试

## 使用示例

### ID 生成
```java
// 雪花算法 ID
Long snowflakeId = IDUtil.getSnowflakeId();

// UUID v7
String uuidv7 = IDUtil.getUUIDv7();

// MongoDB ObjectId
String objectId = IDUtil.getObjectId();
```

### 延迟初始化
```java
Lazy<Object> lazy = Lazy.of(() -> expensiveOperation());
Object value = lazy.get(); // 只会执行一次
```

### 异常包装
```java
// 使用 Unchecked 包装，避免处理受检异常
List<String> list = files.stream()
    .map(Unchecked.function(file -> Files.readString(file.toPath())))
    .collect(Collectors.toList());
```

### 业务异常
```java
throw new BizException(BizCode.FAIL, "操作失败");
```

## 常见问题 (FAQ)

### Q: 如何使用雪花算法生成 ID？
```java
Long id = IDUtil.getSnowflakeId();
```

### Q: 如何进行延迟初始化？
```java
Lazy<Object> lazy = Lazy.of(() -> expensiveOperation());
Object value = lazy.get(); // 只会执行一次
```

### Q: 如何包装函数式接口的异常？
```java
// 使用 Unchecked 包装，避免处理受检异常
List<String> list = files.stream()
    .map(Unchecked.function(file -> Files.readString(file.toPath())))
    .collect(Collectors.toList());
```

## 相关文件清单

### 主要源码文件
- `src/main/java/ext/library/tool/core/Threads.java`
- `src/main/java/ext/library/tool/core/Systems.java`
- `src/main/java/ext/library/tool/core/Runtimes.java`
- `src/main/java/ext/library/tool/core/Logs.java`
- `src/main/java/ext/library/tool/util/IDUtil.java`
- `src/main/java/ext/library/tool/holder/Lazy.java`
- `src/main/java/ext/library/tool/exception/BizException.java`

### 测试文件
- `src/test/java/ext/library/tool/core/RuntimesTest.java`
- `src/test/java/ext/library/tool/core/SystemsTest.java`
- `src/test/java/ext/library/tool/util/Base64UtilTest.java`
- `src/test/java/ext/library/tool/util/CalcUtilTest.java`
- `src/test/java/ext/library/tool/domain/MongoObjectIdTest.java`

## 变更记录 (Changelog)

### 2025-12-24
- 更新：补充完整的工具类列表
- 更新：新增常量和字典相关组件

### 2025-12-19
- 创建：模块文档
- 统计：约 50+ 个工具类
