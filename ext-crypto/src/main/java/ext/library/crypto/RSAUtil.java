package ext.library.crypto;

import ext.library.tool.core.Exceptions;
import ext.library.tool.util.Base64Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;


/**
 * RSA 加解密
 */
public class RSAUtil {
    private static final Logger log = LoggerFactory.getLogger(RSAUtil.class);

    private static final String ALGO = "RSA";
    /**
     * RSA 最大加密明文大小
     */
    private static final int MAX_ENCRYPT_BLOCK = 117;
    /**
     * RSA 最大解密密文大小
     */
    private static final int MAX_DECRYPT_BLOCK = 128;
    /**
     * 标准签名算法 RSA2
     */
    private static final String SIGN_ALGO = "SHA256withRSA";

    /**
     * 生成密钥对
     *
     * @return {@link KeyPair } 密钥对
     *
     */
    public static KeyPair genKeyPair() {
        KeyPairGenerator generator;
        try {
            generator = KeyPairGenerator.getInstance(ALGO);
        } catch (NoSuchAlgorithmException e) {
            log.error("[🔐] 生成 RSA 密钥对失败", e);
            throw Exceptions.unchecked(e);
        }
        generator.initialize(1024);
        return generator.generateKeyPair();
    }

    /**
     * 转换为公钥格式
     *
     * @param publicKey 公钥字符串
     *
     * @return {@link PublicKey } 公钥
     *
     */
    private static PublicKey castPublicKey(String publicKey) {
        try {
            KeyFactory keyFactory = KeyFactory.getInstance(ALGO);
            byte[] decodedKey = Base64Util.decodeUrlSafe(publicKey.getBytes());
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(decodedKey);
            return keyFactory.generatePublic(keySpec);
        } catch (Exception e) {
            log.error("[🔐] RSA 公钥转换失败", e);
            throw Exceptions.unchecked(e);
        }
    }

    /**
     * 转换为私钥格式
     *
     * @param privateKey 私钥字符串
     *
     * @return {@link PrivateKey } 私钥
     */
    private static PrivateKey castPrivateKey(String privateKey) {
        try {
            KeyFactory keyFactory = KeyFactory.getInstance(ALGO);
            byte[] decodedKey = Base64Util.decodeUrlSafe(privateKey.getBytes());
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(decodedKey);
            return keyFactory.generatePrivate(keySpec);
        } catch (Exception e) {
            log.error("[🔐] RSA 私钥转换失败", e);
            throw Exceptions.unchecked(e);
        }
    }

    /**
     * RSA 加密
     *
     * @param plainText 明文
     * @param publicKey 公钥字符串
     *
     * @return {@link String } 密文
     *
     */
    public static String encrypt(String publicKey, String plainText) {
        try {
            Cipher cipher = Cipher.getInstance(ALGO);
            cipher.init(Cipher.ENCRYPT_MODE, castPublicKey(publicKey));
            int inputLen = plainText.getBytes().length;
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            int offset = 0;
            byte[] cache;
            int i = 0;
            // 对数据分段加密
            while (inputLen - offset > 0) {
                if (inputLen - offset > MAX_ENCRYPT_BLOCK) {
                    cache = cipher.doFinal(plainText.getBytes(), offset, MAX_ENCRYPT_BLOCK);
                } else {
                    cache = cipher.doFinal(plainText.getBytes(), offset, inputLen - offset);
                }
                out.write(cache, 0, cache.length);
                i++;
                offset = i * MAX_ENCRYPT_BLOCK;
            }
            byte[] encryptedData = out.toByteArray();
            out.close();
            // 获取加密内容使用 base64 进行编码，并以 UTF-8 为标准转化成字符串
            // 加密后的字符串
            return Base64Util.encodeUrlSafeToStr(encryptedData);
        } catch (Exception e) {
            log.error("[🔐] RSA 加密失败", e);
            throw Exceptions.unchecked(e);
        }
    }

    /**
     * RSA 解密
     *
     * @param cipherText 密文
     * @param privateKey 私钥字符串
     *
     * @return {@link String } 明文
     *
     */
    public static String decrypt(String privateKey, String cipherText) {
        try {
            Cipher cipher = Cipher.getInstance(ALGO);
            cipher.init(Cipher.DECRYPT_MODE, castPrivateKey(privateKey));
            byte[] dataBytes = Base64Util.decodeUrlSafe(cipherText);
            int inputLen = dataBytes.length;
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            int offset = 0;
            byte[] cache;
            int i = 0;
            // 对数据分段解密
            while (inputLen - offset > 0) {
                if (inputLen - offset > MAX_DECRYPT_BLOCK) {
                    cache = cipher.doFinal(dataBytes, offset, MAX_DECRYPT_BLOCK);
                } else {
                    cache = cipher.doFinal(dataBytes, offset, inputLen - offset);
                }
                out.write(cache, 0, cache.length);
                i++;
                offset = i * MAX_DECRYPT_BLOCK;
            }
            out.close();
            // 解密后的内容
            return out.toString(StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("[🔐] RSA 解密失败", e);
            throw Exceptions.unchecked(e);
        }
    }

    /**
     * 签名
     *
     * @param plainText  明文
     * @param privateKey 私钥字符串
     *
     * @return {@link String } 签名
     *
     */
    public static String sign(String privateKey, String plainText) {
        try {
            byte[] keyBytes = castPrivateKey(privateKey).getEncoded();
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance(ALGO);
            PrivateKey key = keyFactory.generatePrivate(keySpec);
            Signature signature = Signature.getInstance(SIGN_ALGO);
            signature.initSign(key);
            signature.update(plainText.getBytes());
            return Base64Util.encodeUrlSafeToStr(signature.sign());
        } catch (Exception e) {
            log.error("[🔐] RSA 加签失败", e);
            throw Exceptions.throwOut("RSA 加签失败");
        }
    }

    /**
     * 验签
     *
     * @param plainText 明文
     * @param publicKey 公钥字符串
     * @param sign      签名
     *
     * @return boolean 通过验证
     */
    public static boolean verify(String publicKey, String plainText, String sign) {
        try {
            byte[] keyBytes = castPublicKey(publicKey).getEncoded();
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance(ALGO);
            PublicKey key = keyFactory.generatePublic(keySpec);
            Signature signature = Signature.getInstance(SIGN_ALGO);
            signature.initVerify(key);
            signature.update(plainText.getBytes());
            return signature.verify(Base64Util.decodeUrlSafe(sign.getBytes()));
        } catch (Exception e) {
            log.error("[🔐] RSA 验签失败", e);
            throw Exceptions.unchecked(e);
        }
    }

}