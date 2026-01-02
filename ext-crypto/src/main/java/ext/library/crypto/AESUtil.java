package ext.library.crypto;

import ext.library.tool.constant.EmojiSymbol;
import ext.library.tool.exception.ToolException;
import ext.library.tool.util.Base64Util;
import org.springframework.security.crypto.encrypt.Encryptors;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Objects;

/**
 * AES加密工具类，提供AES算法的密钥生成、加密和解密功能
 * 
 * <p>设计目的：封装AES加密算法的复杂实现，提供简单易用的API，确保加密操作的安全性和一致性</p>
 * 
 * <p>核心功能：
 * <ul>
 *   <li>生成安全的AES密钥</li>
 *   <li>使用AES算法加密字符串</li>
 *   <li>使用AES算法解密字符串</li>
 * </ul>
 * </p>
 * 
 * <p>使用场景：
 * <ul>
 *   <li>敏感数据加密存储</li>
 *   <li>数据传输加密</li>
 *   <li>配置文件敏感信息加密</li>
 * </ul>
 * </p>
 * 
 * <p>注意事项：
 * <ul>
 *   <li>密钥长度支持128、192、256位，推荐使用256位</li>
 *   <li>加密和解密必须使用相同的密钥和盐值</li>
 *   <li>密钥应妥善保管，避免泄露</li>
 * </ul>
 * </p>
 */
public final class AESUtil {
    private static final String ALGO = "AES";

    /**
     * 生成AES密钥
     *
     * @param keySize 密钥大小，支持128、192、256位，为null时默认使用128位
     * @return Base64 URL安全编码的密钥字符串
     * @throws ToolException 当密钥生成失败时抛出
     * 
     * <pre>
     * // 生成128位密钥
     * String key128 = AESUtil.genKey(128);
     * // 生成256位密钥
     * String key256 = AESUtil.genKey(256);
     * // 使用默认密钥长度(128位)
     * String keyDefault = AESUtil.genKey(null);
     * </pre>
     */
    public static String genKey(Integer keySize) {
        // 获取 AES 密钥生成器
        KeyGenerator keyGenerator;
        try {
            keyGenerator = KeyGenerator.getInstance(ALGO);
        } catch (NoSuchAlgorithmException e) {
            throw new ToolException(EmojiSymbol.CRYPTO, e);
        }
        // 设置密钥长度和随机源
        keyGenerator.init(Objects.requireNonNullElse(keySize, 128), new SecureRandom());
        // 生成密钥
        SecretKey secretKey = keyGenerator.generateKey();
        // 获取密钥内容
        byte[] key = secretKey.getEncoded();
        return Base64Util.encodeUrlSafeToStr(key);
    }

    /**
     * 使用AES算法加密字符串
     *
     * @param secretKey 密钥字符串
     * @param plainText 待加密的明文
     * @param salt 加密盐值，用于增强安全性
     * @return Base64 URL安全编码的密文字符串
     * @throws ToolException 当加密失败时抛出
     * 
     * <pre>
     * String key = AESUtil.genKey(256);
     * String salt = "random-salt";
     * String plainText = "sensitive data";
     * String cipherText = AESUtil.encrypt(key, plainText, salt);
     * </pre>
     */
    public static String encrypt(String secretKey, String plainText, String salt) {
        // 明文转换为字节数组，使用UTF-8字符集
        byte[] byteArray = plainText.getBytes(StandardCharsets.UTF_8);
        // 加密，设置密钥和随机数
        byte[] cipherArrayTemp = Encryptors.standard(secretKey, salt).encrypt(byteArray);
        byte[] cipherArray = Base64Util.encodeUrlSafe(cipherArrayTemp);
        return new String(cipherArray, StandardCharsets.UTF_8);
    }

    /**
     * 使用AES算法解密字符串
     *
     * @param secretKey 密钥字符串，必须与加密时使用的密钥相同
     * @param cipherText 待解密的密文
     * @param salt 加密盐值，必须与加密时使用的盐值相同
     * @return 解密后的明文字符串
     * @throws ToolException 当解密失败时抛出，可能是因为密钥或盐值不匹配
     * 
     * <pre>
     * String key = AESUtil.genKey(256);
     * String salt = "random-salt";
     * String plainText = "sensitive data";
     * String cipherText = AESUtil.encrypt(key, plainText, salt);
     * String decryptedText = AESUtil.decrypt(key, cipherText, salt);
     * // decryptedText.equals(plainText) == true
     * </pre>
     */
    public static String decrypt(String secretKey, String cipherText, String salt) {
        // 密文转换为字节数组，使用UTF-8字符集
        byte[] byteArray = cipherText.getBytes(StandardCharsets.UTF_8);
        byte[] plainArrayTemp = Base64Util.decodeUrlSafe(byteArray);
        // 解密
        byte[] plainArray = Encryptors.standard(secretKey, salt).decrypt(plainArrayTemp);
        return new String(plainArray, StandardCharsets.UTF_8);
    }
}
