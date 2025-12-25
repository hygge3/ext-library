[根目录](../CLAUDE.md) > **ext-interface-crypto**

# ext-interface-crypto 模块文档

## 模块职责

ext-interface-crypto 提供接口请求/响应加密功能，通过注解方式实现数据加密传输。

## 入口与启动

该模块通过注解和切面实现，无需显式配置。

## 核心组件

### 1. 注解 (annotation/)
- **@RequestDecrypt**: 请求解密注解
- **@ResponseEncrypt**: 响应加密注解

### 2. 切面 (aspect/)
- **RequestDecryptAspect**: 请求解密切面
- **ResponseEncryptAspect**: 响应加密切面

### 3. 处理器 (handler/)
- **CryptoHandler**: 加密处理器接口
- **DefaultCryptoHandler**: 默认加密处理器

## 关键依赖

- **ext-json**: JSON 处理支持
- **ext-crypto**: 加密算法支持
- **spring-webmvc**: Web MVC 支持
- **jakarta.servlet-api**: Servlet API 支持

## 使用示例

### 接口加密
```java
@RestController
@RequestMapping("/api")
public class UserController {

    @RequestDecrypt
    @ResponseEncrypt
    @PostMapping("/user/save")
    public R<User> saveUser(@RequestBody User user) {
        // 请求自动解密，响应自动加密
        return R.ok(userService.save(user));
    }
}
```

## 常见问题 (FAQ)

### Q: 如何自定义加密算法？
实现 `CryptoHandler` 接口并注册为 Bean。

## 相关文件清单

### 主要源码文件
- `src/main/java/ext/library/interface/crypto/annotation/RequestDecrypt.java`
- `src/main/java/ext/library/interface/crypto/annotation/ResponseEncrypt.java`

## 变更记录 (Changelog)

### 2025-12-24
- 创建：模块文档
