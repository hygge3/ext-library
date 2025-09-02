package ext.library.crypto;

import ext.library.tool.core.Exceptions;
import ext.library.tool.util.Base64Util;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.Security;
import java.util.Objects;

@Slf4j
@UtilityClass
public class SM4Util {
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

    /**
     * 生成密钥对
     *
     * @param keySize 密钥大小
     *
     * @return {@link String } 密钥
     */
    public String genKey(Integer keySize) throws NoSuchAlgorithmException, NoSuchProviderException {
        KeyGenerator kg = KeyGenerator.getInstance(ALGORITHM, BouncyCastleProvider.PROVIDER_NAME);
        kg.init(Objects.requireNonNullElse(keySize, 128), new SecureRandom());
        return Base64Util.encodeUrlSafeToStr(kg.generateKey().getEncoded());
    }

    /**
     * 使用指定的加密算法和密钥对给定的字节数组进行加密
     *
     * @param plainText 要加密的字节数组
     * @param secretKey 加密所需的密钥
     *
     * @return byte[]   加密后的字节数组
     */
    public String encryptByECB(String secretKey, String plainText) {
        try {
            Cipher cipher = Cipher.getInstance(SM4_ECB, BouncyCastleProvider.PROVIDER_NAME);
            SecretKeySpec secretKeySpec = new SecretKeySpec(Base64Util.decodeUrlSafe(secretKey), ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
            return Base64Util.encodeUrlSafeToStr(cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            log.error("[🔐] SM4 加密失败", e);
            throw Exceptions.unchecked(e);
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
    public String decryptByECB(String secretKey, String cipherText) {
        try {
            Cipher cipher = Cipher.getInstance(SM4_ECB, BouncyCastleProvider.PROVIDER_NAME);
            SecretKeySpec secretKeySpec = new SecretKeySpec(Base64Util.decodeUrlSafe(secretKey), ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);
            return new String(cipher.doFinal(Base64Util.decodeUrlSafe(cipherText)), StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("[🔐] SM4 ECB 解密失败", e);
            throw Exceptions.unchecked(e);
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
    public String encryptByCBC(String secretKey, String iv, String plainText) {
        try {
            Cipher cipher = Cipher.getInstance(SM4_CBC, BouncyCastleProvider.PROVIDER_NAME);
            SecretKeySpec secretKeySpec = new SecretKeySpec(Base64Util.decodeUrlSafe(secretKey), ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, new IvParameterSpec(Base64Util.decodeUrlSafe(iv)));
            return Base64Util.encodeUrlSafeToStr(cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            log.error("[🔐] SM4 CBC 加密失败", e);
            throw Exceptions.unchecked(e);
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
    public String decryptByCBC(String secretKey, String iv, String cipherText) {
        try {
            Cipher cipher = Cipher.getInstance(SM4_CBC, BouncyCastleProvider.PROVIDER_NAME);
            SecretKeySpec secretKeySpec = new SecretKeySpec(Base64Util.decodeUrlSafe(secretKey), ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, new IvParameterSpec(Base64Util.decodeUrlSafe(iv)));
            return new String(cipher.doFinal(Base64Util.decodeUrlSafe(cipherText)), StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("[🔐] SM4 CBC 解密失败", e);
            throw Exceptions.unchecked(e);
        }
    }
}