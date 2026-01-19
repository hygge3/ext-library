package ext.library.monitor;

/**
 * CPU 信息
 */
public record CpuInfo(
        // 物理处理器数量
        int physicalProcessorCount,
        // 逻辑处理器数量
        int logicalProcessorCount,
        // 系统使用率
        double systemPercent,
        // 用户使用率
        double userPercent,
        // 当前等待率
        double waitPercent,
        // 当前使用率
        double usePercent
) {}