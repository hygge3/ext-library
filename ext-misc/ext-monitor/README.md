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

- CPU 信息采集 (使用率、核心数、型号)
- 内存信息采集 (总量、可用、已用、使用率)
- 磁盘信息采集 (分区、容量、使用率)
- 网络 IO 信息 (接收/发送字节、速度)
- JVM 信息监控
- 系统综合信息
- 跨平台支持 (Windows/Linux/macOS)

## 核心类说明

| 类名 | 说明 |
|------|------|
| `OshiMonitor` | 监控管理器，Spring Bean |
| `SysInfo` | 系统综合信息 |
| `CpuInfo` | CPU 信息 |
| `MemoryInfo` | 内存信息 |
| `DiskInfo` | 磁盘信息 |
| `NetIoInfo` | 网络 IO 信息 |
| `JvmInfo` | JVM 信息 |

## 使用示例

### 注入监控器

```java
@Autowired
private OshiMonitor monitor;
```

### 获取系统信息

```java
public SysInfo getSystemInfo() {
    return monitor.getSysInfo();
}
```

### CPU 信息

```java
public CpuInfo getCpuInfo() {
    CpuInfo cpuInfo = new CpuInfo();
    cpuInfo.setCpuLoad(monitor.cpuLoad());           // CPU 使用率
    cpuInfo.setCpuCount(monitor.processorCount());   // CPU 核心数
    cpuInfo.setCpuModel(monitor.processorModel());   // CPU 型号
    return cpuInfo;
}
```

### 内存信息

```java
public MemoryInfo getMemoryInfo() {
    MemoryInfo memoryInfo = new MemoryInfo();
    memoryInfo.setTotalMemory(monitor.totalMemory());        // 总内存
    memoryInfo.setAvailableMemory(monitor.availableMemory()); // 可用内存
    memoryInfo.setUsedMemory(monitor.usedMemory());          // 已用内存
    memoryInfo.setUsedPercent(monitor.memoryUsedPercent());  // 使用率
    return memoryInfo;
}
```

### 磁盘信息

```java
public List<DiskInfo> getDiskInfo() {
    return monitor.diskInfos();
}
```

### 网络 IO 信息

```java
public NetIoInfo getNetIoInfo() {
    NetIoInfo netIoInfo = new NetIoInfo();
    netIoInfo.setRxBytes(monitor.receiveBytes());      // 接收字节
    netIoInfo.setTxBytes(monitor.transmitBytes());     // 发送字节
    netIoInfo.setRxSpeed(monitor.receiveSpeed());      // 下载速度
    netIoInfo.setTxSpeed(monitor.transmitSpeed());     // 上传速度
    return netIoInfo;
}
```

### JVM 信息

```java
public JvmInfo getJvmInfo() {
    JvmInfo jvmInfo = new JvmInfo();
    jvmInfo.setVersion(System.getProperty("java.version"));
    jvmInfo.setVmName(System.getProperty("java.vm.name"));
    jvmInfo.setTotalMemory(Runtime.getRuntime().totalMemory());
    jvmInfo.setFreeMemory(Runtime.getRuntime().freeMemory());
    jvmInfo.setMaxMemory(Runtime.getRuntime().maxMemory());
    return jvmInfo;
}
```

### 完整监控数据

```java
public Map<String, Object> getAllMetrics() {
    Map<String, Object> metrics = new HashMap<>();
    metrics.put("cpu", getCpuInfo());
    metrics.put("memory", getMemoryInfo());
    metrics.put("disk", getDiskInfo());
    metrics.put("jvm", getJvmInfo());
    metrics.put("netIo", getNetIoInfo());
    return metrics;
}
```

### 监控 API 示例

```java
@RestController
@RequestMapping("/monitor")
public class MonitorController {

    @Autowired
    private OshiMonitor monitor;

    @GetMapping("/cpu")
    public CpuInfo getCpu() {
        CpuInfo cpuInfo = new CpuInfo();
        cpuInfo.setCpuLoad(monitor.cpuLoad());
        cpuInfo.setCpuCount(monitor.processorCount());
        cpuInfo.setCpuModel(monitor.processorModel());
        return cpuInfo;
    }

    @GetMapping("/memory")
    public MemoryInfo getMemory() {
        MemoryInfo memoryInfo = new MemoryInfo();
        memoryInfo.setTotalMemory(monitor.totalMemory());
        memoryInfo.setAvailableMemory(monitor.availableMemory());
        memoryInfo.setUsedMemory(monitor.usedMemory());
        memoryInfo.setUsedPercent(monitor.memoryUsedPercent());
        return memoryInfo;
    }

    @GetMapping("/disk")
    public List<DiskInfo> getDisk() {
        return monitor.diskInfos();
    }

    @GetMapping("/all")
    public SysInfo getAll() {
        return monitor.getSysInfo();
    }
}
```

## API 速查

### OshiMonitor 方法

| 方法 | 描述 |
|------|------|
| `getSysInfo()` | 获取系统综合信息 |
| `cpuLoad()` | 获取 CPU 使用率 |
| `processorCount()` | 获取 CPU 核心数 |
| `processorModel()` | 获取 CPU 型号 |
| `totalMemory()` | 获取总内存 |
| `availableMemory()` | 获取可用内存 |
| `usedMemory()` | 获取已用内存 |
| `memoryUsedPercent()` | 获取内存使用率 |
| `diskInfos()` | 获取磁盘信息列表 |
| `receiveBytes()` | 获取网络接收字节 |
| `transmitBytes()` | 获取网络发送字节 |
| `receiveSpeed()` | 获取下载速度 |
| `transmitSpeed()` | 获取上传速度 |

## 信息类属性

### CpuInfo

| 属性 | 描述 |
|------|------|
| cpuLoad | CPU 使用率 |
| cpuCount | CPU 核心数 |
| cpuModel | CPU 型号 |

### MemoryInfo

| 属性 | 描述 |
|------|------|
| totalMemory | 总内存 |
| availableMemory | 可用内存 |
| usedMemory | 已用内存 |
| usedPercent | 使用率 |

### DiskInfo

| 属性 | 描述 |
|------|------|
| name | 分区名称 |
| totalSpace | 总容量 |
| usableSpace | 可用容量 |
| usedSpace | 已用容量 |
| usedPercent | 使用率 |

### NetIoInfo

| 属性 | 描述 |
|------|------|
| rxBytes | 接收字节 |
| txBytes | 发送字节 |
| rxSpeed | 下载速度 |
| txSpeed | 上传速度 |

### JvmInfo

| 属性 | 描述 |
|------|------|
| version | Java 版本 |
| vmName | JVM 名称 |
| totalMemory | 堆内存总量 |
| freeMemory | 空闲堆内存 |
| maxMemory | 最大堆内存 |

## 许可证

[Apache License 2.0](../../LICENSE)
