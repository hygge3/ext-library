package ext.library.crypto;

import org.junit.jupiter.api.Test;

class DESUtilTest {

    @Test
    void encrypt() {
        String key = DESUtil.genKey(56);
        String encrypt = DESUtil.encrypt(key, "Hello World!");
        String decrypt = DESUtil.decrypt(key, encrypt);
        System.out.println(key);
        System.out.println(encrypt);
        System.out.println(decrypt);
    }
}