package ext.library.monitor;

import lombok.AllArgsConstructor;
import lombok.Getter;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.ComputerSystem;
import oshi.hardware.GlobalMemory;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.hardware.NetworkIF;
import oshi.software.os.FileSystem;
import oshi.software.os.OSFileStore;
import oshi.software.os.OperatingSystem;
import oshi.util.Util;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * 基于 OShi 服务器信息收集监控
 */
@Getter
@AllArgsConstructor
public class OshiMonitor {

    /**
     * 获取系统信息
     */
    SystemInfo systemInfo;

    /**
     * 获取硬件抽象层信息
     *
     * @return {@link HardwareAbstractionLayer}
     */
    public HardwareAbstractionLayer getHardwareAbstractionLayer() {
        return getSystemInfo().getHardware();
    }

    /**
     * 操作系统
     *
     * @return {@link OperatingSystem}
     */
    public OperatingSystem getOperatingSystem() {
        return getSystemInfo().getOperatingSystem();
    }

    /**
     * 中央处理器
     *
     * @return {@link CentralProcessor}
     */
    public CentralProcessor getCentralProcessor() {
        return getHardwareAbstractionLayer().getProcessor();
    }

    /**
     * 获取操作系统信息 <code>
     * System.out.println("manufacturer: " + computerSystem.getManufacturer());
     * System.out.println("model: " + computerSystem.getModel());
     * System.out.println("serialnumber: " + computerSystem.getSerialNumber());
     * final Firmware firmware = computerSystem.getFirmware();
     * System.out.println("firmware:");
     * System.out.println("  manufacturer: " + firmware.getManufacturer());
     * System.out.println("  name: " + firmware.getName());
     * System.out.println("  description: " + firmware.getDescription());
     * System.out.println("  version: " + firmware.getVersion());
     * final Baseboard baseboard = computerSystem.getBaseboard();
     * System.out.println("baseboard:");
     * System.out.println("  manufacturer: " + baseboard.getManufacturer());
     * System.out.println("  model: " + baseboard.getModel());
     * System.out.println("  version: " + baseboard.getVersion());
     * System.out.println("  serialnumber: " + baseboard.getSerialNumber());
     * </code>
     *
     * @return {@link ComputerSystem}
     */
    public ComputerSystem getComputerSystem() {
        return getHardwareAbstractionLayer().getComputerSystem();
    }

    public InetAddress getInetAddress() throws UnknownHostException {
        return InetAddress.getLocalHost();
    }

    public Properties getSystemProperties() {
        return System.getProperties();
    }

    /**
     * 获取系统信息
     *
     * @return {@link SysInfo}
     */
    public SysInfo getSysInfo() {
        Properties props = getSystemProperties();
        String name, ip;
        try {
            InetAddress inetAddress = getInetAddress();
            name = inetAddress.getHostName();
            ip = inetAddress.getHostAddress();
        } catch (UnknownHostException e) {
            name = "unknown";
            ip = "unknown";
        }
        return new SysInfo(name, ip, props.getProperty("os.name"), props.getProperty("os.arch"), props.getProperty("user.dir"));
    }

    /**
     * 获取 cpu 信息
     *
     * @return {@link CpuInfo}
     */
    public CpuInfo getCpuInfo() {
        CentralProcessor centralProcessor = getCentralProcessor();
        long[] prevTicks = centralProcessor.getSystemCpuLoadTicks();
        Util.sleep(600);
        long[] ticks = centralProcessor.getSystemCpuLoadTicks();
        long nice = ticks[CentralProcessor.TickType.NICE.getIndex()] - prevTicks[CentralProcessor.TickType.NICE.getIndex()];
        long irq = ticks[CentralProcessor.TickType.IRQ.getIndex()] - prevTicks[CentralProcessor.TickType.IRQ.getIndex()];
        long softirq = ticks[CentralProcessor.TickType.SOFTIRQ.getIndex()] - prevTicks[CentralProcessor.TickType.SOFTIRQ.getIndex()];
        long steal = ticks[CentralProcessor.TickType.STEAL.getIndex()] - prevTicks[CentralProcessor.TickType.STEAL.getIndex()];
        long sys = ticks[CentralProcessor.TickType.SYSTEM.getIndex()] - prevTicks[CentralProcessor.TickType.SYSTEM.getIndex()];
        long user = ticks[CentralProcessor.TickType.USER.getIndex()] - prevTicks[CentralProcessor.TickType.USER.getIndex()];
        long ioWait = ticks[CentralProcessor.TickType.IOWAIT.getIndex()] - prevTicks[CentralProcessor.TickType.IOWAIT.getIndex()];
        long idle = ticks[CentralProcessor.TickType.IDLE.getIndex()] - prevTicks[CentralProcessor.TickType.IDLE.getIndex()];
        long totalCpu = user + nice + sys + idle + ioWait + irq + softirq + steal;
        return new CpuInfo(centralProcessor.getPhysicalProcessorCount(), centralProcessor.getLogicalProcessorCount(), formatDouble(sys * 1.0 / totalCpu), formatDouble(user * 1.0 / totalCpu), formatDouble(ioWait * 1.0 / totalCpu), formatDouble(1.0 - (idle * 1.0 / totalCpu)));
    }

    /**
     * 获取内存使用信息
     *
     * @return {@link MemoryInfo}
     */
    public MemoryInfo getMemoryInfo() {
        GlobalMemory globalMemory = getHardwareAbstractionLayer().getMemory();
        long totalByte = globalMemory.getTotal();
        long availableByte = globalMemory.getAvailable();
        return new MemoryInfo(formatByte(totalByte), formatByte(totalByte - availableByte), formatByte(availableByte), formatDouble((totalByte - availableByte) * 1.0 / totalByte));
    }

    /**
     * 获取 JVM 信息
     *
     * @return {@link JvmInfo}
     */
    public JvmInfo getJvmInfo() {
        Properties props = getSystemProperties();
        RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
        Runtime runtime = Runtime.getRuntime();
        long jvmTotalMemoryByte = runtime.totalMemory();
        long freeMemoryByte = runtime.freeMemory();
        return new JvmInfo(props.getProperty("java.version"), props.getProperty("java.home"), runtimeMXBean.getVmName(), formatByte(jvmTotalMemoryByte), formatByte(runtime.maxMemory()), formatByte(freeMemoryByte), formatByte(jvmTotalMemoryByte - freeMemoryByte), formatDouble((jvmTotalMemoryByte - freeMemoryByte) * 1.0 / jvmTotalMemoryByte), runtimeMXBean.getStartTime(), runtimeMXBean.getUptime());
    }

    /**
     * 获取磁盘使用信息
     */
    public List<DiskInfo> getDiskInfos() {
        OperatingSystem operatingSystem = getOperatingSystem();
        FileSystem fileSystem = operatingSystem.getFileSystem();
        List<DiskInfo> diskInfos = new ArrayList<>();
        Iterable<OSFileStore> fsArray = fileSystem.getFileStores();
        for (OSFileStore fs : fsArray) {
            long usable = fs.getUsableSpace();
            long total = fs.getTotalSpace();
            double usedSize = (total - usable);
            double usePercent = 0;
            if (total > 0) {
                usePercent = formatDouble(usedSize / total * 100);
            }
            DiskInfo diskInfo = new DiskInfo(fs.getName(), fs.getVolume(), fs.getLabel(), fs.getLogicalVolume(), fs.getMount(), fs.getDescription(), fs.getOptions(), fs.getType(), fs.getUUID(), formatByte(total), total, formatByte(total - usable), usable, formatByte(usable), usePercent);
            diskInfos.add(diskInfo);
        }
        return diskInfos;
    }

    /**
     * 获取网络带宽信息
     *
     * @return {@link NetIoInfo}
     */
    public NetIoInfo getNetIoInfo() {
        long rxBytesBegin = 0;
        long txBytesBegin = 0;
        long rxPacketsBegin = 0;
        long txPacketsBegin = 0;
        long rxBytesEnd = 0;
        long txBytesEnd = 0;
        long rxPacketsEnd = 0;
        long txPacketsEnd = 0;
        HardwareAbstractionLayer hal = getHardwareAbstractionLayer();
        List<NetworkIF> listBegin = hal.getNetworkIFs();
        for (NetworkIF net : listBegin) {
            rxBytesBegin += net.getBytesRecv();
            txBytesBegin += net.getBytesSent();
            rxPacketsBegin += net.getPacketsRecv();
            txPacketsBegin += net.getPacketsSent();
        }

        // 暂停 3 秒
        Util.sleep(3000);

        List<NetworkIF> listEnd = hal.getNetworkIFs();
        for (NetworkIF net : listEnd) {
            rxBytesEnd += net.getBytesRecv();
            txBytesEnd += net.getBytesSent();
            rxPacketsEnd += net.getPacketsRecv();
            txPacketsEnd += net.getPacketsSent();
        }

        long rxBytesAvg = (rxBytesEnd - rxBytesBegin) / 3 / 1024;
        long txBytesAvg = (txBytesEnd - txBytesBegin) / 3 / 1024;
        long rxPacketsAvg = (rxPacketsEnd - rxPacketsBegin) / 3 / 1024;
        long txPacketsAvg = (txPacketsEnd - txPacketsBegin) / 3 / 1024;
        return new NetIoInfo(Long.toString(rxPacketsAvg), Long.toString(txPacketsAvg), Long.toString(rxBytesAvg), Long.toString(txBytesAvg));
    }

    public String formatByte(long byteNumber) {
        // 换算单位
        double format = 1024.0;
        double kbNumber = byteNumber / format;
        if (kbNumber < format) {
            return decimalFormat("#.##KB", kbNumber);
        }
        double mbNumber = kbNumber / format;
        if (mbNumber < format) {
            return decimalFormat("#.##MB", mbNumber);
        }
        double gbNumber = mbNumber / format;
        if (gbNumber < format) {
            return decimalFormat("#.##GB", gbNumber);
        }
        return decimalFormat("#.##TB", gbNumber / format);
    }

    public String decimalFormat(String pattern, double number) {
        return new DecimalFormat(pattern).format(number);
    }

    public double formatDouble(double str) {
        return new BigDecimal(str).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

}