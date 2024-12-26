# ext 脱敏工具

## 功能和特点

为防止隐私或敏感数据的泄露，项目开发中经常需要对特定的数据进行脱敏处理

## 使用

### maven

```xml

<dependency>
    <groupId>ext.library</groupId>
    <artifactId>ext-desensitize</artifactId>
    <version>${version}</version>
</dependency>
```

### gradle

```groovy
compile("ext.library:ext-desensitize:${version}")
```

## 使用文档

### 脱敏注解

在 web 服务中，服务端的响应数据很多情况下都是 json 数据，目前 json 处理基于 Jackson，因为 springMvc 默认的 json 处理是使用
jackson

示例：

```java

@Sensitive(strategy = SensitiveStrategy.PHONE)
private String phone;

```