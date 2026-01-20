package ext.library.crypto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SM4UtilTest {

    @Test
    void encryptAndDecryptByECB() {
        String key = SM4Util.generateKey(128);
        String plainText = "Hello World!";

        String encrypted = SM4Util.encryptByECB(key, plainText);
        String decrypted = SM4Util.decryptByECB(key, encrypted);

        assertEquals(plainText, decrypted);
    }

    @Test
    void encryptAndDecryptByCBC() {
        String key = SM4Util.generateKey(128);
        String iv = SM4Util.generateKey(128);
        String plainText = "Hello World!";

        String encrypted = SM4Util.encryptByCBC(key, iv, plainText);
        String decrypted = SM4Util.decryptByCBC(key, iv, encrypted);

        assertEquals(plainText, decrypted);
    }
}
