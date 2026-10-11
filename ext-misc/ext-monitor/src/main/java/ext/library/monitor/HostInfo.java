package ext.library.monitor;

/**
 * 主机信息
 *
 * @param hostName    主机名称
 * @param hostAddress 主机 IP 地址
 * @param osName      操作系统名称
 * @param osArch      系统架构
 * @param userDir     用户工作目录
 */
public record HostInfo(
        String hostName,
        String hostAddress,
        String osName,
        String osArch,
        String userDir
) {

    /**
     * 获取主机信息（静态便捷方法）
     *
     * @return {@link HostInfo}
     */
    public static HostInfo get() {
        return SystemMonitor.INSTANCE.getHostInfo();
    }
}
