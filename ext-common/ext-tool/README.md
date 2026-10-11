# ext-tool

> 通用工具类库 - 提供基础工具函数、函数式接口、异常体系等核心能力

## 简介

`ext-tool` 是 ext-library 的最底层工具模块，无任何外部依赖，提供了丰富的工具类和函数式编程支持。

## 快速开始

### Maven

```xml
<dependency>
    <groupId>ext.library</groupId>
    <artifactId>ext-tool</artifactId>
</dependency>
```

### Gradle

```groovy
implementation("ext.library:ext-tool")
```

## 包结构

```
ext.library.tool
├── constant/     # 常量定义
├── runtime/      # 运行时工具类（系统、线程、运行时、日志）
├── domain/       # 领域对象（ID生成器、树构建器、版本号）
│   └── dict/     # 字典工具
├── exception/    # 异常体系
├── function/     # 受检函数式接口
├── holder/       # 容器类（延迟加载、单次执行）
│   └── retry/    # 重试机制
└── util/         # 工具类
```

## 功能模块

### 常量类 (constant)

| 类名 | 说明 |
|------|------|
| `EmojiSymbol` | Emoji 符号常量，用于日志模块标识 |
| `PatternPool` | 常用正则表达式预编译池 |
| `Singletons` | 常用单例对象持有器 |

### 运行时工具类 (runtime)

| 类名 | 说明 |
|------|------|
| `Threads` | 线程工具类（sleep、线程池关闭、堆栈信息获取） |
| `Exceptions` | 异常处理工具类（异常转换、解包、打印） |
| `Systems` | 系统信息工具类（操作系统判断、字符集、分隔符） |
| `Runtimes` | 运行时工具类（PID、启动时间、JVM 参数、CPU 核数） |
| `Logs` | 固定格式日志工具类（自动获取调用类，模块化日志输出） |
| `VirtualThreadPools` | 虚拟线程池（MDC 传递、异常处理） |

### 通用工具类 (util)

| 类名 | 说明 |
|------|------|
| `StringUtil` | 字符串工具类（判空、格式化、拼接、匹配） |
| `CollUtil` | 集合工具类 |
| `MapUtil` | Map 工具类 |
| `ObjectUtil` | 对象工具类 |
| `TypeCastUtil` | 类型转换工具类 |
| `DateUtil` | 日期时间工具类 |
| `CalcUtil` | 数学计算工具类（精确计算） |
| `IdUtil` | ID 生成工具类（UUID、UUIDv7、ObjectId、雪花ID） |
| `StreamUtil` | Stream 流工具类 |
| `IOUtil` | IO 工具类 |
| `NetUtil` | 网络工具类 |
| `Base64Util` | Base64 编解码工具类 |
| `HexUtil` | 十六进制工具类 |
| `ImageUtil` | 图片读取写出工具类 |
| `ClassUtil` | 类工具类 |
| `ReflectionUtil` | 反射工具类 |
| `ValidatorUtil` | 验证工具类 |
| `LatchUtil` | 并发闭锁工具类 |

### 容器类 (holder)

| 类名 | 说明 |
|------|------|
| `Lazy<T>` | 延迟加载容器（线程安全，首次调用时计算并缓存） |
| `Once` | 单次执行控制器（确保操作只执行一次） |
| `Unchecked` | Lambda 受检异常处理工具 |

### 受检函数式接口 (function)

| 接口 | 说明 |
|------|------|
| `CheckedFunction<T, R>` | 受检 Function（允许抛出异常） |
| `CheckedConsumer<T>` | 受检 Consumer |
| `CheckedSupplier<T>` | 受检 Supplier |
| `CheckedPredicate<T>` | 受检 Predicate |
| `CheckedRunnable` | 受检 Runnable |
| `CheckedCallable<T>` | 受检 Callable |
| `CheckedComparator<T>` | 受检 Comparator |

### 重试机制 (holder/retry)

| 类名 | 说明 |
|------|------|
| `Retry` | 重试接口 |
| `RetryCallback<T>` | 重试回调接口 |
| `SimpleRetry` | 简单重试实现 |

### 领域对象 (domain)

| 类名 | 说明 |
|------|------|
| `SnowflakeId` | Twitter 雪花算法 ID 生成器 |
| `MongoObjectId` | MongoDB 风格 ObjectId 生成器 |
| `TreeBuilder<T, ID>` | 通用树结构构建工具（支持循环依赖检测） |
| `Version` | 版本号比较工具 |

### 字典工具 (domain/dict)

| 类名 | 说明 |
|------|------|
| `Dict` | 字典接口（枚举字典标准结构） |
| `DictUtil` | 字典工具类（枚举转前端字典列表） |

### 异常体系 (exception)

| 类名 | 说明 |
|------|------|
| `ResponseCode` | 响应码接口 |
| `BizCode` | 业务错误码枚举（按模块分段 600-699） |
| `BizException` | 业务异常（携带错误码） |
| `ExtException` | 框架内部异常（自动添加模块标识） |
| `ToolException` | 工具异常 |

## 使用示例

### ID 生成

```java
// UUID（32位）
String uuid = IdUtil.getUUID();

// UUIDv7（时间有序，适合数据库主键）
String uuidv7 = IdUtil.getUUIDv7();

// 雪花ID
String snowflakeId = IdUtil.getSnowflakeId();

// MongoDB ObjectId（24位）
String objectId = IdUtil.getObjectId();

// 随机字符串
String random = IdUtil.random(16);
```

### 延迟加载

```java
Lazy<ExpensiveObject> lazy = Lazy.of(() -> new ExpensiveObject());
// 首次调用时才会创建对象
ExpensiveObject obj = lazy.get();
```

### 单次执行

```java
Once once = new Once();
// 只会执行一次
once.run(() -> initializeSystem());
once.run(() -> initializeSystem()); // 不会执行
```

### 受检异常处理

```java
// 将受检异常转换为非受检异常
list.stream()
    .map(Unchecked.function(item -> riskyOperation(item)))
    .toList();
```

### 树结构构建

```java
TreeBuilder<Menu, Long> builder = TreeBuilder.create(
    Menu::getId,
    Menu::getParentId,
    Menu::setChildren,
    Comparator.comparing(Menu::getSort)
);
List<Menu> tree = builder.buildTree(menuList);
```

### 版本号比较

```java
Version.of("1.9").lt("1.10");           // true（数字比较）
Version.of("v0.1").incomplete().eq("v0.1.2");  // true（不完整模式）
```

### 重试机制

```java
String result = SimpleRetry.of(3, 1000)
    .execute(() -> callRemoteService());
```

## 错误码规范

业务错误码采用 6xx 段，按模块分类：

| 范围 | 模块 |
|------|------|
| 600-609 | 通用错误（参数、未知异常等） |
| 610-619 | 工具模块 |
| 620-629 | 数据库模块 |
| 630-639 | 缓存模块（Redis） |
| 640-649 | 安全/加密模块 |
| 650-659 | 业务逻辑模块 |
| 660-669 | 外部服务模块（邮件、第三方调用） |
| 670-679 | 日志/监控模块 |
| 690-699 | 保留/未实现 |

## 许可证

[Apache License 2.0](../../LICENSE)
