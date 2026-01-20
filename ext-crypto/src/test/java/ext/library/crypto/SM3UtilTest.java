package ext.library.crypto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SM3UtilTest {

    @Test
    void hashProducesCorrectLength() {
        String hash = SM3Util.hash("hello world");

        assertNotNull(hash);
        // SM3 输出 256 位 = 64 个十六进制字符
        assertEquals(64, hash.length());
    }

    @Test
    void hashIsDeterministic() {
        String input = "test data";

        String hash1 = SM3Util.hash(input);
        String hash2 = SM3Util.hash(input);

        assertEquals(hash1, hash2);
    }

    @Test
    void differentInputProducesDifferentHash() {
        String hash1 = SM3Util.hash("hello");
        String hash2 = SM3Util.hash("world");

        assertFalse(hash1.equals(hash2));
    }

    @Test
    void verifyReturnsTrueForMatchingHash() {
        String input = "verify test";
        String hash = SM3Util.hash(input);

        assertTrue(SM3Util.verify(input, hash));
        assertFalse(SM3Util.verify("wrong input", hash));
    }

    @Test
    void hmacProducesCorrectLength() {
        String hmac = SM3Util.hmac("secret-key", "message");

        assertNotNull(hmac);
        assertEquals(64, hmac.length());
    }

    @Test
    void hmacIsDeterministic() {
        String key = "secret";
        String message = "data";

        String hmac1 = SM3Util.hmac(key, message);
        String hmac2 = SM3Util.hmac(key, message);

        assertEquals(hmac1, hmac2);
    }

    @Test
    void verifyHmacReturnsTrueForMatchingHmac() {
        String key = "my-key";
        String message = "my-message";
        String hmac = SM3Util.hmac(key, message);

        assertTrue(SM3Util.verifyHmac(key, message, hmac));
        assertFalse(SM3Util.verifyHmac(key, "wrong-message", hmac));
        assertFalse(SM3Util.verifyHmac("wrong-key", message, hmac));
    }

    @Test
    void hashToBytesProducesCorrectLength() {
        byte[] hashBytes = SM3Util.hashToBytes("test".getBytes());

        assertEquals(SM3Util.DIGEST_LENGTH, hashBytes.length);
    }
}
