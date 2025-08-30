package ext.library.monitor;

/**
 * 系统内存信息
 */
public record MemoryInfo(
        // 总计
        String total,
        // 已使用
        String used,
        // 未使用
        String free,
        // 使用率
        double usePercent
) {}