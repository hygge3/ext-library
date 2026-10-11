package ext.library.monitor;

/**
 * 系统内存信息
 *
 * @param total      总内存（格式化字符串）
 * @param used       已使用内存（格式化字符串）
 * @param free       空闲内存（格式化字符串）
 * @param usePercent 使用率（0-1）
 */
public record MemoryInfo(
        String total,
        String used,
        String free,
        double usePercent
) {

    /**
     * 获取内存信息（静态便捷方法）
     *
     * @return {@link MemoryInfo}
     */
    public static MemoryInfo get() {
        return SystemMonitor.INSTANCE.getMemoryInfo();
    }
}
