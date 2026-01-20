package ext.library.tool.util;

import ext.library.tool.constant.Singletons;
import ext.library.tool.domain.MongoObjectId;
import org.springframework.util.Assert;

import java.nio.ByteBuffer;
import java.util.UUID;

/**
 * ID 生成工具类
 * <p>
 * 提供多种 ID 生成策略：UUID、UUIDv7、ObjectId、SnowflakeId、随机字符串
 *
 * @since 2025.01.01
 */
public final class IdUtil {

    private IdUtil() {
        // 防止实例化
    }

    /**
     * 生成 UUID（去除连字符）
     * <p>
     * 长度：32 字符
     *
     * @return 32 位十六进制字符串
     */
    public static String getUUID() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * 生成 UUID 版本 7（时间有序）
     * <p>
     * UUID v7 基于时间戳和随机数据生成，具有时间有序性，适合数据库主键。
     * <p>
     * 长度：32 字符
     *
     * @return 32 位十六进制字符串
     */
    public static String getUUIDv7() {
        // random bytes
        byte[] value = new byte[16];
        Singletons.SECURE_RANDOM.nextBytes(value);

        // current timestamp in ms
        long timestamp = System.currentTimeMillis();

        // timestamp (big-endian, 48 bits)
        value[0] = (byte) ((timestamp >> 40) & 0xFF);
        value[1] = (byte) ((timestamp >> 32) & 0xFF);
        value[2] = (byte) ((timestamp >> 24) & 0xFF);
        value[3] = (byte) ((timestamp >> 16) & 0xFF);
        value[4] = (byte) ((timestamp >> 8) & 0xFF);
        value[5] = (byte) (timestamp & 0xFF);

        // version and variant
        value[6] = (byte) ((value[6] & 0x0F) | 0x70);
        value[8] = (byte) ((value[8] & 0x3F) | 0x80);

        ByteBuffer buf = ByteBuffer.wrap(value);
        long high = buf.getLong();
        long low = buf.getLong();
        return new UUID(high, low).toString().replace("-", "");
    }

    /**
     * 生成 MongoDB 风格 ObjectId
     * <p>
     * 长度：24 字符
     *
     * @return 24 位十六进制字符串
     */
    public static String getObjectId() {
        return MongoObjectId.next();
    }

    /**
     * 生成雪花算法 ID
     * <p>
     * 长度：最长 19 位数字字符串
     *
     * @return 数字字符串
     */
    public static String getSnowflakeId() {
        return String.valueOf(Singletons.SNOWFLAKE_ID.nextId());
    }

    /**
     * 生成指定长度的随机字符串（URL 安全 Base64）
     * <p>
     * 使用安全随机数生成器，输出仅包含 [A-Za-z0-9_-] 字符
     *
     * @param length 目标字符串长度，必须大于 0
     *
     * @return 指定长度的随机字符串
     *
     * @throws IllegalArgumentException 如果 length 小于等于 0
     */
    public static String random(int length) {
        Assert.isTrue(length > 0, StringUtil.format("请求的随机字符串长度 {} 必须大于 0", length));

        // Base64 编码：3 字节 -> 4 字符，计算所需字节数
        int byteCount = (length * 3 + 3) / 4;
        byte[] buffer = new byte[byteCount];
        Singletons.SECURE_RANDOM.nextBytes(buffer);

        return Base64Util.encodeUrlSafeToStr(buffer).substring(0, length);
    }

}
