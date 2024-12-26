# ext 工具包

## 功能

1. 常用工具包
2. 综合工具类

## 添加依赖

### maven

```xml

<dependency>
    <groupId>ext.library</groupId>
    <artifactId>ext-tool</artifactId>
    <version>${version}</version>
</dependency>
```

### gradle

```groovy
compile("ext.library:ext-tool:${version}")
```

## 工具类说明

### 常量池

| 类名          | 说明      |
|-------------|---------|
| Symbol      | 符号常量    |
| PatternPool | 常用正则表达式 |

### 常用工具类

| 类名           | 说明          |
|--------------|-------------|
| ArithCompute | 数学计算工具类     |
| BooleanUtils | Boolean 工具类 |
| Converter    | 简单数据类型转换工具类 |
| DateUtil     | 时间处理工具      |
| ImageUtils   | 图片读取写出工具    |
| MapUtils     | Map 处理工具    |
| StreamUtils  | Stream 流工具  |
| TreeUtils    | 树形菜单构建工具    |
| XmlUtils     | xml 解析工具类   |

### 签名加密工具类

| 类名      | 说明  |
|---------|-----|
| AESUtil | Aes |
| RSAUtil | Rsa |

### 线程、异常等工具类

| 类名          | 说明           |
|-------------|--------------|
| Threads     | 线程工具类        |
| ThreadPools | 线程池工具类       |
| Exceptions  | 异常处理工具类      |
| Unchecked   | lambda 异常包装类 |

### 其他工具类

| 类名        | 说明         |
|-----------|------------|
| CountMap  | 计数器        |
| Lazy      | 延迟加载       |
| Once      | 一次加载       |
| Holder    | 部分常量       |
| Version   | Version 工具 |
| INetUtils | 网络工具类      |
| Runtimes  | 系统运行时工具类   |

## Google Guava 说明

### 包说明

| 包名                                | 说明                                          |
|-----------------------------------|---------------------------------------------|
| com.google.common.annotations     | 普通注解类型                                      |
| com.google.common.base            | 基本工具类库和接口                                   |
| com.google.common.cache           | 缓存工具包，非常简单易用且功能强大的 JVM 内缓存                  |
| com.google.common.collect         | 带泛型的集合接口扩展和实现，以及工具类                         |
| com.google.common.eventbus        | 发布订阅风格的事件总线                                 |
| com.google.common.graph           | 对“图”数据结构的支持                                 |
| com.google.common.hash            | 哈希工具包                                       |
| com.google.common.io              | I/O工具包                                      |
| com.google.common.math            | 原始算术类型和超大数的运算工具包                            |
| com.google.common.net             | 网络工具包                                       |
| com.google.common.primitives      | 八种原始类型和无符号类型的静态工具包                          |
| com.google.common.reflect         | 反射工具包                                       |
| com.google.common.util.concurrent | 多线程工具包                                      |
| com.google.common.escape          | 提供了对字符串内容中特殊字符进行替换的框架，并包括了 Xml 和 Html 的两个实现 |
| com.google.common.html            | HtmlEscapers 封装了对 html 中特殊字符的替换             |
| com.google.common.xml             | XmlEscapers 封装了对 xml 中特殊字符的替换               |

### 基本工具 [Basic utilities]

| 类名            | 说明            |
|---------------|---------------|
| Optional      | 使用和避免 null    |
| Preconditions | 前置条件          |
| Objects       | 常见 Object 方法  |
| Ordering      | 排序器           |
| Throwables    | 简化异常和错误的传播与检查 |

### 集合 [Collections]

| 类名                                                                       | 说明    |
|--------------------------------------------------------------------------|-------|
| ImmutableXxx                                                             | 不可变集合 |
| Multiset,Multimap,BiMap,Table,ClassToInstanceMap,RangeSet,RangeMap       | 新集合类型 |
| Iterables,Collections2,Lists,Sets,Maps,Queues,Multisets,Multimaps,Tables | 集合工具类 |
| Forwarding,PeekingIterator,AbstractIterator,AbstractSequentialIterator   | 扩展工具类 |

### 缓存 [Caches]

| 类名           | 说明               |
|--------------|------------------|
| CacheBuilder | 缓存构建器，支持多种缓存过期策略 |

### 函数式风格 [Functional idioms]

| 类名        | 说明                           |
|-----------|------------------------------|
| Functions | Guava 的函数式支持可以显著简化代码，但请谨慎使用它 |
| Suppliers | Guava 的函数式支持可以显著简化代码，但请谨慎使用它 |

### 并发 [Concurrency]

| 类名               | 说明                                |
|------------------|-----------------------------------|
| ListenableFuture | 完成后触发回调的 Future，可以进行一系列的复杂链式的异步操作 |
| Service          | 抽象可开启和关闭的服务，帮助你维护服务的状态逻辑          |

### 字符串处理 [Strings]

| 类名          | 说明    |
|-------------|-------|
| Strings     | 字符串工具 |
| Joiner      | 连接器   |
| Splitter    | 拆分器   |
| CharMatcher | 字符匹配器 |
| Charsets    | 字符集   |
| CaseFormat  | 大小写格式 |

### 区间 [Ranges]

| 类名       | 说明                     |
|----------|------------------------|
| Range<C> | 可比较类型的区间 API，包括连续和离散类型 |

### I/O

| 类名          | 说明    |
|-------------|-------|
| Files       | 文件工具  |
| ByteStreams | 字节流工具 |
| CharStreams | 字符流工具 |

### 散列 [Hash]

| 类名          | 说明                 |
|-------------|--------------------|
| BloomFilter | 布鲁姆过滤器             |
| Hashing     | 若干散列函数及运算 HashCode |

### 事件总线 [EventBus]

| 类名       | 说明                               |
|----------|----------------------------------|
| EventBus | 发布 - 订阅模式的组件通信，但组件不需要显式地注册到其他组件中 |

### 数学运算 [Math]

| 类名                              | 说明    |
|---------------------------------|-------|
| IntMath,LongMath,BigIntegerMath | 整数运算  |
| DoubleMath                      | 浮点数运算 |

### 反射 [Reflection]

| 类名         | 说明                                |
|------------|-----------------------------------|
| TypeToken  | 使用了基于反射的技巧甚至让你在运行时都能够巧妙的操作和查询泛型类型 |
| Invokable  | 简化了常见的反射代码的使用                     |
| Reflection | 反射工具类                             |
| ClassPath  | 提供类路径扫描                           |

## 其他工具推荐

| 名称                                           | 描述                                                 | 
|----------------------------------------------|----------------------------------------------------|
| [FastExcel](https://idev.cn/fastexcel/zh-CN) | FastExcel 是一个基于 Java 的、快速、简洁、解决大文件内存溢出的 Excel 处理工具 |    
| [Forest](https://forest.dtflyx.com/)         | 声明式与编程式双修，让天下没有难以发送的 HTTP 请求                       |    
| [LiteFlow](https://liteflow.cc/)             | 轻量，快速，稳定可编排的组件式规则引擎                                |
| [Easy-Es](https://www.easy-es.cn/)           | 傻瓜级 ElasticSearch 搜索引擎 ORM 框架                      |
