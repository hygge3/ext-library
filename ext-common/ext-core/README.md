# ext 通用包

## 功能

1. spring 内部 async、线程池等通用配置
2. MapStruct 和 Cglib Bean copy 增强，支持链式 bean、Map、优化性能和支持类型转换。
3. 自定义服务异常。
4. 扩展工具包。

## 添加依赖

### maven

```xml

<dependency>
    <groupId>ext.library</groupId>
    <artifactId>ext-core</artifactId>
    <version>${version}</version>
</dependency>
```

### gradle

```groovy
compile("ext.library:ext-core:${version}")
```

## 工具类说明

### 常用工具类

| 类名            | 说明         |
|---------------|------------|
| SpelUtil      | EL 工具类     |
| AspectUtil    | 切面工具类      |
| BeanUtil      | bean 处理工具  |
| ClassUtil     | 类工具类       |
| SpringUtil    | Spring 工具类 |
| ServletUtil   | 客户端工具类     |
| ValidatorUtil | 校验框架工具     |
