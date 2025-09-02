package ext.library.crypto;

import ext.library.tool.core.Exceptions;
import ext.library.tool.util.Base64Util;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;

@Slf4j
@UtilityClass
public class DESUtil {

    private static final String ALGO = "DES";
    private static final String TRANSFORMATION = "DES/ECB/PKCS5Padding";

    /**
     * 生成密钥
     *
     * @param keySize 密钥大小
     *
     * @return {@link String } 密钥
     */
    public String genKey(Integer keySize) {
        KeyGenerator keyGen = null;// 密钥生成器
        try {
            keyGen = KeyGenerator.getInstance(ALGO);
        } catch (NoSuchAlgorithmException e) {
            log.error("[🔐] 生成 DES 密钥失败", e);
            throw Exceptions.unchecked(e);
        }
        keyGen.init(Objects.requireNonNullElse(keySize, 56));// 初始化密钥生成器
        SecretKey secretKey = keyGen.generateKey();// 生成密钥
        byte[] key = secretKey.getEncoded();// 密钥字节数组
        return Base64Util.encodeToStr(key);
    }

    /**
     * DES 加密
     *
     * @param secretKey 密钥
     * @param plainText 明文
     *
     * @return 密文
     */
    public String encrypt(String secretKey, String plainText) {
        byte[] encryptedBytes;
        try {
            DESKeySpec desKeySpec = new DESKeySpec(secretKey.getBytes(StandardCharsets.UTF_8));
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(ALGO);
            SecretKey key = keyFactory.generateSecret(desKeySpec);

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, key);

            encryptedBytes = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            log.error("[🔐] DES 加密失败", e);
            throw Exceptions.unchecked(e);
        }
        return Base64Util.encodeToStr(encryptedBytes);
    }

    /**
     * DES 解密
     *
     * @param secretKey  密钥
     * @param cipherText 密文
     *
     * @return 明文
     */
    public String decrypt(String secretKey, String cipherText) {
        byte[] decryptedBytes;
        try {
            DESKeySpec desKeySpec = new DESKeySpec(secretKey.getBytes(StandardCharsets.UTF_8));
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(ALGO);
            SecretKey key = keyFactory.generateSecret(desKeySpec);

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, key);

            decryptedBytes = cipher.doFinal(Base64Util.decode(cipherText));
        } catch (Exception e) {
            log.error("[🔐] DES 解密失败", e);
            throw Exceptions.unchecked(e);
        }
        return new String(decryptedBytes, StandardCharsets.UTF_8);
    }

}