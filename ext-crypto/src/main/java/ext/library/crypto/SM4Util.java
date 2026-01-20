package ext.library.crypto;

import ext.library.tool.constant.EmojiSymbol;
import ext.library.tool.exception.ToolException;
import ext.library.tool.util.Base64Util;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.security.Security;
import java.util.Objects;

/**
 * SM4 国密对称加密工具类，提供 SM4 算法的密钥生成、加密和解密功能
 *
 * <p>设计目的：封装国密 SM4 对称加密算法，提供 ECB 和 CBC 两种工作模式</p>
 *
 * <p>核心功能：
 * <ul>
 *   <li>生成 SM4 密钥（128 位）</li>
 *   <li>ECB 模式加解密（电子密码本模式，不推荐用于大量数据）</li>
 *   <li>CBC 模式加解密（密码分组链接模式，推荐）</li>
 * </ul>
 * </p>
 *
 * <p>使用示例：
 * <pre>
 * String key = SM4Util.generateKey(128);
 * String iv = SM4Util.generateKey(128); // CBC 模式需要 IV
 *
 * // ECB 模式
 * String cipherECB = SM4Util.encryptByECB(key, "plaintext");
 * String plainECB = SM4Util.decryptByECB(key, cipherECB);
 *
 * // CBC 模式
 * String cipherCBC = SM4Util.encryptByCBC(key, iv, "plaintext");
 * String plainCBC = SM4Util.decryptByCBC(key, iv, cipherCBC);
 * </pre>
 * </p>
 */
public final class SM4Util {

    private static final String ALGORITHM = "SM4";
    /** 电子密码本模式 */
    private static final String SM4_ECB = "SM4/ECB/PKCS7Padding";
    /** 密码分组链接模式 */
    private static final String SM4_CBC = "SM4/CBC/PKCS7Padding";

    static {
        if (Objects.isNull(Security.getProvider(BouncyCastleProvider.PROVIDER_NAME))) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }

    private SM4Util() {
        // 私有构造函数，防止实例化
    }

    /**
     * 生成 SM4 密钥
     *
     * @param keySize 密钥大小（位），支持 128，为 null 时默认使用 128 位
     * @return Base64 URL 安全编码的密钥字符串
     * @throws ToolException 当密钥生成失败时抛出
     */
    public static String generateKey(Integer keySize) {
        try {
            KeyGenerator kg = KeyGenerator.getInstance(ALGORITHM, BouncyCastleProvider.PROVIDER_NAME);
            kg.init(Objects.requireNonNullElse(keySize, 128), new SecureRandom());
            return Base64Util.encodeUrlSafeToStr(kg.generateKey().getEncoded());
        } catch (Exception e) {
            throw new ToolException(EmojiSymbol.CRYPTO, e);
        }
    }

    /**
     * 使用指定的加密算法和密钥对给定的字节数组进行加密
     *
     * @param plainText 要加密的字节数组
     * @param secretKey 加密所需的密钥
     *
     * @return byte[]   加密后的字节数组
     */
    public static String encryptByECB(String secretKey, String plainText) {
        try {
            Cipher cipher = Cipher.getInstance(SM4_ECB, BouncyCastleProvider.PROVIDER_NAME);
            SecretKeySpec secretKeySpec = new SecretKeySpec(Base64Util.decodeUrlSafe(secretKey), ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
            return Base64Util.encodeUrlSafeToStr(cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new ToolException(EmojiSymbol.CRYPTO, e);
        }
    }

    /**
     * 使用指定的加密算法和密钥对给定的字节数组进行解密
     *
     * @param cipherText 要解密的字节数组
     * @param secretKey  解密所需的密钥
     *
     * @return byte[]   解密后的字节数组
     */
    public static String decryptByECB(String secretKey, String cipherText) {
        try {
            Cipher cipher = Cipher.getInstance(SM4_ECB, BouncyCastleProvider.PROVIDER_NAME);
            SecretKeySpec secretKeySpec = new SecretKeySpec(Base64Util.decodeUrlSafe(secretKey), ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);
            return new String(cipher.doFinal(Base64Util.decodeUrlSafe(cipherText)), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new ToolException(EmojiSymbol.CRYPTO, e);
        }
    }

    /**
     * 使用指定的加密算法和密钥对给定的字节数组进行加密
     *
     * @param plainText 要加密的字节数组
     * @param secretKey 加密所需的密钥
     *
     * @return byte[]   加密后的字节数组
     */
    public static String encryptByCBC(String secretKey, String iv, String plainText) {
        try {
            Cipher cipher = Cipher.getInstance(SM4_CBC, BouncyCastleProvider.PROVIDER_NAME);
            SecretKeySpec secretKeySpec = new SecretKeySpec(Base64Util.decodeUrlSafe(secretKey), ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, new IvParameterSpec(Base64Util.decodeUrlSafe(iv)));
            return Base64Util.encodeUrlSafeToStr(cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new ToolException(EmojiSymbol.CRYPTO, e);
        }
    }

    /**
     * 使用指定的加密算法和密钥对给定的字节数组进行解密
     *
     * @param cipherText 要解密的字节数组
     * @param secretKey  解密所需的密钥
     *
     * @return byte[]   解密后的字节数组
     */
    public static String decryptByCBC(String secretKey, String iv, String cipherText) {
        try {
            Cipher cipher = Cipher.getInstance(SM4_CBC, BouncyCastleProvider.PROVIDER_NAME);
            SecretKeySpec secretKeySpec = new SecretKeySpec(Base64Util.decodeUrlSafe(secretKey), ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, new IvParameterSpec(Base64Util.decodeUrlSafe(iv)));
            return new String(cipher.doFinal(Base64Util.decodeUrlSafe(cipherText)), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new ToolException(EmojiSymbol.CRYPTO, e);
        }
    }
}
