package ext.library.tool.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("CalcUtil 工具类测试")
public class CalcUtilTest {

    @Test
    @DisplayName("测试 add 方法")
    void add() {
        // 测试两个整数相加
        assertEquals(new BigDecimal("3"), CalcUtil.add(1, 2), "1 + 2 应该等于 3");

        // 测试多个数字相加
        assertEquals(new BigDecimal("10"), CalcUtil.add(1, 2, 3, 4), "1 + 2 + 3 + 4 应该等于 10");

        // 测试小数相加
        assertEquals(new BigDecimal("3.8"), CalcUtil.add(1.5, 2.3), "1.5 + 2.3 应该等于 3.8");

        // 测试负数相加
        assertEquals(new BigDecimal("4"), CalcUtil.add(-1, 5), "-1 + 5 应该等于 4");

        // 测试零值相加
        assertEquals(new BigDecimal("5"), CalcUtil.add(0, 5), "0 + 5 应该等于 5");

        // 测试字符串数字相加
        assertEquals(new BigDecimal("4.0"), CalcUtil.add("1.5", "2.5"), "'1.5' + '2.5' 应该等于 4.0");

        // 测试字符串和数字混合相加
        assertEquals(new BigDecimal("3.5"), CalcUtil.add("1.5", 2), "'1.5' + 2 应该等于 3.5");

        // 测试两个 BigDecimal 相加
        assertEquals(new BigDecimal("30.579"), CalcUtil.add(new BigDecimal("10.123"), new BigDecimal("20.456")), "10.123 + 20.456 应该等于 30.579");

        // 单个参数
        assertEquals(new BigDecimal("5"), CalcUtil.add(5), "只有一个参数 5 时，结果应该是 5");

        // 精度处理
        assertEquals(new BigDecimal("0.3"), CalcUtil.add(0.1, 0.2), "0.1 + 0.2 应该精确等于 0.3");
    }

    @Test
    void sub() {
    }

    @Test
    void mul() {
    }

    @Test
    void div() {
    }

    @Test
    void round() {
    }

    @Test
    void percentage() {
    }

    @Test
    void centToYuan() {
    }

    @Test
    void yuanToCent() {
    }
}