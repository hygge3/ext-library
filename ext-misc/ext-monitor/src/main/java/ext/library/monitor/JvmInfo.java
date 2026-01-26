package ext.library.monitor;

/**
 * JVM 信息
 *
 * @param jdkVersion JDK 版本
 * @param jdkHome JDK 安装目录
 * @param jdkName JVM 名称
 * @param jvmTotalMemory JVM 总内存（格式化字符串）
 * @param maxMemory 最大可用内存（格式化字符串）
 * @param freeMemory 空闲内存（格式化字符串）
 * @param usedMemory 已使用内存（格式化字符串）
 * @param usePercent 内存使用率（0-1）
 * @param startTime JVM 启动时间（毫秒时间戳）
 * @param uptime JVM 运行时长（毫秒）
 */
public record JvmInfo(
        String jdkVersion,
        String jdkHome,
        String jdkName,
        String jvmTotalMemory,
        String maxMemory,
        String freeMemory,
        String usedMemory,
        double usePercent,
        long startTime,
        long uptime
) {
    private static final SystemMonitor MONITOR = new SystemMonitor();

    /**
     * 获取 JVM 信息（静态便捷方法）
     *
     * @return {@link JvmInfo}
     */
    public static JvmInfo get() {
        return MONITOR.getJvmInfo();
    }
}
