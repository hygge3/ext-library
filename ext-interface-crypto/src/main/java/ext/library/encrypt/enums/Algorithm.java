package ext.library.encrypt.enums;

import ext.library.encrypt.strategy.AESStrategy;
import ext.library.encrypt.strategy.Base64Strategy;
import ext.library.encrypt.strategy.CryptoStrategy;
import ext.library.encrypt.strategy.DESStrategy;
import ext.library.encrypt.strategy.RSAStrategy;
import ext.library.encrypt.strategy.SM2Strategy;
import ext.library.encrypt.strategy.SM4Strategy;

/**
 * 算法
 */
public enum Algorithm {
    RSA(new RSAStrategy()), SM2(new SM2Strategy()), AES(new AESStrategy()), DES(new DESStrategy()), SM4(new SM4Strategy()), BASE64(new Base64Strategy());
    private final CryptoStrategy cryptoStrategy;

    Algorithm(CryptoStrategy cryptoStrategy) {
        this.cryptoStrategy = cryptoStrategy;
    }

    public CryptoStrategy getCryptoStrategy() {
        return cryptoStrategy;
    }
}