package ext.library.crypto;

import ext.library.crypto.SM2Util.SM2KeyPair;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SM2UtilTest {

    @Test
    void encryptAndDecrypt() {
        SM2KeyPair keyPair = SM2Util.generateKeyPair();
        String plainText = "Hello World!";

        String encrypted = SM2Util.encrypt(keyPair.publicKey(), plainText);
        String decrypted = SM2Util.decrypt(keyPair.privateKey(), encrypted);

        assertEquals(plainText, decrypted);
    }

    @Test
    void signAndVerify() {
        SM2KeyPair keyPair = SM2Util.generateKeyPair();
        String plainText = "Data to sign";

        String signature = SM2Util.sign(keyPair.privateKey(), plainText);
        boolean verified = SM2Util.verify(keyPair.publicKey(), plainText, signature);

        assertTrue(verified);
    }
}
