package ext.library.crypto;

import org.junit.jupiter.api.Test;

class SM2UtilTest {

    @Test
    void encrypt() {
        String[] keyPair = SM2Util.genKeyPair();

        String publicKey = keyPair[1];
        System.out.println(publicKey);
        String privateKey = keyPair[0];
        System.out.println(privateKey);
        String encrypt = SM2Util.encrypt(publicKey, "Hello World!");
        System.out.println(encrypt);
        String decrypt = SM2Util.decrypt(privateKey, encrypt);
        System.out.println(decrypt);
        String sign = SM2Util.sign(privateKey, "Hello World!");
        System.out.println(sign);
        boolean verify = SM2Util.verify(publicKey, "Hello World!", sign);
        System.out.println(verify);
    }
}