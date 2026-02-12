package ext.library.monitor;

import java.util.List;

/**
 * 磁盘信息
 *
 * @param name        分区名称
 * @param mount       挂载点
 * @param type        文件系统类型（FAT、NTFS、ext4 等）
 * @param totalSpace  总空间（字节）
 * @param usableSpace 可用空间（字节）
 * @param usePercent  使用率（0-1）
 */
public record DiskInfo(
        String name,
        String mount,
        String type,
        long totalSpace,
        long usableSpace,
        double usePercent
) {

    /**
     * 获取所有磁盘信息（静态便捷方法）
     *
     * @return 磁盘信息列表
     */
    public static List<DiskInfo> getAll() {
        return SystemMonitor.INSTANCE.getDiskInfos();
    }

    /**
     * 获取已用空间（字节）
     */
    public long usedSpace() {
        return totalSpace - usableSpace;
    }

    /**
     * 获取格式化的总空间
     */
    public String totalSpaceFormatted() {
        return FormatUtil.formatBytes(totalSpace);
    }

    /**
     * 获取格式化的可用空间
     */
    public String usableSpaceFormatted() {
        return FormatUtil.formatBytes(usableSpace);
    }

    /**
     * 获取格式化的已用空间
     */
    public String usedSpaceFormatted() {
        return FormatUtil.formatBytes(usedSpace());
    }
}
