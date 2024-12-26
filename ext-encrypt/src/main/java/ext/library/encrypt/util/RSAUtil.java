package ext.library.encrypt.util;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import javax.crypto.Cipher;

import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;

/**
 * RSA 加解密工具
 */
@UtilityClass
public class RSAUtil {

    /**
     * 签名算法名称
     */
    private final String RSA_ALGORITHM = "RSA";

    /**
     * 标准签名算法 RSA
     */
    private final String RSA_SIGNATURE_ALGORITHM = "SHA1withRSA";

    /**
     * 标准签名算法 RSA2
     */
    private final String RSA2_SIGNATURE_ALGORITHM = "SHA256withRSA";

    /**
     * RSA 密钥长度，默认密钥长度是 1024，密钥长度必须是 64 的倍数，在 512 到 65536 位之间，不管是 RSA 还是 RSA2 长度推荐使用 2048
     */
    private final int KEY_SIZE = 2048;

    /**
     * 公钥加密 (用于数据加密)
     *
     * @param str          加密前的字符串
     * @param publicKeyStr base64 编码后的公钥
     * @return base64 编码后的字符串
     * @throws Exception 加密过程中的异常信息
     */
    public String encryptByPublicKey(@NotNull String str, String publicKeyStr) throws Exception {
        // Java 原生 base64 解码
        byte[] pubKey = Base64.getDecoder().decode(publicKeyStr);
        // 创建 X509 编码密钥规范
        X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(pubKey);
        // 返回转换指定算法的 KeyFactory 对象
        KeyFactory keyFactory = KeyFactory.getInstance(RSA_ALGORITHM);
        // 根据 X509 编码密钥规范产生公钥对象
        PublicKey publicKey = keyFactory.generatePublic(x509KeySpec);
        // 根据转换的名称获取密码对象 Cipher（转换的名称：算法/工作模式/填充模式）
        Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());
        // 用公钥初始化此 Cipher 对象（加密模式）
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        // 对数据加密
        byte[] encrypt = cipher.doFinal(str.getBytes());
        // 返回 base64 编码后的字符串
        return Base64.getEncoder().encodeToString(encrypt);
    }

    /**
     * 私钥解密 (用于数据解密)
     *
     * @param str           解密前的字符串
     * @param privateKeyStr base64 编码后的私钥
     * @return 铭文
     * @throws Exception 解密过程中的异常信息
     */
    public String decryptByPrivateKey(String str, String privateKeyStr) throws Exception {
        // Java 原生 base64 解码
        byte[] priKey = Base64.getDecoder().decode(privateKeyStr);
        // 创建 PKCS8 编码密钥规范
        PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(priKey);
        // 返回转换指定算法的 KeyFactory 对象
        KeyFactory keyFactory = KeyFactory.getInstance(RSA_ALGORITHM);
        // 根据 PKCS8 编码密钥规范产生私钥对象
        PrivateKey privateKey = keyFactory.generatePrivate(pkcs8KeySpec);
        // 根据转换的名称获取密码对象 Cipher（转换的名称：算法/工作模式/填充模式）
        Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());
        // 用私钥初始化此 Cipher 对象（解密模式）
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        // 对数据解密
        byte[] decrypt = cipher.doFinal(Base64.getDecoder().decode(str));
        // 返回字符串
        return new String(decrypt);
    }

    /**
     * 私钥加密 (用于数据签名)
     *
     * @param str           加密前的字符串
     * @param privateKeyStr base64 编码后的私钥
     * @return base64 编码后后的字符串
     * @throws Exception 加密过程中的异常信息
     */
    public String encryptByPrivateKey(@NotNull String str, String privateKeyStr) throws Exception {
        // Java 原生 base64 解码
        byte[] priKey = Base64.getDecoder().decode(privateKeyStr);
        // 创建 PKCS8 编码密钥规范
        PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(priKey);
        // 返回转换指定算法的 KeyFactory 对象
        KeyFactory keyFactory = KeyFactory.getInstance(RSA_ALGORITHM);
        // 根据 PKCS8 编码密钥规范产生私钥对象
        PrivateKey privateKey = keyFactory.generatePrivate(pkcs8KeySpec);
        // 根据转换的名称获取密码对象 Cipher（转换的名称：算法/工作模式/填充模式）
        Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());
        // 用私钥初始化此 Cipher 对象（加密模式）
        cipher.init(Cipher.ENCRYPT_MODE, privateKey);
        // 对数据加密
        byte[] encrypt = cipher.doFinal(str.getBytes());
        // 返回 base64 编码后的字符串
        return Base64.getEncoder().encodeToString(encrypt);
    }

    /**
     * 公钥解密 (用于数据验签)
     *
     * @param str          解密前的字符串
     * @param publicKeyStr base64 编码后的公钥
     * @return 解密后的字符串
     * @throws Exception 解密过程中的异常信息
     */
    public String decryptByPublicKey(String str, String publicKeyStr) throws Exception {
        // Java 原生 base64 解码
        byte[] pubKey = Base64.getDecoder().decode(publicKeyStr);
        // 创建 X509 编码密钥规范
        X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(pubKey);
        // 返回转换指定算法的 KeyFactory 对象
        KeyFactory keyFactory = KeyFactory.getInstance(RSA_ALGORITHM);
        // 根据 X509 编码密钥规范产生公钥对象
        PublicKey publicKey = keyFactory.generatePublic(x509KeySpec);
        // 根据转换的名称获取密码对象 Cipher（转换的名称：算法/工作模式/填充模式）
        Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());
        // 用公钥初始化此 Cipher 对象（解密模式）
        cipher.init(Cipher.DECRYPT_MODE, publicKey);
        // 对数据解密
        byte[] decrypt = cipher.doFinal(Base64.getDecoder().decode(str));
        // 返回字符串
        return new String(decrypt);
    }

    /**
     * RSA 签名
     *
     * @param data     待签名数据
     * @param priKey   私钥
     * @param signType RSA 或 RSA2
     * @return 签名
     * @throws Exception 签名过程中的异常信息
     */
    public String sign(byte[] data, byte[] priKey, String signType) throws Exception {
        // 创建 PKCS8 编码密钥规范
        PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(priKey);
        // 返回转换指定算法的 KeyFactory 对象
        KeyFactory keyFactory = KeyFactory.getInstance(RSA_ALGORITHM);
        // 根据 PKCS8 编码密钥规范产生私钥对象
        PrivateKey privateKey = keyFactory.generatePrivate(pkcs8KeySpec);
        // 标准签名算法名称 (RSA 还是 RSA2)
        String algorithm = RSA_ALGORITHM.equals(signType) ? RSA2_SIGNATURE_ALGORITHM : RSA_SIGNATURE_ALGORITHM;
        // 用指定算法产生签名对象 Signature
        Signature signature = Signature.getInstance(algorithm);
        // 用私钥初始化签名对象 Signature
        signature.initSign(privateKey);
        // 将待签名的数据传送给签名对象 (须在初始化之后)
        signature.update(data);
        // 返回签名结果字节数组
        byte[] sign = signature.sign();
        // 返回 Base64 编码后的字符串
        return Base64.getEncoder().encodeToString(sign);
    }

    /**
     * RSA 校验数字签名
     *
     * @param data     待校验数据
     * @param sign     数字签名
     * @param pubKey   公钥
     * @param signType RSA 或 RSA2
     * @return boolean 校验成功返回 true，失败返回 false
     */
    public boolean verify(byte[] data, byte[] sign, byte[] pubKey, String signType) throws Exception {
        // 返回转换指定算法的 KeyFactory 对象
        KeyFactory keyFactory = KeyFactory.getInstance(RSA_ALGORITHM);
        // 创建 X509 编码密钥规范
        X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(pubKey);
        // 根据 X509 编码密钥规范产生公钥对象
        PublicKey publicKey = keyFactory.generatePublic(x509KeySpec);
        // 标准签名算法名称 (RSA 还是 RSA2)
        String algorithm = RSA_ALGORITHM.equals(signType) ? RSA2_SIGNATURE_ALGORITHM : RSA_SIGNATURE_ALGORITHM;
        // 用指定算法产生签名对象 Signature
        Signature signature = Signature.getInstance(algorithm);
        // 用公钥初始化签名对象，用于验证签名
        signature.initVerify(publicKey);
        // 更新签名内容
        signature.update(data);
        // 得到验证结果
        return signature.verify(sign);
    }

}
