package ext.library.crypto;

import ext.library.tool.util.Base64Util;
import org.junit.jupiter.api.Test;

import java.security.KeyPair;

class RSAUtilTest {

    @Test
    void encrypt() {
        KeyPair keyPair = RSAUtil.genKeyPair();
        String privateKey = Base64Util.encodeToStr(keyPair.getPrivate().getEncoded());
        String publicKey = Base64Util.encodeToStr(keyPair.getPublic().getEncoded());
        String encrypt = RSAUtil.encrypt(publicKey, "Hello World!");
        String decrypt = RSAUtil.decrypt(privateKey, encrypt);
        System.out.println(privateKey);
        System.out.println(publicKey);
        System.out.println(encrypt);
        System.out.println(decrypt);
    }
}