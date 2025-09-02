package ext.library.crypto;

import ext.library.tool.core.Exceptions;
import ext.library.tool.util.Base64Util;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.encrypt.Encryptors;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Objects;

@Slf4j
@UtilityClass
public class AESUtil {
    private static final String ALGO = "AES";

    /**
     * 生成密钥对
     *
     * @param keySize 密钥大小，128/192/256
     *
     * @return {@link String } 密钥
     */
    public String genKey(Integer keySize) {
        // 获取 AES 密钥生成器
        KeyGenerator keyGenerator;
        try {
            keyGenerator = KeyGenerator.getInstance(ALGO);
        } catch (NoSuchAlgorithmException e) {
            log.error("[🔐] 生成 AES 密钥失败", e);
            throw Exceptions.unchecked(e);
        }
        // 设置密钥长度和随机源
        keyGenerator.init(Objects.requireNonNullElse(keySize, 128), new SecureRandom());
        // 生成密钥
        SecretKey secretKey = keyGenerator.generateKey();
        // 获取密钥内容
        byte[] key = secretKey.getEncoded();
        return Base64Util.encodeToStr(key);
    }

    /**
     * 加密
     *
     * @param secretKey 密钥
     * @param plainText 明文
     * @param salt      盐
     *
     * @return 密文
     */
    public String encrypt(String secretKey, String plainText, String salt) {
        // 明文
        byte[] byteArray = plainText.getBytes();
        // 加密，设置密钥和随机数
        byte[] cipherArrayTemp = Encryptors.standard(secretKey, salt).encrypt(byteArray);
        byte[] cipherArray = Base64Util.encode(cipherArrayTemp);
        return new String(cipherArray);
    }

    /**
     * 解密
     *
     * @param secretKey  密钥
     * @param cipherText 密文
     * @param salt       盐
     *
     * @return 明文
     */
    public String decrypt(String secretKey, String cipherText, String salt) {
        // 密文
        byte[] byteArray = cipherText.getBytes();
        byte[] plainArrayTemp = Base64Util.decode(byteArray);
        // 解密
        byte[] plainArray = Encryptors.standard(secretKey, salt).decrypt(plainArrayTemp);
        return new String(plainArray);
    }
}