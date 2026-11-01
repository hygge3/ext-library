package ext.library.security.util;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Locale;

/**
 * TOTP（RFC 6238）工具类
 * <p>
 * 与 Google Authenticator / Microsoft Authenticator 等主流验证器兼容：HMAC-SHA1、6 位数字、30 秒步长。
 * secret 对外一律 Base32（无填充），即验证器二维码里出现的字符串。
 *
 * @since 4.1.1
 */
public final class TotpUtil {

    private static final String ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";
    private static final String HMAC_ALGORITHM = "HmacSHA1";
    private static final int DIGITS = 6;
    private static final int PERIOD = 30;
    private static final int SECRET_BYTES = 20;
    private static final SecureRandom RANDOM = new SecureRandom();

    private TotpUtil() {
    }

    /**
     * 生成 20 字节随机密钥的 Base32 字符串
     *
     * @return Base32 secret（无填充）
     */
    public static String generateSecret() {
        byte[] bytes = new byte[SECRET_BYTES];
        RANDOM.nextBytes(bytes);
        return encodeBase32(bytes);
    }

    /**
     * 构建 otpauth:// 绑定 URI，前端渲染成二维码由验证器扫描
     *
     * @param issuer  发行方（显示在验证器里）
     * @param account 账号（显示在验证器里）
     * @param secret  Base32 secret
     *
     * @return otpauth URI
     */
    public static String buildUri(String issuer, String account, String secret) {
        String label = encode(issuer) + ":" + encode(account);
        return "otpauth://totp/" + label
               + "?secret=" + secret
               + "&issuer=" + encode(issuer)
               + "&algorithm=SHA1&digits=" + DIGITS + "&period=" + PERIOD;
    }

    /**
     * 计算指定时间片的验证码
     *
     * @param secret      Base32 secret
     * @param epochSecond 时间（秒）
     *
     * @return 6 位验证码
     */
    public static String generateCode(String secret, long epochSecond) {
        return code(decodeBase32(secret), epochSecond / PERIOD);
    }

    /**
     * 校验验证码
     *
     * @param secret Base32 secret
     * @param code   待校验的验证码
     * @param window 时间片容差（0 只接受当前时间片，1 表示前后各 1 个时间片）
     *
     * @return true 校验通过
     */
    public static boolean verify(String secret, String code, int window) {
        if (code == null || code.length() != DIGITS || secret == null || secret.isBlank()) {
            return false;
        }
        byte[] key = decodeBase32(secret);
        long current = System.currentTimeMillis() / 1000 / PERIOD;
        byte[] expected = code.getBytes(StandardCharsets.US_ASCII);
        // 全窗口比对完再返回，避免按时间片提前退出泄露命中位置
        boolean matched = false;
        for (int i = -Math.abs(window); i <= Math.abs(window); i++) {
            matched |= MessageDigest.isEqual(expected, code(key, current + i).getBytes(StandardCharsets.US_ASCII));
        }
        return matched;
    }

    // region HMAC / Base32

    private static String code(byte[] key, long counter) {
        byte[] data = new byte[8];
        for (int i = 7; i >= 0; i--) {
            data[i] = (byte) (counter & 0xFF);
            counter >>>= 8;
        }

        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(new SecretKeySpec(key, HMAC_ALGORITHM));
            byte[] hash = mac.doFinal(data);
            int offset = hash[hash.length - 1] & 0x0F;
            int binary = ((hash[offset] & 0x7F) << 24)
                         | ((hash[offset + 1] & 0xFF) << 16)
                         | ((hash[offset + 2] & 0xFF) << 8)
                         | (hash[offset + 3] & 0xFF);
            return String.format(Locale.ROOT, "%0" + DIGITS + "d", binary % (int) Math.pow(10, DIGITS));
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("TOTP 计算失败", e);
        }
    }

    private static String encodeBase32(byte[] data) {
        StringBuilder sb = new StringBuilder((data.length * 8 + 4) / 5);
        int buffer = 0;
        int bits = 0;
        for (byte b : data) {
            buffer = (buffer << 8) | (b & 0xFF);
            bits += 8;
            while (bits >= 5) {
                sb.append(ALPHABET.charAt((buffer >> (bits - 5)) & 0x1F));
                bits -= 5;
            }
        }
        if (bits > 0) {
            sb.append(ALPHABET.charAt((buffer << (5 - bits)) & 0x1F));
        }
        return sb.toString();
    }

    private static byte[] decodeBase32(String secret) {
        // 验证器里抄来的 secret 常带小写、空格、填充符
        String normalized = secret.replace(" ", "").replace("-", "").replace("=", "").toUpperCase(Locale.ROOT);
        int buffer = 0;
        int bits = 0;
        byte[] out = new byte[normalized.length() * 5 / 8];
        int index = 0;
        for (int i = 0; i < normalized.length(); i++) {
            int value = ALPHABET.indexOf(normalized.charAt(i));
            if (value < 0) {
                throw new IllegalArgumentException("非法的 Base32 secret");
            }
            buffer = (buffer << 5) | value;
            bits += 5;
            if (bits >= 8) {
                out[index++] = (byte) ((buffer >> (bits - 8)) & 0xFF);
                bits -= 8;
            }
        }
        return out;
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8).replace("+", "%20");
    }

    // endregion
}
