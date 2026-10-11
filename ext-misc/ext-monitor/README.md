# ext-monitor

> 系统监控模块 - 提供系统资源信息采集功能

## 简介

`ext-monitor` 是 ext-library 的系统监控模块，基于 OSHI 库，提供 CPU、内存、磁盘、网络 IO、JVM 等系统资源信息采集功能，支持跨平台。

## 快速开始

### Maven

```xml
<dependency>
    <groupId>ext.library</groupId>
    <artifactId>ext-monitor</artifactId>
</dependency>
```

### Gradle

```groovy
implementation("ext.library:ext-monitor")
```

## 依赖说明

| 依赖 | 说明 |
|------|------|
| oshi-core-java25 | OSHI 系统信息库 (Java 25 版本) |

## 功能特性

- CPU 信息采集（使用率、核心数）
- 内存信息采集（总量、可用、已用、使用率）
- 磁盘信息采集（分区、容量、使用率）
- 网络 IO 信息（接收/发送字节、包数、速率）
- JVM 信息监控
- 主机信息采集
- 跨平台支持（Windows/Linux/macOS）
- **静态便捷方法**：无需注入即可直接使用
- **可配置采样时间**：CPU 和网络 IO 支持自定义采样时间

## 核心类说明

| 类名 | 说明 |
|------|------|
| `SystemMonitor` | 监控管理器（可作为 Spring Bean 注入） |
| `HostInfo` | 主机信息 |
| `CpuInfo` | CPU 信息 |
| `MemoryInfo` | 内存信息 |
| `DiskInfo` | 磁盘信息 |
| `NetIoInfo` | 网络 IO 信息 |
| `JvmInfo` | JVM 信息 |

## 使用示例

### 方式一：静态便捷方法（推荐）

无需 Spring 注入，直接使用静态方法：

```java
// 获取 CPU 信息（默认采样 500ms）
CpuInfo cpu = CpuInfo.get();
System.out.println("CPU 使用率: " + cpu.usePercent());

// 自定义采样时间（200ms）
CpuInfo cpuFast = CpuInfo.get(200);

// 获取内存信息
MemoryInfo memory = MemoryInfo.get();
System.out.println("内存使用率: " + memory.usePercent());

// 获取 JVM 信息
JvmInfo jvm = JvmInfo.get();
System.out.println("JDK 版本: " + jvm.jdkVersion());

// 获取主机信息
HostInfo host = HostInfo.get();
System.out.println("主机名: " + host.hostName());

// 获取磁盘信息
List<DiskInfo> disks = DiskInfo.getAll();
for (DiskInfo disk : disks) {
    System.out.println(disk.name() + ": " + disk.totalSpaceFormatted());
}

// 获取网络 IO（默认采样 1 秒）
NetIoInfo net = NetIoInfo.get();
System.out.println("下载速度: " + net.receiveKBPerSecond() + " KB/s");
```

### 方式二：Spring Bean 注入

```java
@RestController
@RequestMapping("/monitor")
public class MonitorController {

    @Autowired
    private SystemMonitor monitor;

    @GetMapping("/cpu")
    public CpuInfo getCpu() {
        return monitor.getCpuInfo();
    }

    @GetMapping("/memory")
    public MemoryInfo getMemory() {
        return monitor.getMemoryInfo();
    }

    @GetMapping("/disk")
    public List<DiskInfo> getDisk() {
        return monitor.getDiskInfos();
    }

    @GetMapping("/jvm")
    public JvmInfo getJvm() {
        return monitor.getJvmInfo();
    }

    @GetMapping("/host")
    public HostInfo getHost() {
        return monitor.getHostInfo();
    }

    @GetMapping("/net")
    public NetIoInfo getNetIo() {
        return monitor.getNetIoInfo();
    }
}
```

## API 速查

### 静态便捷方法

| 方法 | 描述 | 采样时间 |
|------|------|----------|
| `CpuInfo.get()` | 获取 CPU 信息 | 500ms |
| `CpuInfo.get(millis)` | 获取 CPU 信息（自定义采样） | 自定义 |
| `MemoryInfo.get()` | 获取内存信息 | 无 |
| `JvmInfo.get()` | 获取 JVM 信息 | 无 |
| `HostInfo.get()` | 获取主机信息 | 无 |
| `DiskInfo.getAll()` | 获取所有磁盘信息 | 无 |
| `NetIoInfo.get()` | 获取网络 IO 信息 | 1000ms |
| `NetIoInfo.get(millis)` | 获取网络 IO 信息（自定义采样） | 自定义 |

### SystemMonitor 方法

| 方法 | 描述 |
|------|------|
| `getHostInfo()` | 获取主机信息 |
| `getCpuInfo()` | 获取 CPU 信息（采样 500ms） |
| `getCpuInfo(millis)` | 获取 CPU 信息（自定义采样时间） |
| `getMemoryInfo()` | 获取内存信息 |
| `getJvmInfo()` | 获取 JVM 信息 |
| `getDiskInfos()` | 获取磁盘信息列表 |
| `getNetIoInfo()` | 获取网络 IO 信息（采样 1 秒） |
| `getNetIoInfo(millis)` | 获取网络 IO 信息（自定义采样时间） |

## 数据结构

### CpuInfo

| 属性 | 类型 | 描述 |
|------|------|------|
| `physicalProcessorCount` | int | 物理处理器数量 |
| `logicalProcessorCount` | int | 逻辑处理器数量 |
| `systemPercent` | double | 系统使用率（0-1） |
| `userPercent` | double | 用户使用率（0-1） |
| `waitPercent` | double | IO 等待率（0-1） |
| `usePercent` | double | 总使用率（0-1） |

### MemoryInfo

| 属性 | 类型 | 描述 |
|------|------|------|
| `total` | String | 总内存（如 "16GB"） |
| `used` | String | 已使用（如 "8GB"） |
| `free` | String | 空闲（如 "8GB"） |
| `usePercent` | double | 使用率（0-1） |

### DiskInfo

| 属性 | 类型 | 描述 |
|------|------|------|
| `name` | String | 分区名称 |
| `mount` | String | 挂载点 |
| `type` | String | 文件系统类型 |
| `totalSpace` | long | 总空间（字节） |
| `usableSpace` | long | 可用空间（字节） |
| `usePercent` | double | 使用率（0-1） |

**便捷方法**：
- `usedSpace()` - 已用空间（字节）
- `totalSpaceFormatted()` - 格式化的总空间
- `usableSpaceFormatted()` - 格式化的可用空间
- `usedSpaceFormatted()` - 格式化的已用空间

### NetIoInfo

| 属性 | 类型 | 描述 |
|------|------|------|
| `receivePacketsPerSecond` | long | 每秒接收包数 |
| `transmitPacketsPerSecond` | long | 每秒发送包数 |
| `receiveBytesPerSecond` | long | 每秒接收字节 |
| `transmitBytesPerSecond` | long | 每秒发送字节 |

**便捷方法**：
- `receiveKBPerSecond()` - 每秒接收 KB 数
- `transmitKBPerSecond()` - 每秒发送 KB 数

### JvmInfo

| 属性 | 类型 | 描述 |
|------|------|------|
| `jdkVersion` | String | JDK 版本 |
| `jdkHome` | String | JDK 安装目录 |
| `jdkName` | String | JVM 名称 |
| `jvmTotalMemory` | String | JVM 总内存 |
| `maxMemory` | String | 最大可用内存 |
| `freeMemory` | String | 空闲内存 |
| `usedMemory` | String | 已使用内存 |
| `usePercent` | double | 使用率（0-1） |
| `startTime` | long | 启动时间戳（毫秒） |
| `uptime` | long | 运行时长（毫秒） |

### HostInfo

| 属性 | 类型 | 描述 |
|------|------|------|
| `hostName` | String | 主机名 |
| `hostAddress` | String | IP 地址 |
| `osName` | String | 操作系统名称 |
| `osArch` | String | 系统架构 |
| `userDir` | String | 工作目录 |

## 注意事项

1. **采样时间**：CPU 和网络 IO 信息需要采样计算，默认采样时间分别为 500ms 和 1000ms。如需更快响应，可使用自定义采样时间，但精度会降低。

2. **百分比表示**：所有 `usePercent` 字段值范围为 0-1（如 0.75 表示 75%）。

3. **静态方法**：每个 Info 类的静态方法内部共享一个 SystemMonitor 实例，适合轻量级使用。在 Spring 环境中建议注入 SystemMonitor Bean 以获得更好的可测试性。

## 许可证

[Apache License 2.0](../../LICENSE)
