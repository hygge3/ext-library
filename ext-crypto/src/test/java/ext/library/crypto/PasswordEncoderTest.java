package ext.library.crypto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PasswordEncoderTest {

    @Test
    void encodeAndMatch() {
        String rawPassword = "mySecretPassword";

        String encoded = PasswordEncoder.encode(rawPassword);

        assertNotNull(encoded);
        assertTrue(encoded.startsWith("{bcrypt}"));
        assertTrue(PasswordEncoder.matches(rawPassword, encoded));
        assertFalse(PasswordEncoder.matches("wrongPassword", encoded));
    }

    @Test
    void samePasswordProducesDifferentHash() {
        String rawPassword = "password123";

        String encoded1 = PasswordEncoder.encode(rawPassword);
        String encoded2 = PasswordEncoder.encode(rawPassword);

        // BCrypt 使用随机盐，相同密码产生不同哈希
        assertNotEquals(encoded1, encoded2);

        // 但两者都能验证
        assertTrue(PasswordEncoder.matches(rawPassword, encoded1));
        assertTrue(PasswordEncoder.matches(rawPassword, encoded2));
    }

    @Test
    void upgradeEncodingReturnsFalseForBcrypt() {
        String encoded = PasswordEncoder.encode("password");

        // bcrypt 是当前默认算法，不需要升级
        assertFalse(PasswordEncoder.upgradeEncoding(encoded));
    }

    @Test
    void getEncoderReturnsNonNull() {
        assertNotNull(PasswordEncoder.getEncoder());
    }
}
