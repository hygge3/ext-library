package ext.library.crypto;

import com.google.common.collect.Maps;
import ext.library.tool.constant.Holder;
import ext.library.tool.core.Exceptions;
import ext.library.tool.holder.Lazy;
import ext.library.tool.util.Base64Util;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.asn1.gm.GMNamedCurves;
import org.bouncycastle.asn1.gm.GMObjectIdentifiers;
import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.crypto.engines.SM2Engine;
import org.bouncycastle.crypto.params.ECNamedDomainParameters;
import org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import org.bouncycastle.crypto.params.ECPublicKeyParameters;
import org.bouncycastle.crypto.params.ParametersWithRandom;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPrivateKey;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPublicKey;
import org.bouncycastle.jcajce.provider.asymmetric.ec.KeyPairGeneratorSpi;
import org.bouncycastle.jcajce.provider.asymmetric.x509.CertificateFactory;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jce.spec.ECParameterSpec;
import org.bouncycastle.jce.spec.ECPrivateKeySpec;
import org.bouncycastle.jce.spec.ECPublicKeySpec;
import org.bouncycastle.math.ec.ECPoint;

import java.io.ByteArrayInputStream;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.security.Signature;
import java.security.cert.X509Certificate;
import java.util.Map;
import java.util.Objects;

@Slf4j
@UtilityClass
public class SM2Util {
    private static final String ALGO = "EC";

    private static final Lazy<BouncyCastleProvider> PROVIDER = Lazy.of(() -> {
        BouncyCastleProvider provider = new BouncyCastleProvider();
        if (Objects.isNull(Security.getProvider(BouncyCastleProvider.PROVIDER_NAME))) {
            Security.addProvider(provider);
        }
        return new BouncyCastleProvider();
    });

    /**
     * 获取 SM2 密钥对
     * BC 库使用的公钥=64 个字节 +1 个字节（04 标志位），BC 库使用的私钥=32 个字节
     * SM2 秘钥的组成部分有 私钥 D、公钥 X、公钥 Y , 他们都可以用长度为 64 的 16 进制的 HEX 串表示，
     * SM2 公钥并不是直接由 X+Y 表示 , 而是额外添加了一个头，当启用压缩时：公钥=有头 + 公钥 X，即省略了公钥 Y 的部分
     *
     * @param compressed 是否压缩公钥（加密解密都使用 BC 库才能使用压缩）
     *
     * @return {@link Map.Entry }<{@link String }, {@link String }> 密钥对：{公钥：私钥}
     */
    public Map.Entry<String, String> genKeyPair(boolean compressed) {
        // 1.创建密钥生成器
        KeyPairGeneratorSpi.EC spi = new KeyPairGeneratorSpi.EC();
        // 获取一条 SM2 曲线参数
        X9ECParameters parameters = GMNamedCurves.getByOID(GMObjectIdentifiers.sm2p256v1);
        // 构造 spec 参数
        ECParameterSpec parameterSpec = new ECParameterSpec(parameters.getCurve(), parameters.getG(), parameters.getN());
        // 2.初始化生成器，带上随机数
        try {
            spi.initialize(parameterSpec, Holder.SECURE_RANDOM);
        } catch (InvalidAlgorithmParameterException e) {
            log.error("[🔐] 生成 SM2 密钥对失败", e);
            throw Exceptions.unchecked(e);
        }
        // 3.生成密钥对
        KeyPair asymmetricCipherKeyPair = spi.generateKeyPair();
        // 把公钥放入 map 中，默认压缩公钥
        // 公钥前面的 02 或者 03 表示是压缩公钥，04 表示未压缩公钥，04 的时候，可以去掉前面的 04
        BCECPublicKey publicKeyParameters = (BCECPublicKey) asymmetricCipherKeyPair.getPublic();
        ECPoint ecPoint = publicKeyParameters.getQ();
        byte[] publicKey = ecPoint.getEncoded(compressed);
        // 把私钥放入 map 中
        BCECPrivateKey privateKeyParameters = (BCECPrivateKey) asymmetricCipherKeyPair.getPrivate();
        BigInteger intPrivateKey = privateKeyParameters.getD();
        return Maps.immutableEntry(Base64Util.encodeToStr(publicKey), Base64Util.encodeToStr(intPrivateKey.toByteArray()));
    }

    /**
     * 转换为公钥格式
     *
     * @param publicKey 公钥字符串
     *
     * @return {@link byte[] } 公钥
     */
    private byte[] castPublicKey(String publicKey) {
        return Base64Util.decode(publicKey);
    }

    /**
     * 转换为私钥格式
     *
     * @param privateKey 私钥字符串
     *
     * @return {@link BigInteger } 私钥
     */
    private BigInteger castPrivateKey(String privateKey) {
        return new BigInteger(Base64Util.decodeToStr(privateKey));
    }

    /**
     * SM2 加密算法
     *
     * @param publicKey 公钥字符串
     * @param plainText 明文
     *
     * @return 密文，BC 库产生的密文带由 04 标识符，与非 BC 库对接时需要去掉开头的 04
     */
    public static String encrypt(String publicKey, String plainText) {
        byte[] bytes = plainText.getBytes(StandardCharsets.UTF_8);
        // 获取一条 SM2 曲线参数
        X9ECParameters parameters = GMNamedCurves.getByOID(GMObjectIdentifiers.sm2p256v1);
        // 构造 ECC 算法参数，曲线方程、椭圆曲线 G 点、大整数 N
        ECNamedDomainParameters namedDomainParameters = new ECNamedDomainParameters(GMObjectIdentifiers.sm2p256v1, parameters.getCurve(), parameters.getG(), parameters.getN());
        // 提取公钥点
        ECPoint pukPoint = parameters.getCurve().decodePoint(castPublicKey(publicKey));
        // 公钥前面的 02 或者 03 表示是压缩公钥，04 表示未压缩公钥，04 的时候，可以去掉前面的 04
        ECPublicKeyParameters publicKeyParameters = new ECPublicKeyParameters(pukPoint, namedDomainParameters);
        SM2Engine sm2Engine = new SM2Engine(SM2Engine.Mode.C1C3C2);
        // 设置 sm2 为加密模式
        sm2Engine.init(true, new ParametersWithRandom(publicKeyParameters, Holder.SECURE_RANDOM));
        final byte[] encrypt;
        try {
            encrypt = sm2Engine.processBlock(bytes, 0, bytes.length);
        } catch (InvalidCipherTextException e) {
            log.error("[🔐] SM2 加密失败", e);
            throw Exceptions.unchecked(e);
        }
        return Base64Util.encodeToStr(encrypt);
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
    public String decrypt(String privateKey, String cipherText) {
        byte[] cipherBytes = Base64Util.decode(cipherText);
        // 获取一条 SM2 曲线参数
        X9ECParameters parameters = GMNamedCurves.getByOID(GMObjectIdentifiers.sm2p256v1);
        // 构造 ECC 算法参数，曲线方程、椭圆曲线 G 点、大整数 N
        ECNamedDomainParameters namedDomainParameters = new ECNamedDomainParameters(GMObjectIdentifiers.sm2p256v1, parameters.getCurve(), parameters.getG(), parameters.getN());
        ECPrivateKeyParameters privateKeyParameters = new ECPrivateKeyParameters(castPrivateKey(privateKey), namedDomainParameters);
        SM2Engine sm2Engine = new SM2Engine(SM2Engine.Mode.C1C3C2);
        // 设置 sm2 为解密模式
        sm2Engine.init(false, privateKeyParameters);
        // 使用 BC 库加解密时密文以 04 开头，传入的密文前面没有 04 则补上
        byte[] plainBytes;
        try {
            if (cipherBytes[0] == 0x04) {
                plainBytes = sm2Engine.processBlock(cipherBytes, 0, cipherBytes.length);
            } else {
                byte[] bytes = new byte[cipherBytes.length + 1];
                bytes[0] = 0x04;
                System.arraycopy(cipherBytes, 0, bytes, 1, cipherBytes.length);
                plainBytes = sm2Engine.processBlock(bytes, 0, bytes.length);
            }
        } catch (Exception e) {
            log.error("[🔐] SM2 解密失败", e);
            throw Exceptions.unchecked(e);
        }
        return new String(plainBytes, StandardCharsets.UTF_8);
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
    public String sign(String privateKey, String plainText) {
        X9ECParameters parameters = GMNamedCurves.getByOID(GMObjectIdentifiers.sm2p256v1);
        ECParameterSpec parameterSpec = new ECParameterSpec(parameters.getCurve(), parameters.getG(), parameters.getN());
        ECPrivateKeySpec privateKeySpec = new ECPrivateKeySpec(castPrivateKey(privateKey), parameterSpec);
        PrivateKey bcecPrivateKey = new BCECPrivateKey(ALGO, privateKeySpec, BouncyCastleProvider.CONFIGURATION);
        try {
            // 创建签名对象
            Signature signature = Signature.getInstance(GMObjectIdentifiers.sm2sign_with_sm3.toString(), PROVIDER.get());
            // 初始化为签名状态
            signature.initSign(bcecPrivateKey);
            // 传入签名字节
            signature.update(plainText.getBytes(StandardCharsets.UTF_8));
            // 签名
            return Base64Util.encodeToStr(signature.sign());
        } catch (Exception e) {
            log.error("[🔐] SM2 签名失败", e);
            throw Exceptions.unchecked(e);
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
    public boolean verify(String publicKey, String plainText, String sign) {
        X9ECParameters parameters = GMNamedCurves.getByOID(GMObjectIdentifiers.sm2p256v1);
        ECParameterSpec parameterSpec = new ECParameterSpec(parameters.getCurve(), parameters.getG(), parameters.getN());
        ECPoint ecPoint = parameters.getCurve().decodePoint(castPublicKey(publicKey));
        ECPublicKeySpec publicKeySpec = new ECPublicKeySpec(ecPoint, parameterSpec);
        PublicKey bcecPublicKey = new BCECPublicKey(ALGO, publicKeySpec, BouncyCastleProvider.CONFIGURATION);
        try {
            // 创建签名对象
            Signature signature = Signature.getInstance(GMObjectIdentifiers.sm2sign_with_sm3.toString(), PROVIDER.get());
            // 初始化为验签状态
            signature.initVerify(bcecPublicKey);
            signature.update(plainText.getBytes(StandardCharsets.UTF_8));
            return signature.verify(Base64Util.decode(sign));
        } catch (Exception e) {
            log.error("[🔐] SM2 验签失败", e);
            throw Exceptions.unchecked(e);
        }
    }

    /**
     * 证书验签
     *
     * @param certText  证书串
     * @param plainText 签名原文
     * @param signText  签名产生签名值 此处的签名值实际上就是 R 和 S 的 sequence
     *
     * @return 通过验签
     *
     */
    public static boolean certVerify(String certText, String plainText, String signText) {
        try {
            // 解析证书
            CertificateFactory factory = new CertificateFactory();
            X509Certificate certificate = (X509Certificate) factory.engineGenerateCertificate(new ByteArrayInputStream(Base64Util.decode(certText)));
            // 验证签名
            Signature signature = Signature.getInstance(certificate.getSigAlgName(), PROVIDER.get());
            signature.initVerify(certificate);
            signature.update(plainText.getBytes(StandardCharsets.UTF_8));
            return signature.verify(Base64Util.decode(signText));
        } catch (Exception e) {
            log.error("[🔐] SM2 证书验签失败", e);
            throw Exceptions.unchecked(e);
        }
    }

}