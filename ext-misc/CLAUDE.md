[根目录](../CLAUDE.md) > **ext-misc**

# ext-misc - 杂项工具层

> 提供 HTTP 客户端、二维码生成、系统监控等独立工具

## 层级职责

ext-misc 包含相对独立的工具模块，不属于特定业务领域，可按需引入。

## 子模块列表

| 模块 | 描述 | 主要依赖 |
|------|------|----------|
| ext-http | HTTP 客户端封装 | ext-tool |
| ext-useragent | User-Agent 解析器 | ext-tool |
| ext-qrcode | 二维码生成工具 | ext-tool, zxing |
| ext-monitor | 系统监控信息采集 | oshi-core |

## 模块详情

### ext-http

HTTP 客户端模块，提供：

- HTTP 请求封装
- 常用 HTTP 方法支持 (GET, POST, PUT, DELETE 等)
- 请求/响应处理
- 同步和异步请求支持

**包结构**: `ext.library.http`

### ext-useragent

User-Agent 解析模块，提供：

- 浏览器识别与版本解析
- 操作系统识别与版本解析
- 设备平台识别（移动/桌面/iOS/Android）
- 渲染引擎识别与版本解析

**包结构**: `ext.library.useragent`

**使用示例**:
```java
UserAgent ua = UserAgentParser.parse(userAgentString);
if (ua != null) {
    ua.getBrowser().getName();  // 浏览器名称
    ua.getOs().getName();       // 操作系统
    ua.isMobile();              // 是否移动设备
}
```

### ext-qrcode

二维码模块，基于 Google ZXing，提供：

- `QrCode` - 二维码生成工具类
- 支持自定义尺寸、颜色、Logo
- 多种输出格式 (PNG, JPG, Base64 等)

**包结构**: `ext.library.qrcode`

**使用示例**:
```java
// 生成二维码
byte[] qrImage = QrCode.from("https://example.com")
    .size(300)
    .toBytes();

// 生成 Base64（可直接用于 HTML img 标签）
String base64 = QrCode.from("内容").toBase64();

// 识别二维码
String content = QrCode.read(inputStream);
```

### ext-monitor

系统监控模块，基于 OSHI，提供：

- `SystemMonitor` - 监控管理器（Spring Bean）
- `CpuInfo` - CPU 信息采集
- `MemoryInfo` - 内存信息采集
- `DiskInfo` - 磁盘信息采集
- `JvmInfo` - JVM 信息采集
- `NetIoInfo` - 网络 IO 信息
- `HostInfo` - 主机信息

**包结构**: `ext.library.monitor`

**使用示例**:
```java
// 静态便捷方法（推荐）
CpuInfo cpu = CpuInfo.get();
MemoryInfo memory = MemoryInfo.get();
JvmInfo jvm = JvmInfo.get();
HostInfo host = HostInfo.get();
List<DiskInfo> disks = DiskInfo.getAll();
NetIoInfo net = NetIoInfo.get();

// 或注入 SystemMonitor Bean
@Autowired
private SystemMonitor monitor;

CpuInfo cpu = monitor.getCpuInfo();
```

## 依赖关系

```
ext-tool
    |
    +---> ext-http
    |
    +---> ext-useragent
    |
    +---> ext-qrcode (+ zxing)

oshi-core-java25
    |
    +---> ext-monitor
```

## 相关文件

- `ext-http/src/main/java/ext/library/http/` - HTTP 客户端源码
- `ext-useragent/src/main/java/ext/library/useragent/` - User-Agent 解析源码
- `ext-qrcode/src/main/java/ext/library/qrcode/` - 二维码源码
- `ext-monitor/src/main/java/ext/library/monitor/` - 系统监控源码

## 变更记录

| 日期 | 变更内容 |
|------|----------|
| 2026-01-26 | 重构 ext-qrcode：修复 API 命名(form→from)、NPE 问题、MIME 类型、添加参数验证 |
| 2026-01-26 | 重构 ext-monitor：重命名类、修复计算错误、添加静态便捷方法 |
| 2026-01-19 | 初始化 CLAUDE.md 文档 |
