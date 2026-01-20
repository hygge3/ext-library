package ext.library.crypto;

import ext.library.tool.util.Base64Util;
import org.junit.jupiter.api.Test;

import java.security.KeyPair;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RSAUtilTest {

    @Test
    void encryptAndDecrypt() {
        KeyPair keyPair = RSAUtil.generateKeyPair();
        String privateKey = Base64Util.encodeUrlSafeToStr(keyPair.getPrivate().getEncoded());
        String publicKey = Base64Util.encodeUrlSafeToStr(keyPair.getPublic().getEncoded());
        String plainText = "Hello World!";

        String encrypted = RSAUtil.encrypt(publicKey, plainText);
        String decrypted = RSAUtil.decrypt(privateKey, encrypted);

        assertEquals(plainText, decrypted);
    }

    @Test
    void signAndVerify() {
        KeyPair keyPair = RSAUtil.generateKeyPair();
        String privateKey = Base64Util.encodeUrlSafeToStr(keyPair.getPrivate().getEncoded());
        String publicKey = Base64Util.encodeUrlSafeToStr(keyPair.getPublic().getEncoded());
        String plainText = "Data to sign";

        String signature = RSAUtil.sign(privateKey, plainText);
        boolean verified = RSAUtil.verify(publicKey, plainText, signature);

        assertTrue(verified);
    }
}
