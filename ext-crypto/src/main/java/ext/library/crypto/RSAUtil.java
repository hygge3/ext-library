package ext.library.crypto;

import ext.library.tool.constant.EmojiSymbol;
import ext.library.tool.exception.ToolException;
import ext.library.tool.util.Base64Util;

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
 * RSA 加解密工具类，提供 RSA 算法的密钥对生成、加密、解密、签名和验签功能
 *
 * <p>设计目的：封装 RSA 非对称加密算法的复杂实现，提供简单易用的 API</p>
 *
 * <p>核心功能：
 * <ul>
 *   <li>生成 RSA 密钥对（默认 4096 位）</li>
 *   <li>使用公钥加密、私钥解密</li>
 *   <li>使用私钥签名、公钥验签（SHA256withRSA）</li>
 *   <li>支持大数据分段加解密</li>
 * </ul>
 * </p>
 *
 * <p>注意事项：
 * <ul>
 *   <li>默认密钥长度为 4096 位，安全性高</li>
 *   <li>加密数据长度受密钥长度限制，超长数据自动分段处理</li>
 *   <li>私钥应妥善保管，避免泄露</li>
 * </ul>
 * </p>
 */
public final class RSAUtil {

    private static final String ALGO = "RSA";
    /**
     * 默认密钥长度（位）
     */
    private static final int DEFAULT_KEY_SIZE = 4096;
    /**
     * RSA 最大加密明文大小（基于 4096 位密钥，PKCS1 填充需减去 11 字节）
     */
    private static final int MAX_ENCRYPT_BLOCK = DEFAULT_KEY_SIZE / 8 - 11;
    /**
     * RSA 最大解密密文大小（基于 4096 位密钥）
     */
    private static final int MAX_DECRYPT_BLOCK = DEFAULT_KEY_SIZE / 8;
    /**
     * 标准签名算法 RSA2（SHA256withRSA）
     */
    private static final String SIGN_ALGO = "SHA256withRSA";

    /**
     * 生成 RSA 密钥对
     *
     * @return {@link KeyPair } 密钥对（4096 位）
     */
    public static KeyPair generateKeyPair() {
        KeyPairGenerator generator;
        try {
            generator = KeyPairGenerator.getInstance(ALGO);
        } catch (NoSuchAlgorithmException e) {
            throw new ToolException(EmojiSymbol.CRYPTO, e);
        }
        generator.initialize(DEFAULT_KEY_SIZE);
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
            throw new ToolException(EmojiSymbol.CRYPTO, e);
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
            throw new ToolException(EmojiSymbol.CRYPTO, e);
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
            throw new ToolException(EmojiSymbol.CRYPTO, e);
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
            throw new ToolException(EmojiSymbol.CRYPTO, e);
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
            throw new ToolException(EmojiSymbol.CRYPTO, e);
        }
    }

    /**
     * 验签
     *
     * @param publicKey 公钥字符串
     * @param plainText 明文
     * @param signature 签名（Base64 编码）
     *
     * @return boolean 验签是否通过
     */
    public static boolean verify(String publicKey, String plainText, String signature) {
        try {
            byte[] keyBytes = castPublicKey(publicKey).getEncoded();
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance(ALGO);
            PublicKey key = keyFactory.generatePublic(keySpec);
            Signature signer = Signature.getInstance(SIGN_ALGO);
            signer.initVerify(key);
            signer.update(plainText.getBytes());
            return signer.verify(Base64Util.decodeUrlSafe(signature.getBytes()));
        } catch (Exception e) {
            throw new ToolException(EmojiSymbol.CRYPTO, e);
        }
    }

}
