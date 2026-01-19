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
 */
public final class SnowflakeId {

    // 自定义纪元（epoch）：2025-01-01T00:00:00Z
    private static final long EPOCH = 1735689600000L;

    /** 机器 id 所占的位数 */
    private static final int WORKER_ID_BITS = 5;
    /** 数据标识 id 所占的位数 */
    private static final int DATACENTER_ID_BITS = 5;
    /** 序列在 id 中占的位数 */
    private static final int SEQUENCE_BITS = 12;

    /** 机器 ID 向左移 12 位 */
    private static final long MAX_WORKER_ID = ~(-1L << WORKER_ID_BITS);
    /** 支持的最大数据标识 id，结果是 31 */// 31
    private static final long MAX_DATACENTER_ID = ~(-1L << DATACENTER_ID_BITS);
    /** 生成序列的掩码，这里为 4095 (0b111111111111=0xfff=4095) */
    private static final long SEQUENCE_MASK = ~(-1L << SEQUENCE_BITS);

    private static final int WORKER_ID_SHIFT = SEQUENCE_BITS;                                      // 12
    private static final int DATACENTER_ID_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS;                 // 17
    private static final int TIMESTAMP_LEFT_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS + DATACENTER_ID_BITS; // 22

    /** 数据中心 ID(0~31) */
    private final long datacenterId;
    /** 工作机器 ID(0~31) */
    private final long workerId;

    /** 上次生成 ID 的时间截 */
    private long lastTimestamp = -1L;
    /** 毫秒内序列 (0~4095) */
    private long sequence = 0L;

    /**
     * @param datacenterId 数据中心 ID，范围 [0,31]
     * @param workerId     工作节点 ID，范围 [0,31]
     */
    public SnowflakeId(long datacenterId, long workerId) {
        Assert.isTrue(datacenterId >= 0 && datacenterId <= MAX_DATACENTER_ID, StringUtil.format("datacenterId 超出范围：{}", datacenterId));
        Assert.isTrue(workerId >= 0 && workerId <= MAX_WORKER_ID, StringUtil.format("workerId 超出范围：{}", workerId));
        this.datacenterId = datacenterId;
        this.workerId = workerId;
    }

    /**
     * 生成下一个 ID（线程安全）
     */
    public synchronized long nextId() {
        long timestamp = currentTime();

        // 时钟回拨保护：等待到 lastTimestamp 之后
        if (timestamp < lastTimestamp) {
            timestamp = waitUntil(lastTimestamp);
        }

        if (timestamp == lastTimestamp) {
            sequence = (sequence + 1) & SEQUENCE_MASK;
            if (sequence == 0) {
                // 序列溢出，等待到下一毫秒
                timestamp = waitNextMillis(lastTimestamp);
            }
        } else {
            // 新毫秒重置序列
            sequence = 0L;
        }

        lastTimestamp = timestamp;

        return ((timestamp - EPOCH) << TIMESTAMP_LEFT_SHIFT) | (datacenterId << DATACENTER_ID_SHIFT) | (workerId << WORKER_ID_SHIFT) | sequence;
    }

    private long currentTime() {
        return System.currentTimeMillis();
    }

    private long waitNextMillis(long lastTs) {
        long ts = currentTime();
        while (ts <= lastTs) {
            ts = currentTime();
        }
        return ts;
    }

    private long waitUntil(long targetTs) {
        long ts = currentTime();
        while (ts < targetTs) {
            ts = currentTime();
        }
        return ts;
    }
}