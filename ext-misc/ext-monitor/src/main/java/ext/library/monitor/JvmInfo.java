package ext.library.monitor;

/**
 * JVM 信息
 */
public record JvmInfo(
        // jdk 版本
        String jdkVersion,
        // jdk home
        String jdkHome,
        // jdk name
        String jdkName,
        // jvm 总内存
        String jvmTotalMemory,
        // Java 虚拟机将尝试使用的最大内存量
        String maxMemory,
        // 空闲内存
        String freeMemory,
        // 已使用内存
        String usedMemory,
        // 内存使用率
        double usePercent,
        // 返回 Java 虚拟机的启动时间（毫秒）。此方法返回 Java 虚拟机启动的大致时间。
        long startTime,
        // 返回 Java 虚拟机的正常运行时间（毫秒）
        long uptime
) {}