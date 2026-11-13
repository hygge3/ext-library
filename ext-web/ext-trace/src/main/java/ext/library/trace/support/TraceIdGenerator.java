package ext.library.trace.support;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 追踪 ID 生成
 */
public final class TraceIdGenerator {

    private static final char[] HEX = "0123456789abcdef".toCharArray();

    private TraceIdGenerator() {}

    /**
     * 生成 16 位十六进制追踪 ID（短、可读、低碰撞）
     */
    public static String generate() {
        long v = ThreadLocalRandom.current().nextLong();
        char[] out = new char[16];
        for (int i = 15; i >= 0; i--) {
            out[i] = HEX[(int) (v & 0xF)];
            v >>>= 4;
        }
        return new String(out);
    }

    /**
     * 生成 UUID 形式追踪 ID（无横线）
     */
    public static String generateUuid() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
