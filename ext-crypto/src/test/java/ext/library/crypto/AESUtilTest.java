package ext.library.crypto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class AESUtilTest {

    @Test
    void encryptAndDecrypt() {
        String password = AESUtil.generatePassword();
        String salt = AESUtil.generateSalt();
        String plainText = "Hello World!";

        String encrypted = AESUtil.encrypt(password, salt, plainText);
        String decrypted = AESUtil.decrypt(password, salt, encrypted);

        assertEquals(plainText, decrypted);
    }

    @Test
    void sameInputProducesDifferentCiphertext() {
        String password = AESUtil.generatePassword();
        String salt = AESUtil.generateSalt();
        String plainText = "Same data";

        // AES-GCM 使用随机 IV，相同输入应产生不同密文
        String encrypted1 = AESUtil.encrypt(password, salt, plainText);
        String encrypted2 = AESUtil.encrypt(password, salt, plainText);

        assertNotEquals(encrypted1, encrypted2);

        // 但两者都能正确解密
        assertEquals(plainText, AESUtil.decrypt(password, salt, encrypted1));
        assertEquals(plainText, AESUtil.decrypt(password, salt, encrypted2));
    }

    @Test
    void plainTextPasswordAndSaltWork() {
        // 口令和盐值是普通单词（非 Hex）也必须能用，否则首个加密请求就会抛 Non-hex character
        String password = "ext-example-share-sercet-32bytes";
        String salt = "ext-example-salt";

        String encrypted = AESUtil.encrypt(password, salt, "Hello World!");

        assertEquals("Hello World!", AESUtil.decrypt(password, salt, encrypted));
    }

    @Test
    void generatePasswordProducesValidLength() {
        String password = AESUtil.generatePassword();
        // 32 字节 Base64 URL 编码约 43 字符
        assert password.length() >= 40;
    }

    @Test
    void generateSaltProducesHexString() {
        String salt = AESUtil.generateSalt();
        // 16 字节 Hex 编码 = 32 字符
        assertEquals(32, salt.length());
        // 验证是有效的 Hex 字符串
        assert salt.matches("[0-9a-f]+");
    }
}
