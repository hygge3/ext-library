[根目录](../CLAUDE.md) > **ext-monitor**

# ext-monitor 模块文档

## 模块职责

ext-monitor 提供系统监控功能，包括 CPU、内存、磁盘、网络等指标采集。

## 入口与启动

### 自动配置类
- **MonitorAutoConfig**: 监控自动配置

## 核心组件

### 1. 配置类 (config/)
- **MonitorAutoConfig**: 监控自动配置

### 2. 监控指标
- **CpuInfo**: CPU 信息
- **MemoryInfo**: 内存信息
- **JvmInfo**: JVM 信息
- **DiskInfo**: 磁盘信息
- **SysInfo**: 系统信息
- **NetIoInfo**: 网络 IO 信息

## 关键依赖

- **oshi-core-java25**: 系统信息采集库（JDK 25 版本）

## 使用示例

### 获取系统信息
```java
// CPU 信息
CpuInfo cpuInfo = new CpuInfo();
System.out.println("CPU 使用率: " + cpuInfo.getUsed());

// 内存信息
MemoryInfo memoryInfo = new MemoryInfo();
System.out.println("内存使用率: " + memoryInfo.getUsed());

// JVM 信息
JvmInfo jvmInfo = new JvmInfo();
System.out.println("JVM 堆内存: " + jvmInfo.getHeapMemory());

// 磁盘信息
DiskInfo diskInfo = new DiskInfo();
System.out.println("磁盘使用率: " + diskInfo.getUsed());
```

## 常见问题 (FAQ)

### Q: 支持哪些操作系统？
支持 Windows、Linux、macOS 等主流操作系统。

## 相关文件清单

### 主要源码文件
- `src/main/java/ext/library/monitor/MonitorAutoConfig.java`
- `src/main/java/ext/library/monitor/CpuInfo.java`
- `src/main/java/ext/library/monitor/MemoryInfo.java`
- `src/main/java/ext/library/monitor/JvmInfo.java`

### 配置文件
- `src/main/resources/META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`

## 变更记录 (Changelog)

### 2025-12-24
- 创建：模块文档
