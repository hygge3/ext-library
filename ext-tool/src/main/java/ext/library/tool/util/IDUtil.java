package ext.library.tool.util;

import com.google.common.base.Preconditions;
import ext.library.tool.constant.Holder;
import ext.library.tool.domain.ObjectId;
import lombok.experimental.UtilityClass;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.UUID;

@UtilityClass
public class IDUtil {

    /**
     * 生成 uuid
     * 长度：32
     *
     * @return {@code String }
     */
    public String getUUID() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }

    /**
     * UUID 版本 7 (v7) 由时间戳和随机数据生成。
     * 长度：32
     *
     * @return {@code String }
     */
    public static String getUUIDv7() {
        // random bytes
        byte[] value = new byte[16];
        Holder.SECURE_RANDOM.nextBytes(value);

        // current timestamp in ms
        long timestamp = System.currentTimeMillis();

        // timestamp
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
        return new UUID(high, low).toString().replaceAll("-", "");
    }

    /**
     * 生成 ULID
     * 长度：26
     *
     * @return {@code String }
     */
    public String getULID() {
        return Holder.ULID.nextULID();
    }

    /**
     * 生成 ObjectId
     * 长度：24
     *
     * @return {@code String }
     */
    public String getObjectId() {
        return ObjectId.get().toHexString();
    }

    /**
     * 生成 SnowflakeId
     * 长度：16
     *
     * @return {@code String }
     */
    public String getSnowflakeId() {
        return String.valueOf(Holder.SNOWFLAKE_ID.nextId());
    }

    /**
     * Sqids 编码
     *
     * @param numbers 数字
     *
     * @return {@code String }
     */
    public String sqidsEncode(List<Long> numbers) {
        return Holder.SQIDS.encode(numbers);
    }

    /**
     * Sqids 解码
     *
     * @param sqids SQIDS
     *
     * @return {@code List<Long> }
     */
    public List<Long> sqidsDecode(String sqids) {
        return Holder.SQIDS.decode(sqids);
    }

    /**
     * 随机数生成
     *
     * @param count 字符长度
     *
     * @return 随机数
     */
    public String random(int count) {
        if (count == 0) {
            return "";
        }
        Preconditions.checkArgument(count > 0, "Requested random string length %s is less than 0.", count);
        final byte[] buffer = new byte[5];
        Holder.RANDOM.nextBytes(buffer);
        return Base64Util.encodeToStr(buffer); // or base32()
    }

}