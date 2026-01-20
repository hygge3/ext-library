package ext.library.apicrypto.enums;

import ext.library.apicrypto.strategy.AESStrategy;
import ext.library.apicrypto.strategy.Base64Strategy;
import ext.library.apicrypto.strategy.CryptoStrategy;
import ext.library.apicrypto.strategy.RSAStrategy;
import ext.library.apicrypto.strategy.SM2Strategy;
import ext.library.apicrypto.strategy.SM4Strategy;

/**
 * 加密算法枚举
 * <p>
 * 定义支持的加密算法类型，每个枚举值关联对应的加密策略实现。
 *
 * @since 4.0.0
 */
public enum Algorithm {

    /**
     * RSA 非对称加密算法
     */
    RSA(new RSAStrategy()),

    /**
     * SM2 国密非对称加密算法
     */
    SM2(new SM2Strategy()),

    /**
     * AES 对称加密算法
     */
    AES(new AESStrategy()),

    /**
     * SM4 国密对称加密算法
     */
    SM4(new SM4Strategy()),

    /**
     * Base64 编码（非加密算法，仅用于编解码）
     */
    BASE64(new Base64Strategy());

    private final CryptoStrategy cryptoStrategy;

    Algorithm(CryptoStrategy cryptoStrategy) {
        this.cryptoStrategy = cryptoStrategy;
    }

    /**
     * 获取加密策略实现
     *
     * @return 对应的加密策略
     */
    public CryptoStrategy getCryptoStrategy() {
        return cryptoStrategy;
    }
}
