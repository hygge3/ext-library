package ext.library.monitor;

/**
 * CPU 信息
 *
 * @param physicalProcessorCount 物理处理器数量
 * @param logicalProcessorCount 逻辑处理器数量
 * @param systemPercent 系统使用率（0-1）
 * @param userPercent 用户使用率（0-1）
 * @param waitPercent IO 等待率（0-1）
 * @param usePercent 总使用率（0-1）
 */
public record CpuInfo(
        int physicalProcessorCount,
        int logicalProcessorCount,
        double systemPercent,
        double userPercent,
        double waitPercent,
        double usePercent
) {
    private static final SystemMonitor MONITOR = new SystemMonitor();

    /**
     * 获取 CPU 信息（静态便捷方法，默认采样 500ms）
     *
     * @return {@link CpuInfo}
     */
    public static CpuInfo get() {
        return MONITOR.getCpuInfo();
    }

    /**
     * 获取 CPU 信息（静态便捷方法）
     *
     * @param sampleMillis 采样时间（毫秒）
     * @return {@link CpuInfo}
     */
    public static CpuInfo get(int sampleMillis) {
        return MONITOR.getCpuInfo(sampleMillis);
    }
}
