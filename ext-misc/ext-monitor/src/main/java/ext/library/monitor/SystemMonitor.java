package ext.library.monitor;

import oshi.ffm.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.GlobalMemory;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.hardware.NetworkIF;
import oshi.software.os.FileSystem;
import oshi.software.os.OSFileStore;
import oshi.software.os.OperatingSystem;
import oshi.util.Util;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * 系统监控器
 * <p>
 * 基于 OSHI 库，提供 CPU、内存、磁盘、网络 IO、JVM 等系统资源信息采集功能。
 * </p>
 */
public record SystemMonitor(SystemInfo systemInfo) {

    public static final SystemMonitor INSTANCE = new SystemMonitor();

    private static final int defaultCpuSampleMillis = 500;
    private static final int defaultNetSampleMillis = 1000;

    public SystemMonitor() {
        this(new SystemInfo());
    }

    /**
     * 获取底层 SystemInfo 对象
     */
    @Override
    public SystemInfo systemInfo() {
        return systemInfo;
    }

    /**
     * 获取硬件抽象层
     */
    public HardwareAbstractionLayer getHardware() {
        return systemInfo.getHardware();
    }

    /**
     * 获取操作系统信息
     */
    public OperatingSystem getOperatingSystem() {
        return systemInfo.getOperatingSystem();
    }

    /**
     * 获取中央处理器
     */
    public CentralProcessor getProcessor() {
        return getHardware().getProcessor();
    }

    // ==================== 主机信息 ====================

    /**
     * 获取主机信息
     *
     * @return {@link HostInfo}
     */
    public HostInfo getHostInfo() {
        Properties props = System.getProperties();
        String hostName, hostAddress;
        try {
            InetAddress inetAddress = InetAddress.getLocalHost();
            hostName = inetAddress.getHostName();
            hostAddress = inetAddress.getHostAddress();
        } catch (UnknownHostException e) {
            hostName = "unknown";
            hostAddress = "unknown";
        }
        return new HostInfo(
                hostName,
                hostAddress,
                props.getProperty("os.name"),
                props.getProperty("os.arch"),
                props.getProperty("user.dir")
        );
    }

    // ==================== CPU 信息 ====================

    /**
     * 获取 CPU 信息（默认采样 500ms）
     *
     * @return {@link CpuInfo}
     */
    public CpuInfo getCpuInfo() {
        return getCpuInfo(defaultCpuSampleMillis);
    }

    /**
     * 获取 CPU 信息
     *
     * @param sampleMillis 采样时间（毫秒）
     *
     * @return {@link CpuInfo}
     */
    public CpuInfo getCpuInfo(int sampleMillis) {
        CentralProcessor processor = getProcessor();
        long[] prevTicks = processor.getSystemCpuLoadTicks();
        Util.sleep(sampleMillis);
        long[] ticks = processor.getSystemCpuLoadTicks();

        long nice = ticks[CentralProcessor.TickType.NICE.getIndex()] - prevTicks[CentralProcessor.TickType.NICE.getIndex()];
        long irq = ticks[CentralProcessor.TickType.IRQ.getIndex()] - prevTicks[CentralProcessor.TickType.IRQ.getIndex()];
        long softirq = ticks[CentralProcessor.TickType.SOFTIRQ.getIndex()] - prevTicks[CentralProcessor.TickType.SOFTIRQ.getIndex()];
        long steal = ticks[CentralProcessor.TickType.STEAL.getIndex()] - prevTicks[CentralProcessor.TickType.STEAL.getIndex()];
        long sys = ticks[CentralProcessor.TickType.SYSTEM.getIndex()] - prevTicks[CentralProcessor.TickType.SYSTEM.getIndex()];
        long user = ticks[CentralProcessor.TickType.USER.getIndex()] - prevTicks[CentralProcessor.TickType.USER.getIndex()];
        long ioWait = ticks[CentralProcessor.TickType.IOWAIT.getIndex()] - prevTicks[CentralProcessor.TickType.IOWAIT.getIndex()];
        long idle = ticks[CentralProcessor.TickType.IDLE.getIndex()] - prevTicks[CentralProcessor.TickType.IDLE.getIndex()];
        long total = user + nice + sys + idle + ioWait + irq + softirq + steal;

        // 处理采样时间过短导致总时间为 0 的情况
        if (total == 0) {
            return new CpuInfo(
                    processor.getPhysicalProcessorCount(),
                    processor.getLogicalProcessorCount(),
                    0, 0, 0, 0
            );
        }

        return new CpuInfo(
                processor.getPhysicalProcessorCount(),
                processor.getLogicalProcessorCount(),
                FormatUtil.round(sys * 1.0 / total),
                FormatUtil.round(user * 1.0 / total),
                FormatUtil.round(ioWait * 1.0 / total),
                FormatUtil.round(1.0 - (idle * 1.0 / total))
        );
    }

    // ==================== 内存信息 ====================

    /**
     * 获取内存信息
     *
     * @return {@link MemoryInfo}
     */
    public MemoryInfo getMemoryInfo() {
        GlobalMemory memory = getHardware().getMemory();
        long total = memory.getTotal();
        long available = memory.getAvailable();
        long used = total - available;
        return new MemoryInfo(
                FormatUtil.formatBytes(total),
                FormatUtil.formatBytes(used),
                FormatUtil.formatBytes(available),
                FormatUtil.round(used * 1.0 / total)
        );
    }

    // ==================== JVM 信息 ====================

    /**
     * 获取 JVM 信息
     *
     * @return {@link JvmInfo}
     */
    public JvmInfo getJvmInfo() {
        Properties props = System.getProperties();
        RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
        Runtime runtime = Runtime.getRuntime();

        long total = runtime.totalMemory();
        long free = runtime.freeMemory();
        long used = total - free;

        return new JvmInfo(
                props.getProperty("java.version"),
                props.getProperty("java.home"),
                runtimeMXBean.getVmName(),
                FormatUtil.formatBytes(total),
                FormatUtil.formatBytes(runtime.maxMemory()),
                FormatUtil.formatBytes(free),
                FormatUtil.formatBytes(used),
                FormatUtil.round(used * 1.0 / total),
                runtimeMXBean.getStartTime(),
                runtimeMXBean.getUptime()
        );
    }

    // ==================== 磁盘信息 ====================

    /**
     * 获取磁盘信息列表
     *
     * @return 磁盘信息列表
     */
    public List<DiskInfo> getDiskInfos() {
        FileSystem fileSystem = getOperatingSystem().getFileSystem();
        List<DiskInfo> diskInfos = new ArrayList<>();

        for (OSFileStore fs : fileSystem.getFileStores()) {
            long total = fs.getTotalSpace();
            long usable = fs.getUsableSpace();
            long used = total - usable;
            double usePercent = total > 0 ? FormatUtil.round(used * 1.0 / total) : 0;

            diskInfos.add(new DiskInfo(
                    fs.getName(),
                    fs.getMount(),
                    fs.getType(),
                    total,
                    usable,
                    usePercent
            ));
        }
        return diskInfos;
    }

    // ==================== 网络 IO 信息 ====================

    /**
     * 获取网络 IO 信息（默认采样 1 秒）
     *
     * @return {@link NetIoInfo}
     */
    public NetIoInfo getNetIoInfo() {
        return getNetIoInfo(defaultNetSampleMillis);
    }

    /**
     * 获取网络 IO 信息
     *
     * @param sampleMillis 采样时间（毫秒）
     *
     * @return {@link NetIoInfo}
     */
    public NetIoInfo getNetIoInfo(int sampleMillis) {
        HardwareAbstractionLayer hal = getHardware();

        // 采样开始
        long rxBytesBegin = 0, txBytesBegin = 0, rxPacketsBegin = 0, txPacketsBegin = 0;
        for (NetworkIF net : hal.getNetworkIFs()) {
            rxBytesBegin += net.getBytesRecv();
            txBytesBegin += net.getBytesSent();
            rxPacketsBegin += net.getPacketsRecv();
            txPacketsBegin += net.getPacketsSent();
        }

        Util.sleep(sampleMillis);

        // 采样结束
        long rxBytesEnd = 0, txBytesEnd = 0, rxPacketsEnd = 0, txPacketsEnd = 0;
        for (NetworkIF net : hal.getNetworkIFs()) {
            rxBytesEnd += net.getBytesRecv();
            txBytesEnd += net.getBytesSent();
            rxPacketsEnd += net.getPacketsRecv();
            txPacketsEnd += net.getPacketsSent();
        }

        // 计算每秒速率
        double seconds = sampleMillis / 1000.0;
        long rxBytesPerSec = (long) ((rxBytesEnd - rxBytesBegin) / seconds);
        long txBytesPerSec = (long) ((txBytesEnd - txBytesBegin) / seconds);
        long rxPacketsPerSec = (long) ((rxPacketsEnd - rxPacketsBegin) / seconds);
        long txPacketsPerSec = (long) ((txPacketsEnd - txPacketsBegin) / seconds);

        return new NetIoInfo(rxPacketsPerSec, txPacketsPerSec, rxBytesPerSec, txBytesPerSec);
    }
}