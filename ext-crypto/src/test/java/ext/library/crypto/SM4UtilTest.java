package ext.library.crypto;

import org.junit.jupiter.api.Test;

import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

class SM4UtilTest {

    @Test
    void genKeyPair() throws NoSuchAlgorithmException, NoSuchProviderException {
        String key = SM4Util.genKey(128);
        String encrypt = SM4Util.encryptByECB(key, "Hello World!");
        String decrypt = SM4Util.decryptByECB(key, encrypt);
        System.out.println(key);
        System.out.println(encrypt);
        System.out.println(decrypt);
    }
}