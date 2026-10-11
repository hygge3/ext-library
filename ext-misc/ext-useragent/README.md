# ext-useragent

> User-Agent 解析模块 - 识别浏览器、操作系统、平台和渲染引擎

## 简介

`ext-useragent` 提供 User-Agent 字符串的解析功能，支持识别主流浏览器、操作系统、设备平台和渲染引擎。

## 快速开始

### Maven

```xml
<dependency>
    <groupId>ext.library</groupId>
    <artifactId>ext-useragent</artifactId>
</dependency>
```

### Gradle

```groovy
implementation("ext.library:ext-useragent")
```

## 依赖说明

| 依赖 | 说明 |
|------|------|
| ext-tool | 基础工具类 |

## 功能特性

- 浏览器识别：Chrome、Firefox、Safari、Edge、微信、钉钉等 30+ 浏览器
- 操作系统识别：Windows、macOS、Linux、Android、iOS 等
- 平台识别：移动设备、桌面设备、游戏机等
- 渲染引擎识别：Webkit、Gecko、Trident、Presto 等
- 版本解析：精确提取各组件版本号
- 移动设备判断：快速判断是否为移动设备

## 核心类说明

| 类名 | 说明 |
|------|------|
| `UserAgentParser` | 解析入口，提供 `parse()` 方法 |
| `UserAgent` | 解析结果，包含所有识别信息 |
| `Browser` | 浏览器信息 |
| `OS` | 操作系统信息 |
| `Platform` | 平台信息 |
| `Engine` | 渲染引擎信息 |

## 使用示例

### 基本解析

```java
String uaString = "Mozilla/5.0 (iPhone; CPU iPhone OS 14_0 like Mac OS X) AppleWebKit/605.1.15";
UserAgent ua = UserAgentParser.parse(uaString);

if (ua != null) {
    // 浏览器信息
    System.out.println("浏览器: " + ua.getBrowser().getName());
    System.out.println("浏览器版本: " + ua.getVersion());

    // 操作系统信息
    System.out.println("操作系统: " + ua.getOs().getName());
    System.out.println("系统版本: " + ua.getOsVersion());

    // 平台信息
    System.out.println("平台: " + ua.getPlatform().getName());
    System.out.println("是否移动设备: " + ua.isMobile());

    // 渲染引擎
    System.out.println("引擎: " + ua.getEngine().getName());
    System.out.println("引擎版本: " + ua.getEngineVersion());
}
```

### 在 Spring MVC 中使用

```java
@GetMapping("/info")
public Map<String, Object> getUserInfo(HttpServletRequest request) {
    String uaString = request.getHeader("User-Agent");
    UserAgent ua = UserAgentParser.parse(uaString);

    Map<String, Object> info = new HashMap<>();
    if (ua != null) {
        info.put("browser", ua.getBrowser().getName());
        info.put("os", ua.getOs().getName());
        info.put("mobile", ua.isMobile());
    }
    return info;
}
```

### 判断设备类型

```java
UserAgent ua = UserAgentParser.parse(uaString);

// 判断是否移动设备
if (ua.isMobile()) {
    // 移动端逻辑
}

// 判断具体平台
Platform platform = ua.getPlatform();
if (platform.isIos()) {
    // iOS 设备
} else if (platform.isAndroid()) {
    // Android 设备
}

// 判断特定浏览器
if (ua.getBrowser().isMobile()) {
    // 移动浏览器（微信、钉钉等）
}
```

## 支持的浏览器

| 类别 | 浏览器 |
|------|--------|
| 主流浏览器 | Chrome、Firefox、Safari、Edge、Opera |
| 国产浏览器 | QQ浏览器、UC浏览器、MIUI浏览器、Quark、百度 |
| 移动应用 | 微信、钉钉、支付宝、淘宝 |
| 其他 | IE、Konqueror、Thunderbird、Outlook 等 |

## 支持的操作系统

| 类别 | 系统 |
|------|------|
| Windows | Windows 10/8.1/8/7/Vista/XP/2003/2000、Windows Phone |
| Apple | macOS (OSX)、iOS (iPhone/iPad/iPod) |
| 移动 | Android |
| 其他 | Linux、Symbian、Wii、PlayStation |

## 注意事项

1. **空值处理**：`parse()` 方法对空字符串返回 null
2. **未知类型**：无法识别的组件返回 `UNKNOWN` 常量
3. **线程安全**：解析器和结果类都是线程安全的
4. **正则性能**：使用预编译的 Pattern，性能良好

## 许可证

[Apache License 2.0](../../LICENSE)
