package ext.library.crypto;

import org.junit.jupiter.api.Test;

class AESUtilTest {

    @Test
    void encrypt() {
        String key = AESUtil.genKey(128);
        String encrypt = AESUtil.encrypt(key, "Hello World!", "123456");
        String decrypt = AESUtil.decrypt(key, encrypt, "123456");
        System.out.println(key);
        System.out.println(encrypt);
        System.out.println(decrypt);
    }
}