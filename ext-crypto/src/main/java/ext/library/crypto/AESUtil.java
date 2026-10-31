package ext.library.crypto;

import org.springframework.security.crypto.encrypt.AesGcmBytesEncryptor;
import org.springframework.security.crypto.encrypt.BytesEncryptor;
import org.springframework.security.crypto.keygen.KeyGenerators;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * AES 加密工具类，提供 AES-GCM 256 位加密功能
 *
 * <p>设计目的：基于 Spring Security Crypto 封装 AES 加密，提供简单易用的 API</p>
 *
 * <p>核心功能：
 * <ul>
 *   <li>生成安全的加密密钥和盐值</li>
 *   <li>使用 AES-GCM 256 位加密字符串（AEAD 认证加密）</li>
 *   <li>使用 AES-GCM 256 位解密字符串</li>
 * </ul>
 * </p>
 *
 * <p>技术实现：
 * <ul>
 *   <li>加密算法：AES-GCM 256 位（Galois/Counter Mode）</li>
 *   <li>密钥派生：PBKDF2WithHmacSHA256，600000 次迭代</li>
 *   <li>每次加密使用随机 16 字节 IV，确保相同明文产生不同密文</li>
 * </ul>
 * </p>
 *
 * <p>使用示例：
 * <pre>
 * // 生成密钥和盐值
 * String password = AESUtil.generatePassword();
 * String salt = AESUtil.generateSalt();
 *
 * // 加密
 * String encrypted = AESUtil.encrypt(password, salt, "sensitive data");
 *
 * // 解密
 * String decrypted = AESUtil.decrypt(password, salt, encrypted);
 * </pre>
 * </p>
 *
 * <p>注意事项：
 * <ul>
 *   <li>密码和盐值必须妥善保管，两者都需要用于解密</li>
 *   <li>盐值为 16 字节（128 位）Hex 编码字符串</li>
 *   <li>AES-GCM 提供认证加密，可检测密文篡改</li>
 *   <li>PBKDF2 迭代次数较高，实例创建较慢，应复用而非每次调用重建</li>
 * </ul>
 * </p>
 *
 * @see org.springframework.security.crypto.encrypt.AesGcmBytesEncryptor
 */
public final class AESUtil {

    /** 缓存的加密器上限，防止按 (密码,盐) 组合无界增长 */
    private static final int MAX_CACHED_ENCRYPTORS = 1024;

    /** 已派生的加密器：{@code password\0salt} → encryptor */
    private static final Map<String, BytesEncryptor> encryptors = new ConcurrentHashMap<>();

    private AESUtil() {
        // 私有构造函数，防止实例化
    }

    /**
     * 生成随机密码
     *
     * <p>生成 32 字节（256 位）的随机密码，Base64 URL 安全编码</p>
     *
     * @return Base64 URL 安全编码的随机密码
     */
    public static String generatePassword() {
        byte[] keyBytes = KeyGenerators.secureRandom(32).generateKey();
        return Base64.getUrlEncoder().withoutPadding().encodeToString(keyBytes);
    }

    /**
     * 生成随机盐值
     *
     * <p>生成 16 字节（128 位）的随机盐值，Hex 编码</p>
     *
     * @return Hex 编码的随机盐值（32 个十六进制字符）
     */
    public static String generateSalt() {
        byte[] salt = KeyGenerators.secureRandom(16).generateKey();
        return HexFormat.of().formatHex(salt);
    }

    /**
     * 使用 AES-GCM 256 位加密字符串
     *
     * @param password 加密密码
     * @param salt     盐值（Hex 编码，至少 8 字节）
     * @param plainText 待加密的明文
     * @return Base64 URL 安全编码的密文
     */
    public static String encrypt(String password, String salt, String plainText) {
        BytesEncryptor encryptor = getEncryptor(password, salt);
        byte[] encrypted = encryptor.encrypt(plainText.getBytes(StandardCharsets.UTF_8));
        return Base64.getUrlEncoder().withoutPadding().encodeToString(encrypted);
    }

    /**
     * 使用 AES-GCM 256 位解密字符串
     *
     * @param password   解密密码，必须与加密时使用的密码相同
     * @param salt       盐值，必须与加密时使用的盐值相同
     * @param cipherText 待解密的密文（Base64 URL 安全编码）
     * @return 解密后的明文字符串
     */
    public static String decrypt(String password, String salt, String cipherText) {
        BytesEncryptor encryptor = getEncryptor(password, salt);
        byte[] decrypted = encryptor.decrypt(Base64.getUrlDecoder().decode(cipherText));
        return new String(decrypted, StandardCharsets.UTF_8);
    }

    /**
     * 取得（并缓存）已派生的加密器
     * <p>
     * {@code AesGcmBytesEncryptor.withPassword(password, salt).build()} 每次都会跑一遍 60 万次 PBKDF2（实测约 200ms），
     * 每个加密请求重建一次就等于白烧 CPU，并发一上来线程池直接打满。
     *
     * @param password 加密密码
     * @param salt     盐值
     *
     * @return 加密器
     */
    private static BytesEncryptor getEncryptor(String password, String salt) {
        // shortcut: 上限之外整体丢弃；盐按用户随机生成时条目会持续增长，需要精确淘汰时换 Caffeine
        if (encryptors.size() >= MAX_CACHED_ENCRYPTORS) {
            encryptors.clear();
        }
        return encryptors.computeIfAbsent(password + '\u0000' + salt,
                key -> AesGcmBytesEncryptor.withPassword(password, salt).build());
    }

    /**
     * 将字节数组转换为 Hex 字符串
     *
     * @param bytes 字节数组
     * @return Hex 编码字符串
     */
    public static String bytesToHex(byte[] bytes) {
        return HexFormat.of().formatHex(bytes);
    }

    /**
     * 将 Hex 字符串转换为字节数组
     *
     * @param hex Hex 编码字符串
     * @return 字节数组
     */
    public static byte[] hexToBytes(String hex) {
        return HexFormat.of().parseHex(hex);
    }
}
