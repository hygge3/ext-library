package ext.library.crypto;

import ext.library.tool.constant.EmojiSymbol;
import ext.library.tool.exception.ToolException;
import ext.library.tool.util.Base64Util;
import org.bouncycastle.crypto.CryptoException;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.crypto.engines.SM2Engine;
import org.bouncycastle.crypto.generators.ECKeyPairGenerator;
import org.bouncycastle.crypto.params.ECDomainParameters;
import org.bouncycastle.crypto.params.ECKeyGenerationParameters;
import org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import org.bouncycastle.crypto.params.ECPublicKeyParameters;
import org.bouncycastle.crypto.params.ParametersWithRandom;
import org.bouncycastle.crypto.signers.SM2Signer;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPublicKey;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jce.spec.ECParameterSpec;
import org.bouncycastle.math.ec.ECPoint;
import org.bouncycastle.math.ec.custom.gm.SM2P256V1Curve;

import java.io.ByteArrayInputStream;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.PublicKey;
import java.security.Security;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

/**
 * SM2 国密非对称加密工具类，提供 SM2 算法的密钥对生成、加密、解密、签名和验签功能
 *
 * <p>设计目的：封装国密 SM2 非对称加密算法，提供简单易用的 API</p>
 *
 * <p>核心功能：
 * <ul>
 *   <li>生成 SM2 密钥对</li>
 *   <li>使用公钥加密、私钥解密</li>
 *   <li>使用私钥签名、公钥验签</li>
 *   <li>支持证书验签</li>
 * </ul>
 * </p>
 *
 * <p>数据格式：
 * <ul>
 *   <li>公钥/私钥：Base64 URL 安全编码</li>
 *   <li>密文数据顺序：C1C3C2</li>
 *   <li>字符编码：UTF-8</li>
 * </ul>
 * </p>
 *
 * <p>使用示例：
 * <pre>
 * SM2KeyPair keyPair = SM2Util.generateKeyPair();
 * String encrypted = SM2Util.encrypt(keyPair.publicKey(), "plaintext");
 * String decrypted = SM2Util.decrypt(keyPair.privateKey(), encrypted);
 *
 * String signature = SM2Util.sign(keyPair.privateKey(), "data");
 * boolean verified = SM2Util.verify(keyPair.publicKey(), "data", signature);
 * </pre>
 * </p>
 *
 * @since 2025.09.02
 */
public final class SM2Util {

    // SM2 曲线参数
    private static final SM2P256V1Curve curve = new SM2P256V1Curve();
    private static final ECPoint ecPoint = curve.createPoint(
            new BigInteger("32C4AE2C1F1981195F9904466A39C9948FE30BBFF2660BE1715A4589334C74C7", 16),
            new BigInteger("BC3736A2F4F6779C59BDCEE36B692153D0A9877CC62A474002DF32E52139F0A0", 16)
    );
    private static final ECDomainParameters domainParams = new ECDomainParameters(
            curve, ecPoint, curve.getOrder(), curve.getCofactor()
    );

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    private SM2Util() {
        // 私有构造函数，防止实例化
    }

    /**
     * 生成 SM2 密钥对
     *
     * @return SM2 密钥对
     */
    public static SM2KeyPair generateKeyPair() {
        // 初始化密钥对生成器
        ECKeyPairGenerator keyPairGenerator = new ECKeyPairGenerator();
        keyPairGenerator.init(new ECKeyGenerationParameters(domainParams, ext.library.tool.constant.Singletons.SECURE_RANDOM));

        // 生成密钥对
        org.bouncycastle.crypto.AsymmetricCipherKeyPair keyPair = keyPairGenerator.generateKeyPair();
        ECPrivateKeyParameters privateKey = (ECPrivateKeyParameters) keyPair.getPrivate();
        ECPublicKeyParameters publicKey = (ECPublicKeyParameters) keyPair.getPublic();

        // 转换为 Base64 格式
        byte[] priKeyBytes = privateKey.getD().toByteArray();
        // 处理可能的负数情况，去掉多余的符号位
        if (priKeyBytes[0] == 0 && priKeyBytes.length > 32) {
            byte[] tmp = new byte[priKeyBytes.length - 1];
            System.arraycopy(priKeyBytes, 1, tmp, 0, tmp.length);
            priKeyBytes = tmp;
        }
        String priKeyBase64 = Base64Util.encodeUrlSafeToStr(priKeyBytes);
        String pubKeyBase64 = Base64Util.encodeUrlSafeToStr(publicKey.getQ().getEncoded(false));
        return new SM2KeyPair(priKeyBase64, pubKeyBase64);
    }

    /**
     * SM2 加密
     *
     * @param publicKey 公钥（Base64 URL 安全编码）
     * @param plainText 明文
     *
     * @return 密文（Base64 URL 安全编码）
     */
    public static String encrypt(String publicKey, String plainText) {
        // 解析公钥
        byte[] pubKeyBytes = Base64Util.decodeUrlSafe(publicKey);
        ECPoint pubKeyPoint = curve.decodePoint(pubKeyBytes);
        ECPublicKeyParameters pubKeyParams = new ECPublicKeyParameters(pubKeyPoint, domainParams);

        // 初始化加密引擎
        SM2Engine sm2Engine = new SM2Engine();
        sm2Engine.init(true, new ParametersWithRandom(pubKeyParams));

        // 执行加密
        try {
            byte[] encryptedData = sm2Engine.processBlock(plainText.getBytes(StandardCharsets.UTF_8), 0, plainText.length());
            // 返回 Base64 编码结果
            return Base64Util.encodeUrlSafeToStr(encryptedData);
        } catch (InvalidCipherTextException e) {
            throw new ToolException(EmojiSymbol.CRYPTO, e);
        }
    }

    /**
     * SM2 解密
     *
     * @param privateKey 私钥（Base64 URL 安全编码）
     * @param cipherText 密文（Base64 URL 安全编码）
     *
     * @return 明文
     */
    public static String decrypt(String privateKey, String cipherText) {
        // 解析私钥
        byte[] priKeyBytes = Base64Util.decodeUrlSafe(privateKey);
        BigInteger priKeyBig = new BigInteger(1, priKeyBytes);
        ECPrivateKeyParameters priKeyParams = new ECPrivateKeyParameters(priKeyBig, domainParams);

        // 初始化解密引擎
        SM2Engine sm2Engine = new SM2Engine();
        sm2Engine.init(false, priKeyParams);

        // 解码密文
        byte[] cipherData = Base64Util.decodeUrlSafe(cipherText);
        try {
            // 执行解密
            byte[] decryptedData = sm2Engine.processBlock(cipherData, 0, cipherData.length);

            // 返回明文
            return new String(decryptedData, StandardCharsets.UTF_8);
        } catch (InvalidCipherTextException e) {
            throw new ToolException(EmojiSymbol.CRYPTO, e);
        }
    }

    /**
     * SM2 签名
     *
     * @param privateKey 私钥（Base64 URL 安全编码）
     * @param plainText  待签名数据
     *
     * @return 签名值（Base64 URL 安全编码）
     */
    public static String sign(String privateKey, String plainText) {
        // 解析私钥
        byte[] priKeyBytes = Base64Util.decodeUrlSafe(privateKey);
        BigInteger priKeyBig = new BigInteger(1, priKeyBytes);
        ECPrivateKeyParameters priKeyParams = new ECPrivateKeyParameters(priKeyBig, domainParams);

        // 初始化签名器
        SM2Signer signer = new SM2Signer();
        signer.init(true, new ParametersWithRandom(priKeyParams));

        // 生成签名
        byte[] data = plainText.getBytes(StandardCharsets.UTF_8);
        signer.update(data, 0, data.length);
        try {
            byte[] signature = signer.generateSignature();
            // 返回 Base64 编码结果
            return Base64Util.encodeUrlSafeToStr(signature);
        } catch (CryptoException e) {
            throw new ToolException(EmojiSymbol.CRYPTO, e);
        }
    }

    /**
     * SM2 验签
     *
     * @param publicKey 公钥（Base64 URL 安全编码）
     * @param plainText 待验证数据
     * @param signature 签名值（Base64 URL 安全编码）
     *
     * @return 验签是否通过
     */
    public static boolean verify(String publicKey, String plainText, String signature) {
        // 解析公钥
        byte[] pubKeyBytes = Base64Util.decodeUrlSafe(publicKey);
        ECPoint pubKeyPoint = curve.decodePoint(pubKeyBytes);
        ECPublicKeyParameters pubKeyParams = new ECPublicKeyParameters(pubKeyPoint, domainParams);

        // 初始化验签器
        SM2Signer signer = new SM2Signer();
        signer.init(false, pubKeyParams);

        // 验证签名
        byte[] data = plainText.getBytes(StandardCharsets.UTF_8);
        byte[] signData = Base64Util.decodeUrlSafe(signature);
        signer.update(data, 0, data.length);
        return signer.verifySignature(signData);
    }

    /**
     * 使用证书进行 SM2 验签
     *
     * @param certText  证书（Base64 URL 安全编码）
     * @param plainText 待验证数据
     * @param signature 签名值（Base64 URL 安全编码）
     *
     * @return 验签是否通过
     */
    public static boolean verifyWithCertificate(String certText, String plainText, String signature) {
        X509Certificate certificate;
        try {
            // 解析证书
            byte[] certBytes = Base64Util.decodeUrlSafe(certText);
            CertificateFactory cf = CertificateFactory.getInstance("X.509", "BC");
            certificate = (X509Certificate) cf.generateCertificate(new ByteArrayInputStream(certBytes));
        } catch (Exception e) {
            throw new ToolException(EmojiSymbol.CRYPTO, e);
        }
        // 获取证书中的公钥
        PublicKey publicKey = certificate.getPublicKey();

        // 检查是否为 EC 公钥
        if (!(publicKey instanceof BCECPublicKey ecPublicKey)) {
            throw new ToolException(EmojiSymbol.CRYPTO, "证书中的公钥不是 EC 公钥");
        }

        ECParameterSpec parameterSpec = ecPublicKey.getParameters();

        // 检查是否为 SM2 曲线
        if (!isSM2Curve(parameterSpec)) {
            throw new ToolException(EmojiSymbol.CRYPTO, "证书中的公钥不是 SM2 曲线");
        }

        // 转换为 BC 的参数格式
        ECPublicKeyParameters pubKeyParams = new ECPublicKeyParameters(ecPublicKey.getQ(), domainParams);

        // 初始化验签器
        SM2Signer signer = new SM2Signer();
        signer.init(false, pubKeyParams);

        // 验证签名
        byte[] data = plainText.getBytes(StandardCharsets.UTF_8);
        byte[] signData = Base64Util.decodeUrlSafe(signature);
        signer.update(data, 0, data.length);

        return signer.verifySignature(signData);
    }

    /**
     * 检查是否为 SM2 曲线
     *
     * @param parameterSpec EC 参数规范
     *
     * @return 是否为 SM2 曲线
     */
    private static boolean isSM2Curve(ECParameterSpec parameterSpec) {
        if (parameterSpec == null) {
            return false;
        }

        // 检查曲线参数是否匹配 SM2 标准
        BigInteger curveA = parameterSpec.getCurve().getA().toBigInteger();
        BigInteger curveB = parameterSpec.getCurve().getB().toBigInteger();
        BigInteger order = parameterSpec.getN();

        // SM2 曲线参数
        BigInteger sm2A = new BigInteger("FFFFFFFEFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF00000000FFFFFFFFFFFFFFFC", 16);
        BigInteger sm2B = new BigInteger("28E9FA9E9D9F5E344D5A9E4BCF6509A7F39789F515AB8F92DDBCBD414D940E93", 16);
        BigInteger sm2Order = new BigInteger("FFFFFFFEFFFFFFFFFFFFFFFFFFFFFFFF7203DF6B21C6052B53BBF40939D54123", 16);

        return curveA.equals(sm2A) && curveB.equals(sm2B) && order.equals(sm2Order);
    }

    /**
     * SM2 密钥对
     *
     * @param privateKey 私钥（Base64 URL 安全编码）
     * @param publicKey  公钥（Base64 URL 安全编码）
     */
    public record SM2KeyPair(String privateKey, String publicKey) {
    }
}
