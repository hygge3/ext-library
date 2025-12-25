# ext-monitor（系统监控）

## 功能

- 系统资源监控
- CPU 信息采集
- 内存信息采集
- 磁盘信息采集
- 网络 IO 信息
- JVM 信息监控
- 跨平台支持（基于 OSHI）

## 依赖引用

### Maven

```xml
<dependency>
    <groupId>ext.library</groupId>
    <artifactId>ext-monitor</artifactId>
    <version>${version}</version>
</dependency>
```

### Gradle

```groovy
compile("ext.library:ext-monitor:${version}")
```

## 核心类说明

| 类名 | 说明 |
|-----|------|
| OshiMonitor | 监控管理器 |
| SysInfo | 系统信息 |
| CpuInfo | CPU 信息 |
| MemoryInfo | 内存信息 |
| DiskInfo | 磁盘信息 |
| NetIoInfo | 网络 IO 信息 |
| JvmInfo | JVM 信息 |

## 使用示例

### 获取系统信息

```java
@Autowired
private OshiMonitor monitor;

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

### 网络 IO

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
