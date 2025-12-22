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
- **VirtualThreadPools**: 虚拟线程池支持

### 2. 工具类 (util/)
- **DateUtil**: 日期时间处理工具
- **CalcUtil**: 数学计算工具
- **StringUtil**: 字符串操作工具
- **CollUtil**: 集合操作工具
- **MapUtil**: Map 操作工具
- **IDUtil**: 雪花算法 ID 生成器
- **ImageUtil**: 图片处理工具
- **IOUtil**: IO 流处理工具
- **ReflectionUtil**: 反射工具
- **StreamUtil**: Stream 流处理工具
- **TreeBuilder**: 树形结构构建工具

### 3. 持有器 (holder/)
- **Lazy**: 延迟加载工具
- **Once**: 一次性执行工具
- **Unchecked**: 函数式接口异常包装

### 4. 领域对象 (domain/)
- **SnowflakeId**: 雪花算法 ID 实现
- **Version**: 版本号处理
- **MongoObjectId**: MongoDB ObjectId 生成

### 5. 异常 (exception/)
- **ExtException**: 通用运行时异常
- **BizException**: 业务异常（带错误码）
- **BizCode**: 业务错误码枚举

## 关键依赖

- **Google Guava**: 主要依赖，提供丰富的工具类
- 无 Spring 依赖，可独立使用

## 测试与质量

- 测试位置：`src/test/java/ext/library/tool/`
- 测试覆盖：6 个测试类
- 主要测试类：
  - `RuntimesTest`: 运行时工具测试
  - `SystemsTest`: 系统工具测试
  - `Base64UtilTest`: Base64 编码测试
  - `CalcUtilTest`: 计算工具测试

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
- `src/main/java/ext/library/tool/util/IDUtil.java`
- `src/main/java/ext/library/tool/holder/Lazy.java`
- `src/main/java/ext/library/tool/exception/BizException.java`

### 测试文件
- `src/test/java/ext/library/tool/core/RuntimesTest.java`
- `src/test/java/ext/library/tool/util/Base64UtilTest.java`

## 变更记录 (Changelog)

### 2025-12-19
- 📝 创建模块文档
- 📊 统计：约 50+ 个工具类