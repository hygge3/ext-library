package ext.library.tool.domain;

import ext.library.tool.util.StringUtil;
import org.springframework.util.Assert;

/**
 * Twitter Snowflake 算法实现
 * <p>
 * 64-bit 结构：
 * 1 位符号位 (始终为 0) +
 * 41 位时间戳 (毫秒，相对自定义 epoch) +
 * 5 位数据中心 ID +
 * 5 位工作节点 ID +
 * 12 位序列号 (同毫秒内递增)
 *
 * @since 2025.01.01
 */
public final class SnowflakeId {
    /** 自定义纪元（epoch）：2025-01-01T00:00:00Z */
    private static final long epoch = 1735689600000L;

    /** 机器 ID 所占的位数 */
    private static final int workerIdBits = 5;

    /** 数据中心 ID 所占的位数 */
    private static final int datacenterIdBits = 5;

    /** 序列号所占的位数 */
    private static final int sequenceBits = 12;

    /** 支持的最大机器 ID，结果是 31 */
    private static final long maxWorkerId = ~(-1L << workerIdBits);

    /** 支持的最大数据中心 ID，结果是 31 */
    private static final long maxDatacenterId = ~(-1L << datacenterIdBits);

    /** 序列号掩码，用于限制序列号范围 (0~4095) */
    private static final long sequenceMask = ~(-1L << sequenceBits);

    /** 机器 ID 左移位数 (12 位) */
    private static final int workerIdShift = sequenceBits;

    /** 数据中心 ID 左移位数 (17 位) */
    private static final int datacenterIdShift = sequenceBits + workerIdBits;

    /** 时间戳左移位数 (22 位) */
    private static final int timestampLeftShift = sequenceBits + workerIdBits + datacenterIdBits;

    /** 数据中心 ID (0~31) */
    private final long datacenterId;

    /** 工作机器 ID (0~31) */
    private final long workerId;

    /** 上次生成 ID 的时间戳 */
    private long lastTimestamp = -1L;

    /** 毫秒内序列号 (0~4095) */
    private long sequence = 0L;

    /**
     * 构造 Snowflake ID 生成器
     *
     * @param datacenterId 数据中心 ID，范围 [0, 31]
     * @param workerId     工作节点 ID，范围 [0, 31]
     *
     * @throws IllegalArgumentException 如果 ID 超出有效范围
     */
    public SnowflakeId(long datacenterId, long workerId) {
        Assert.isTrue(datacenterId >= 0 && datacenterId <= maxDatacenterId,
                StringUtil.format("datacenterId 超出范围：{}", datacenterId));
        Assert.isTrue(workerId >= 0 && workerId <= maxWorkerId,
                StringUtil.format("workerId 超出范围：{}", workerId));
        this.datacenterId = datacenterId;
        this.workerId = workerId;
    }

    /**
     * 生成下一个 ID（线程安全）
     *
     * @return 生成的唯一 ID
     */
    public synchronized long nextId() {
        long timestamp = currentTime();

        // 时钟回拨保护：等待到 lastTimestamp 之后
        if (timestamp < lastTimestamp) {
            timestamp = waitUntil(lastTimestamp);
        }

        if (timestamp == lastTimestamp) {
            sequence = (sequence + 1) & sequenceMask;
            if (sequence == 0) {
                // 序列溢出，等待到下一毫秒
                timestamp = waitNextMillis(lastTimestamp);
            }
        } else {
            // 新毫秒重置序列
            sequence = 0L;
        }

        lastTimestamp = timestamp;

        return ((timestamp - epoch) << timestampLeftShift)
                | (datacenterId << datacenterIdShift)
                | (workerId << workerIdShift)
                | sequence;
    }

    /**
     * 获取当前时间戳（毫秒）
     *
     * @return 当前时间戳
     */
    private long currentTime() {
        return System.currentTimeMillis();
    }

    /**
     * 等待直到下一毫秒
     * <p>
     * 当同一毫秒内序列号溢出时调用，阻塞等待到下一毫秒
     *
     * @param lastTs 上次生成 ID 的时间戳
     *
     * @return 下一毫秒的时间戳
     */
    private long waitNextMillis(long lastTs) {
        long ts = currentTime();
        while (ts <= lastTs) {
            ts = currentTime();
        }
        return ts;
    }

    /**
     * 等待直到指定时间戳
     * <p>
     * 当检测到时钟回拨时调用，阻塞等待直到时间恢复
     *
     * @param targetTs 目标时间戳
     *
     * @return 达到或超过目标的时间戳
     */
    private long waitUntil(long targetTs) {
        long ts = currentTime();
        while (ts < targetTs) {
            ts = currentTime();
        }
        return ts;
    }
}
