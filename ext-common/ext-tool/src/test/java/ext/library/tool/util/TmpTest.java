package ext.library.tool.util;

import org.junit.jupiter.api.Test;

public class TmpTest {
    @Test
    void test() {
        System.out.println(Base64Util.encodeUrlSafeToStr("单元测试"));
        System.out.println(Base64Util.encodeToStr("单元测试"));
    }
}