# ext-misc

> 杂项工具层 - 提供 HTTP 客户端、二维码生成、系统监控等独立工具

## 简介

`ext-misc` 是 ext-library 的杂项工具层聚合模块，包含相对独立的工具模块，不属于特定业务领域，可按需引入。

## 子模块列表

| 模块 | 描述 | 主要依赖 |
|------|------|----------|
| [ext-http](./ext-http/README.md) | HTTP 客户端封装 | ext-tool |
| [ext-qrcode](./ext-qrcode/README.md) | 二维码生成工具 | ext-tool, ZXing |
| [ext-monitor](./ext-monitor/README.md) | 系统监控信息采集 | OSHI |

## 模块架构

```
ext-misc (杂项工具层)
├── ext-http      # HTTP 客户端
├── ext-qrcode    # 二维码生成
└── ext-monitor   # 系统监控
```

## 依赖关系

```
ext-tool
    │
    ├──► ext-http
    │
    └──► ext-qrcode (+ ZXing)

oshi-core-java25
    │
    └──► ext-monitor
```

## 快速开始

### 按需引入

```xml
<!-- HTTP 客户端 -->
<dependency>
    <groupId>ext.library</groupId>
    <artifactId>ext-http</artifactId>
</dependency>

<!-- 二维码生成 -->
<dependency>
    <groupId>ext.library</groupId>
    <artifactId>ext-qrcode</artifactId>
</dependency>

<!-- 系统监控 -->
<dependency>
    <groupId>ext.library</groupId>
    <artifactId>ext-monitor</artifactId>
</dependency>
```

## 功能概览

### ext-http

基于 JDK 11+ HttpClient 的 HTTP 客户端工具包，提供流畅的链式调用 API。

```java
String result = HttpUtil.get("https://api.example.com/data")
    .header("Authorization", "Bearer token")
    .query("page", "1")
    .execute()
    .asString();
```

### ext-qrcode

基于 Google ZXing 的二维码生成工具，支持自定义 Logo、颜色、尺寸。

```java
QrCode.form("https://example.com")
    .size(512)
    .logo("/path/to/logo.png")
    .toFile("/path/to/output.png");
```

### ext-monitor

基于 OSHI 的系统监控工具，提供 CPU、内存、磁盘、网络、JVM 等信息采集。

```java
CpuInfo cpuInfo = monitor.getCpuInfo();
MemoryInfo memoryInfo = monitor.getMemoryInfo();
List<DiskInfo> diskInfos = monitor.diskInfos();
```

## 许可证

[Apache License 2.0](../../LICENSE)
