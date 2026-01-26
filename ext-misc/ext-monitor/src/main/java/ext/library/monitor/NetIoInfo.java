package ext.library.monitor;

/**
 * 网络 IO 信息
 *
 * @param receivePacketsPerSecond 每秒接收的数据包数
 * @param transmitPacketsPerSecond 每秒发送的数据包数
 * @param receiveBytesPerSecond 每秒接收的字节数
 * @param transmitBytesPerSecond 每秒发送的字节数
 */
public record NetIoInfo(
        long receivePacketsPerSecond,
        long transmitPacketsPerSecond,
        long receiveBytesPerSecond,
        long transmitBytesPerSecond
) {
    private static final SystemMonitor MONITOR = new SystemMonitor();

    /**
     * 获取网络 IO 信息（静态便捷方法，默认采样 1 秒）
     *
     * @return {@link NetIoInfo}
     */
    public static NetIoInfo get() {
        return MONITOR.getNetIoInfo();
    }

    /**
     * 获取网络 IO 信息（静态便捷方法）
     *
     * @param sampleMillis 采样时间（毫秒）
     * @return {@link NetIoInfo}
     */
    public static NetIoInfo get(int sampleMillis) {
        return MONITOR.getNetIoInfo(sampleMillis);
    }

    /**
     * 获取每秒接收的 KB 数
     */
    public double receiveKBPerSecond() {
        return receiveBytesPerSecond / 1024.0;
    }

    /**
     * 获取每秒发送的 KB 数
     */
    public double transmitKBPerSecond() {
        return transmitBytesPerSecond / 1024.0;
    }
}
