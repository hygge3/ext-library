package ext.library.tool.domain;

import ext.library.tool.constant.Holder;
import org.springframework.util.Assert;

import java.time.Instant;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * MongoDB 对象 ID 工具类
 * <p>
 * 提供 MongoDB ObjectId 的生成，校验和转换功能。该类用于生成符合 MongoDB 规范的 ObjectId 字符串，支持从字节数组转换为十六进制字符串，以及从十六进制字符串转换为字节数组。
 * <p>
 * ObjectId 由 12 字节组成，包含时间戳，机器标识，进程 ID 和随机数等信息。该类通过结合当前时间戳和随机值生成唯一的 ObjectId.
 *
 * @date 2025.10.24
 */
public final class MongoObjectId {

    /** 下一个计数器值，用于生成唯一标识符 */
    private static final AtomicInteger NEXT_COUNTER = new AtomicInteger(Holder.SECURE_RANDOM.nextInt() & 0x00FF_FFFF);
    /** 用于生成随机值的字节数组，长度为 5 */
    private static final byte[] RANDOM_VALUE = new byte[5];

    /** 用于将字节转换为十六进制字符串的字符数组，包含小写字母 */
    private static final char[] HEX = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    static {
        Holder.SECURE_RANDOM.nextBytes(RANDOM_VALUE);
    }

    /**
     * 私有构造函数，用于防止外部实例化
     * <p>
     * 该构造函数为空，仅用于内部使用
     */
    private MongoObjectId() {}

    /**
     * 生成一个十六进制格式的随机字符串
     * <p>
     * 调用 nextBytes 方法获取随机字节数据，然后将其转换为十六进制字符串
     *
     * @return 十六进制格式的随机字符串
     */
    public static String next() {
        return toHex(nextBytes());
    }

    /**
     * 生成一个包含时间戳和计数器值的随机字节数组
     * <p>
     * 该方法使用当前时间戳的前 4 个字节和一个递增的计数器值的后 4 个字节，组合成一个 12 字节的数组。
     *
     * @return 包含时间戳和计数器值的字节数组
     */
    public static byte[] nextBytes() {
        byte[] bytes = new byte[12];
        int time = (int) (Instant.now().getEpochSecond());
        bytes[0] = (byte) (time >>> 24);
        bytes[1] = (byte) (time >>> 16);
        bytes[2] = (byte) (time >>> 8);
        bytes[3] = (byte) (time);

        bytes[4] = RANDOM_VALUE[0];
        bytes[5] = RANDOM_VALUE[1];
        bytes[6] = RANDOM_VALUE[2];
        bytes[7] = RANDOM_VALUE[3];
        bytes[8] = RANDOM_VALUE[4];

        int cnt = NEXT_COUNTER.getAndIncrement() & 0x00FF_FFFF;
        bytes[9] = (byte) (cnt >>> 16);
        bytes[10] = (byte) (cnt >>> 8);
        bytes[11] = (byte) (cnt);
        return bytes;
    }

    /**
     * 验证给定的字符串是否为有效的 24 位十六进制字符串
     * <p>
     * 该方法检查输入字符串是否为 null, 长度是否为 24, 并且每个字符是否为十六进制数字 (0-9,a-f,A-F).
     *
     * @param hex24 要验证的十六进制字符串
     *
     * @return 如果字符串是有效的 24 位十六进制字符串，返回 true; 否则返回 false
     */
    public static boolean isValid(String hex24) {
        if (hex24 == null || hex24.length() != 24) return false;
        for (int i = 0; i < 24; i++) {
            char c = hex24.charAt(i);
            boolean ok = (c >= '0' && c <= '9') || (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F');
            if (!ok) return false;
        }
        return true;
    }

    /**
     * 将 12 字节的字节数组转换为十六进制字符串表示
     * <p>
     * 该方法用于将长度为 12 的字节数组转换为十六进制字符串，每个字节被转换为两个十六进制字符。
     * 如果输入的字节数组为 null 或长度不等于 12, 则会抛出相应的异常。
     *
     * @param bytes12 需要转换的 12 字节字节数组
     *
     * @return 转换后的十六进制字符串
     *
     * @throws NullPointerException     如果输入的字节数组为 null
     * @throws IllegalArgumentException 如果字节数组长度不等于 12
     */
    public static String toHex(byte[] bytes12) {
        Objects.requireNonNull(bytes12, "bytes12 必须不为 null");
        Assert.isTrue(bytes12.length == 12, "ObjectId 字节长度必须为 12");
        char[] out = new char[24];
        int j = 0;
        for (byte b : bytes12) {
            int v = b & 0xFF;
            out[j++] = HEX[v >>> 4];
            out[j++] = HEX[v & 0x0F];
        }
        return new String(out);
    }

    /**
     * 将十六进制字符串转换为字节数组
     * <p>
     * 该方法将一个长度为 24 的十六进制字符串转换为对应的 12 字节的字节数组。每个字节由两个十六进制字符表示。
     *
     * @param hex24 十六进制字符串，长度必须为 24
     *
     * @return 转换后的字节数组，长度为 12
     *
     * @throws IllegalArgumentException 当输入的十六进制字符串无效时抛出
     */
    public static byte[] fromHex(String hex24) {
        Assert.isTrue(isValid(hex24), "无效的 ObjectId 十六进制");
        byte[] out = new byte[12];
        for (int i = 0; i < 12; i++) {
            int hi = toNibble(hex24.charAt(i * 2));
            int lo = toNibble(hex24.charAt(i * 2 + 1));
            out[i] = (byte) ((hi << 4) | lo);
        }
        return out;
    }

    /**
     * 将字符转换为十六进制的 4 位二进制数 (nibble)
     * <p>
     * 该方法用于将单个十六进制字符转换为对应的数值，支持 0-9,a-f,A-F.
     * 如果输入字符不是有效的十六进制字符，则抛出异常。
     *
     * @param c 十六进制字符
     *
     * @return 对应的十六进制数值 (0-15)
     *
     * @throws IllegalArgumentException 当输入字符不是有效的十六进制字符时抛出
     */
    private static int toNibble(char c) {
        if (c >= '0' && c <= '9') return c - '0';
        if (c >= 'a' && c <= 'f') return c - 'a' + 10;
        if (c >= 'A' && c <= 'F') return c - 'A' + 10;
        throw new IllegalArgumentException("无效的十六进制字符：" + c);
    }
}
