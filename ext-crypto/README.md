# ext 请求解密响应加密

## 使用

### maven

```xml

<dependency>
    <groupId>ext.library</groupId>
    <artifactId>ext-encrypt</artifactId>
    <version>${version}</version>
</dependency>
```

### gradle

```groovy
compile("ext.library:ext-encrypt:${version}")
```

## 配置

| 配置项                   | 默认值 | 说明     |
|-----------------------|-----|--------|
| ext.crypto.public-key | -   | RSA 公钥 |
| ext.crypto.secret-key | -   | RSA 私钥 |

注意：必须填写公钥和私钥

## 使用文档

### 注解

```java
// 请求解密
@RequestDecrypt
// 响应加密
@ResponseEncrypt

// 注解到相应接口上
```