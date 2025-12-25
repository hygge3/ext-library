[根目录](../CLAUDE.md) > **ext-http**

# ext-http 模块文档

## 模块职责

ext-http 提供 HTTP 客户端增强功能，包括 UserAgent 解析等。

## 入口与启动

该模块是工具类集合，无需自动配置。

## 核心组件

### 1. UserAgent 解析 (useragent/)
- **UserAgent**: UserAgent 解析器
- **UserAgentParser**: UserAgent 解析器实现
- **UserAgentInfo**: UserAgent 信息
- **Browser**: 浏览器信息
- **OS**: 操作系统信息
- **Engine**: 浏览器引擎信息
- **Platform**: 平台信息

## 关键依赖

- **ext-tool**: 基础工具类

## 使用示例

### 解析 UserAgent
```java
String userAgentString = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36";

UserAgentInfo info = UserAgentParser.parse(userAgentString);

System.out.println(info.getBrowser()); // Chrome
System.out.println(info.getOs()); // Windows
System.out.println(info.getEngine()); // WebKit
```

## 常见问题 (FAQ)

### Q: 支持哪些浏览器？
支持主流浏览器：Chrome、Firefox、Safari、Edge 等。

## 相关文件清单

### 主要源码文件
- `src/main/java/ext/library/http/useragent/UserAgent.java`
- `src/main/java/ext/library/http/useragent/UserAgentParser.java`
- `src/main/java/ext/library/http/useragent/UserAgentInfo.java`
- `src/main/java/ext/library/http/useragent/Browser.java`
- `src/main/java/ext/library/http/useragent/OS.java`

## 变更记录 (Changelog)

### 2025-12-24
- 创建：模块文档
