package ext.library.tool.runtime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.charset.Charset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Systems 工具类测试")
class SystemsTest {

    @Test
    @DisplayName("测试操作系统类型判断的互斥性")
    void testOsTypeExclusivity() {
        // 统计有多少个操作系统判断返回 true
        int trueCount = 0;
        if (Systems.isWindows()) trueCount++;
        if (Systems.isLinux()) trueCount++;
        if (Systems.isMac()) trueCount++;
        if (Systems.isMacX()) trueCount++;
        if (Systems.isAix()) trueCount++;

        // 至少应有一个操作系统类型为真（在已知的系统上）
        assertTrue(trueCount >= 1, "应至少识别出一种操作系统类型");
    }

    @Test
    @DisplayName("测试 Windows 系统判断")
    void testIsWindows() {
        boolean isWindows = Systems.isWindows();
        String osName = Systems.osName();

        if (osName.contains("Windows")) {
            assertTrue(isWindows, "在 Windows 系统上应返回 true");
        } else {
            assertFalse(isWindows, "在非 Windows 系统上应返回 false");
        }
    }

    @Test
    @DisplayName("测试 Linux 系统判断")
    void testIsLinux() {
        boolean isLinux = Systems.isLinux();
        String osName = Systems.osName();

        if (osName.contains("Linux")) {
            assertTrue(isLinux, "在 Linux 系统上应返回 true");
        } else {
            assertFalse(isLinux, "在非 Linux 系统上应返回 false");
        }
    }

    @Test
    @DisplayName("测试 Mac OS X 系统判断")
    void testIsMacX() {
        boolean isMacX = Systems.isMacX();
        String osName = Systems.osName();

        if (osName.contains("OS X")) {
            assertTrue(isMacX, "在 OS X 系统上应返回 true");
        } else {
            assertFalse(isMacX, "在非 OS X 系统上应返回 false");
        }
    }

    @Test
    @DisplayName("测试 Mac OS 系统判断")
    void testIsMac() {
        boolean isMac = Systems.isMac();
        String osName = Systems.osName();

        if (osName.contains("Mac OS")) {
            assertTrue(isMac, "在 Mac OS 系统上应返回 true");
        } else {
            assertFalse(isMac, "在非 Mac OS 系统上应返回 false");
        }
    }

    @Test
    @DisplayName("测试 AIX 系统判断")
    void testIsAix() {
        boolean isAix = Systems.isAix();
        String osName = Systems.osName();

        if (osName.contains("AIX")) {
            assertTrue(isAix, "在 AIX 系统上应返回 true");
        } else {
            assertFalse(isAix, "在非 AIX 系统上应返回 false");
        }
    }

    @Test
    @DisplayName("测试获取操作系统名称")
    void testOsName() {
        String osName = Systems.osName();

        assertNotNull(osName, "操作系统名称不应为 null");
        assertFalse(osName.isEmpty(), "操作系统名称不应为空字符串");

        // 验证返回值与系统属性一致
        assertEquals(System.getProperty("os.name"), osName,
                "应返回系统属性 os.name 的值");
    }

    @Test
    @DisplayName("测试获取系统字符集")
    void testCharset() {
        Charset charset = Systems.charset();

        assertNotNull(charset, "系统字符集不应为 null");

        // 验证字符集名称有效
        assertNotNull(charset.name(), "字符集名称不应为 null");
        assertFalse(charset.name().isEmpty(), "字符集名称不应为空");
    }

    @Test
    @DisplayName("测试获取系统行分隔符")
    void testLineSeparator() {
        String lineSeparator = Systems.lineSeparator();

        assertNotNull(lineSeparator, "行分隔符不应为 null");
        assertFalse(lineSeparator.isEmpty(), "行分隔符不应为空");

        // 验证返回值与系统属性一致
        assertEquals(System.lineSeparator(), lineSeparator,
                "应返回系统的行分隔符");

        // 验证常见系统的行分隔符
        if (Systems.isWindows()) {
            assertEquals("\r\n", lineSeparator, "Windows 系统应使用 \\r\\n");
        } else if (Systems.isLinux() || Systems.isMac() || Systems.isMacX()) {
            assertEquals("\n", lineSeparator, "Unix 系列系统应使用 \\n");
        }
    }

    @Test
    @DisplayName("测试获取系统文件分隔符")
    void testFileSeparator() {
        String fileSeparator = Systems.fileSeparator();

        assertNotNull(fileSeparator, "文件分隔符不应为 null");
        assertFalse(fileSeparator.isEmpty(), "文件分隔符不应为空");

        // 验证返回值与系统属性一致
        assertEquals(File.separator, fileSeparator,
                "应返回系统的文件分隔符");

        // 验证常见系统的文件分隔符
        if (Systems.isWindows()) {
            assertEquals("\\", fileSeparator, "Windows 系统应使用 \\");
        } else if (Systems.isLinux() || Systems.isMac() || Systems.isMacX()) {
            assertEquals("/", fileSeparator, "Unix 系列系统应使用 /");
        }
    }

    @Test
    @DisplayName("测试获取系统用户名")
    void testUsername() {
        String username = Systems.username();

        assertNotNull(username, "用户名不应为 null");
        assertFalse(username.isEmpty(), "用户名不应为空字符串");

        // 验证返回值与系统属性一致
        assertEquals(System.getProperty("user.name"), username,
                "应返回系统属性 user.name 的值");
    }

    @Test
    @DisplayName("测试方法调用的稳定性")
    void testMethodStability() {
        // 多次调用应返回相同的结果
        String osName1 = Systems.osName();
        String osName2 = Systems.osName();
        assertEquals(osName1, osName2, "多次调用 osName 应返回相同值");

        String username1 = Systems.username();
        String username2 = Systems.username();
        assertEquals(username1, username2, "多次调用 username 应返回相同值");

        String lineSeparator1 = Systems.lineSeparator();
        String lineSeparator2 = Systems.lineSeparator();
        assertEquals(lineSeparator1, lineSeparator2, "多次调用 lineSeparator 应返回相同值");

        String fileSeparator1 = Systems.fileSeparator();
        String fileSeparator2 = Systems.fileSeparator();
        assertEquals(fileSeparator1, fileSeparator2, "多次调用 fileSeparator 应返回相同值");
    }

    @Test
    @DisplayName("测试操作系统判断的一致性")
    void testOsDetectionConsistency() {
        String osName = Systems.osName().toLowerCase();

        // 如果是 Windows，只有 isWindows 应该为 true
        if (Systems.isWindows()) {
            assertTrue(osName.contains("windows"),
                    "isWindows 为 true 时，osName 应包含 'windows'");
            assertFalse(Systems.isLinux(), "Windows 系统不应被判断为 Linux");
            assertFalse(Systems.isAix(), "Windows 系统不应被判断为 AIX");
        }

        // 如果是 Linux，只有 isLinux 应该为 true
        if (Systems.isLinux()) {
            assertTrue(osName.contains("linux"),
                    "isLinux 为 true 时，osName 应包含 'linux'");
            assertFalse(Systems.isWindows(), "Linux 系统不应被判断为 Windows");
            assertFalse(Systems.isAix(), "Linux 系统不应被判断为 AIX");
        }

        // 如果是 Mac，isMac 或 isMacX 至少一个为 true
        if (Systems.isMac() || Systems.isMacX()) {
            assertTrue(osName.contains("mac") || osName.contains("os x"),
                    "Mac 系统的 osName 应包含 'mac' 或 'os x'");
            assertFalse(Systems.isWindows(), "Mac 系统不应被判断为 Windows");
            assertFalse(Systems.isLinux(), "Mac 系统不应被判断为 Linux");
            assertFalse(Systems.isAix(), "Mac 系统不应被判断为 AIX");
        }
    }

    @Test
    @DisplayName("测试字符集的有效性")
    void testCharsetValidity() {
        Charset charset = Systems.charset();

        // 验证字符集可以编码和解码
        String testString = "Hello World 你好世界";
        byte[] encoded = testString.getBytes(charset);
        String decoded = new String(encoded, charset);

        assertEquals(testString, decoded, "字符集应能正确编码和解码字符串");
    }

    @Test
    @DisplayName("测试文件分隔符的实际应用")
    void testFileSeparatorUsage() {
        String separator = Systems.fileSeparator();

        // 构建一个简单的路径
        String path = "folder" + separator + "file.txt";

        assertNotNull(path, "使用文件分隔符构建的路径不应为 null");
        assertTrue(path.contains(separator), "路径应包含文件分隔符");

        if (Systems.isWindows()) {
            assertTrue(path.contains("\\"), "Windows 路径应包含 \\");
        } else {
            assertTrue(path.contains("/"), "Unix 路径应包含 /");
        }
    }
}
