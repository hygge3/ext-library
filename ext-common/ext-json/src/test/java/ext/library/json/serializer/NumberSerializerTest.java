package ext.library.json.serializer;

import ext.library.json.util.JsonUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 数字序列化器测试
 */
@DisplayName("数字序列化器测试")
class NumberSerializerTest {

    private static final long MAX_SAFE = 9007199254740991L;
    private static final long MIN_SAFE = -9007199254740991L;

    // ========== BigNumberSerializer ==========

    @Test
    @DisplayName("安全范围内的整数输出为数字")
    void testSafeIntegerAsNumber() {
        assertEquals("123", JsonUtil.toJson(123L));
        assertEquals("0", JsonUtil.toJson(0L));
    }

    @Test
    @DisplayName("边界值 JS 可精确表示，输出为数字")
    void testBoundaryIsNumber() {
        assertEquals(String.valueOf(MAX_SAFE), JsonUtil.toJson(MAX_SAFE));
        assertEquals(String.valueOf(MIN_SAFE), JsonUtil.toJson(MIN_SAFE));
    }

    @Test
    @DisplayName("超出安全范围输出为字符串")
    void testUnsafeAsString() {
        assertEquals("\"9007199254740992\"", JsonUtil.toJson(MAX_SAFE + 1));
        assertEquals("\"-9007199254740992\"", JsonUtil.toJson(MIN_SAFE - 1));
    }

    @Test
    @DisplayName("超出 long 范围的大整数必须输出为字符串，不能被截断")
    void testBigIntegerNoTruncation() {
        // 2^64 + 15：longValue() 截断后会落回安全范围，必须靠 longValueExact 拦截
        BigInteger big = BigInteger.TWO.pow(64).add(BigInteger.valueOf(15));
        long truncated = big.longValue();

        String json = JsonUtil.toJson(big);

        assertEquals("\"" + big + "\"", json, "大整数应原样输出为字符串");
        assertTrue(Math.abs(truncated) <= MAX_SAFE,
                "前提校验：截断值确实落在安全范围内，否则本用例失去意义");
    }

    @Test
    @DisplayName("long 范围内的 BigInteger 按安全区间处理")
    void testBigIntegerInRange() {
        assertEquals("100", JsonUtil.toJson(BigInteger.valueOf(100)));
    }

    // ========== BigDecimalPlainSerializer ==========

    @Test
    @DisplayName("BigDecimal 输出为 JSON 数字而非字符串")
    void testBigDecimalIsNumber() {
        String json = JsonUtil.toJson(new BigDecimal("123.456"));

        assertEquals("123.456", json);
        assertTrue(JsonUtil.isValidJson(json));
    }

    @Test
    @DisplayName("BigDecimal 不使用科学计数法")
    void testBigDecimalNoScientificNotation() {
        BigDecimal tiny = new BigDecimal("0.00000001");

        String json = JsonUtil.toJson(tiny);

        assertEquals("0.00000001", json, "不应输出为 1E-8");
    }

    @Test
    @DisplayName("BigDecimal 保留 scale，不做 stripTrailingZeros")
    void testBigDecimalKeepsScale() {
        String json = JsonUtil.toJson(new BigDecimal("1.10"));

        assertEquals("1.10", json, "金额 scale 不应被抹掉");
    }

    @Test
    @DisplayName("对象中的 BigDecimal 字段也是数字")
    void testBigDecimalField() {
        String json = JsonUtil.toJson(new Holder(new BigDecimal("9999.9999999999")));

        assertTrue(json.contains("\"amount\":9999.9999999999"), "字段应为数字：" + json);
    }

    static class Holder {
        public BigDecimal amount;

        Holder(BigDecimal amount) {
            this.amount = amount;
        }
    }

}
