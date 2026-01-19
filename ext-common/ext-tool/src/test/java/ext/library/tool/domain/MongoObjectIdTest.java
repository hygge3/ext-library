package ext.library.tool.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("MongoObjectId 工具类测试")
class MongoObjectIdTest {

    @Test
    @DisplayName("测试生成 ObjectId 字符串")
    void testNext() {
        String objectId = MongoObjectId.next();

        assertNotNull(objectId, "生成的 ObjectId 不应为 null");
        assertEquals(24, objectId.length(), "ObjectId 长度应为 24");
        assertTrue(MongoObjectId.isValid(objectId), "生成的 ObjectId 应该是有效的");
    }

    @Test
    @DisplayName("测试生成 ObjectId 字节数组")
    void testNextBytes() {
        byte[] bytes = MongoObjectId.nextBytes();

        assertNotNull(bytes, "生成的字节数组不应为 null");
        assertEquals(12, bytes.length, "字节数组长度应为 12");
    }

    @Test
    @DisplayName("测试 ObjectId 的唯一性")
    void testUniqueness() {
        Set<String> ids = new HashSet<>();
        int count = 10000;

        for (int i = 0; i < count; i++) {
            String id = MongoObjectId.next();
            assertTrue(ids.add(id), "生成的 ObjectId 应该是唯一的");
        }

        assertEquals(count, ids.size(), "应生成指定数量的唯一 ObjectId");
    }

    @Test
    @DisplayName("测试多线程环境下的 ObjectId 唯一性")
    void testThreadSafety() throws InterruptedException {
        int threadCount = 10;
        int idsPerThread = 1000;
        Set<String> ids = ConcurrentHashMap.newKeySet();
        CountDownLatch latch = new CountDownLatch(threadCount);
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    for (int j = 0; j < idsPerThread; j++) {
                        String id = MongoObjectId.next();
                        assertTrue(ids.add(id), "多线程环境下生成的 ObjectId 应该是唯一的");
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();

        assertEquals(threadCount * idsPerThread, ids.size(),
                "多线程环境下应生成指定数量的唯一 ObjectId");
    }

    @Test
    @DisplayName("测试有效的 ObjectId 字符串验证")
    void testIsValidWithValidId() {
        String validId = MongoObjectId.next();
        assertTrue(MongoObjectId.isValid(validId), "应识别有效的 ObjectId");

        // 测试标准的 24 位十六进制字符串
        assertTrue(MongoObjectId.isValid("507f1f77bcf86cd799439011"),
                "标准的 24 位十六进制字符串应该有效");
        assertTrue(MongoObjectId.isValid("000000000000000000000000"),
                "全 0 的 24 位十六进制字符串应该有效");
        assertTrue(MongoObjectId.isValid("FFFFFFFFFFFFFFFFFFFFFFFF"),
                "全 F 的大写 24 位十六进制字符串应该有效");
        assertTrue(MongoObjectId.isValid("ffffffffffffffffffffffff"),
                "全 f 的小写 24 位十六进制字符串应该有效");
    }

    @Test
    @DisplayName("测试无效的 ObjectId 字符串验证")
    void testIsValidWithInvalidId() {
        // null 值
        assertFalse(MongoObjectId.isValid(null), "null 应该无效");

        // 长度不对
        assertFalse(MongoObjectId.isValid(""), "空字符串应该无效");
        assertFalse(MongoObjectId.isValid("507f1f77bcf86cd79943901"),
                "长度为 23 的字符串应该无效");
        assertFalse(MongoObjectId.isValid("507f1f77bcf86cd7994390111"),
                "长度为 25 的字符串应该无效");

        // 包含非十六进制字符
        assertFalse(MongoObjectId.isValid("507f1f77bcf86cd79943901g"),
                "包含非十六进制字符 'g' 应该无效");
        assertFalse(MongoObjectId.isValid("507f1f77bcf86cd79943901@"),
                "包含特殊字符应该无效");
        assertFalse(MongoObjectId.isValid("507f1f77bcf86cd79943901 "),
                "包含空格应该无效");
    }

    @Test
    @DisplayName("测试字节数组转十六进制字符串")
    void testToHex() {
        byte[] bytes = MongoObjectId.nextBytes();
        String hex = MongoObjectId.toHex(bytes);

        assertNotNull(hex, "转换结果不应为 null");
        assertEquals(24, hex.length(), "十六进制字符串长度应为 24");
        assertTrue(MongoObjectId.isValid(hex), "转换结果应该是有效的 ObjectId");

        // 测试特定字节数组
        byte[] testBytes = new byte[12];
        for (int i = 0; i < 12; i++) {
            testBytes[i] = (byte) i;
        }
        String testHex = MongoObjectId.toHex(testBytes);
        assertEquals("000102030405060708090a0b", testHex,
                "应正确转换字节数组为十六进制字符串");
    }

    @Test
    @DisplayName("测试 toHex 的边界情况")
    void testToHexBoundary() {
        // 测试全 0
        byte[] zeros = new byte[12];
        assertEquals("000000000000000000000000", MongoObjectId.toHex(zeros),
                "全 0 字节数组应转换为 24 个 '0'");

        // 测试全 0xFF
        byte[] maxBytes = new byte[12];
        for (int i = 0; i < 12; i++) {
            maxBytes[i] = (byte) 0xFF;
        }
        assertEquals("ffffffffffffffffffffffff", MongoObjectId.toHex(maxBytes),
                "全 0xFF 字节数组应转换为 24 个 'f'");
    }

    @Test
    @DisplayName("测试 toHex 抛出异常")
    void testToHexThrowsException() {
        // null 参数
        assertThrows(NullPointerException.class, () -> {
            MongoObjectId.toHex(null);
        }, "传入 null 应抛出 NullPointerException");

        // 长度不正确的字节数组
        assertThrows(IllegalArgumentException.class, () -> {
            MongoObjectId.toHex(new byte[11]);
        }, "长度为 11 的字节数组应抛出 IllegalArgumentException");

        assertThrows(IllegalArgumentException.class, () -> {
            MongoObjectId.toHex(new byte[13]);
        }, "长度为 13 的字节数组应抛出 IllegalArgumentException");
    }

    @Test
    @DisplayName("测试十六进制字符串转字节数组")
    void testFromHex() {
        String hex = "507f1f77bcf86cd799439011";
        byte[] bytes = MongoObjectId.fromHex(hex);

        assertNotNull(bytes, "转换结果不应为 null");
        assertEquals(12, bytes.length, "字节数组长度应为 12");

        // 验证往返转换
        String hexAgain = MongoObjectId.toHex(bytes);
        assertEquals(hex, hexAgain, "往返转换应得到相同结果");
    }

    @Test
    @DisplayName("测试 fromHex 和 toHex 的往返转换")
    void testHexRoundTrip() {
        // 多次测试往返转换
        for (int i = 0; i < 100; i++) {
            byte[] originalBytes = MongoObjectId.nextBytes();
            String hex = MongoObjectId.toHex(originalBytes);
            byte[] convertedBytes = MongoObjectId.fromHex(hex);

            assertArrayEquals(originalBytes, convertedBytes,
                    "往返转换应得到相同的字节数组");
        }
    }

    @Test
    @DisplayName("测试 fromHex 处理大小写混合")
    void testFromHexCaseInsensitive() {
        String lowerCase = "507f1f77bcf86cd799439011";
        String upperCase = "507F1F77BCF86CD799439011";
        String mixedCase = "507f1F77BcF86Cd799439011";

        byte[] bytes1 = MongoObjectId.fromHex(lowerCase);
        byte[] bytes2 = MongoObjectId.fromHex(upperCase);
        byte[] bytes3 = MongoObjectId.fromHex(mixedCase);

        assertArrayEquals(bytes1, bytes2, "大小写不同的十六进制字符串应转换为相同的字节数组");
        assertArrayEquals(bytes1, bytes3, "大小写混合的十六进制字符串应转换为相同的字节数组");
    }

    @Test
    @DisplayName("测试 fromHex 抛出异常")
    void testFromHexThrowsException() {
        // 无效的十六进制字符串
        assertThrows(IllegalArgumentException.class, () -> {
            MongoObjectId.fromHex(null);
        }, "传入 null 应抛出 IllegalArgumentException");

        assertThrows(IllegalArgumentException.class, () -> {
            MongoObjectId.fromHex("507f1f77bcf86cd79943901");
        }, "长度不足 24 应抛出 IllegalArgumentException");

        assertThrows(IllegalArgumentException.class, () -> {
            MongoObjectId.fromHex("507f1f77bcf86cd79943901g");
        }, "包含非十六进制字符应抛出 IllegalArgumentException");

        assertThrows(IllegalArgumentException.class, () -> {
            MongoObjectId.fromHex("507f1f77bcf86cd79943901@");
        }, "包含特殊字符应抛出 IllegalArgumentException");
    }

    @Test
    @DisplayName("测试 ObjectId 的时间戳部分")
    void testTimestampPart() throws InterruptedException {
        // 生成第一个 ObjectId
        byte[] bytes1 = MongoObjectId.nextBytes();

        // 等待至少 1 秒
        Thread.sleep(1100);

        // 生成第二个 ObjectId
        byte[] bytes2 = MongoObjectId.nextBytes();

        // 提取时间戳部分（前 4 个字节）
        int timestamp1 = ((bytes1[0] & 0xFF) << 24) |
                ((bytes1[1] & 0xFF) << 16) |
                ((bytes1[2] & 0xFF) << 8) |
                (bytes1[3] & 0xFF);

        int timestamp2 = ((bytes2[0] & 0xFF) << 24) |
                ((bytes2[1] & 0xFF) << 16) |
                ((bytes2[2] & 0xFF) << 8) |
                (bytes2[3] & 0xFF);

        assertTrue(timestamp2 > timestamp1, "后生成的 ObjectId 时间戳应该更大");
        assertTrue(timestamp2 - timestamp1 >= 1, "时间戳差应至少为 1 秒");
    }

    @Test
    @DisplayName("测试 ObjectId 的递增性")
    void testCounterIncrement() {
        byte[] bytes1 = MongoObjectId.nextBytes();
        byte[] bytes2 = MongoObjectId.nextBytes();

        // 提取计数器部分（后 3 个字节）
        int counter1 = ((bytes1[9] & 0xFF) << 16) |
                ((bytes1[10] & 0xFF) << 8) |
                (bytes1[11] & 0xFF);

        int counter2 = ((bytes2[9] & 0xFF) << 16) |
                ((bytes2[10] & 0xFF) << 8) |
                (bytes2[11] & 0xFF);

        // 在短时间内，计数器应该递增（考虑可能的溢出）
        assertTrue(counter2 == (counter1 + 1) || counter2 == 0,
                "计数器应该递增或在溢出后重置为 0");
    }

    @Test
    @DisplayName("测试连续生成的 ObjectId 格式一致性")
    void testConsistentFormat() {
        for (int i = 0; i < 100; i++) {
            String id = MongoObjectId.next();

            assertEquals(24, id.length(), "每个生成的 ObjectId 长度应为 24");
            assertTrue(MongoObjectId.isValid(id), "每个生成的 ObjectId 应该有效");

            // 验证只包含十六进制字符
            assertTrue(id.matches("[0-9a-f]{24}"),
                    "ObjectId 应只包含小写十六进制字符");
        }
    }

    @Test
    @DisplayName("测试 ObjectId 字节数组的结构")
    void testByteStructure() {
        byte[] bytes = MongoObjectId.nextBytes();

        // 验证长度
        assertEquals(12, bytes.length, "字节数组长度应为 12");

        // 前 4 字节是时间戳，应该不全为 0（在合理的时间范围内）
        boolean hasNonZeroTimestamp = bytes[0] != 0 || bytes[1] != 0 ||
                bytes[2] != 0 || bytes[3] != 0;
        assertTrue(hasNonZeroTimestamp, "时间戳部分不应全为 0");
    }
}