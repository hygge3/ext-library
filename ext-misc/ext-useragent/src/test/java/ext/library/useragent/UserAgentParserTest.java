package ext.library.useragent;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("UserAgentParser 解析测试")
class UserAgentParserTest {

    @Nested
    @DisplayName("桌面浏览器解析")
    class DesktopBrowserTests {

        @Test
        @DisplayName("解析 Chrome 浏览器")
        void parseChrome() {
            String ua = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36";
            UserAgent result = UserAgentParser.parse(ua);

            assertNotNull(result);
            assertEquals("Chrome", result.getBrowser().getName());
            assertEquals("120.0.0.0", result.getVersion());
            assertEquals("Windows 10 or Windows Server 2016", result.getOs().getName());
            assertEquals("Webkit", result.getEngine().getName());
            assertFalse(result.isMobile());
        }

        @Test
        @DisplayName("解析 Firefox 浏览器")
        void parseFirefox() {
            String ua = "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:121.0) Gecko/20100101 Firefox/121.0";
            UserAgent result = UserAgentParser.parse(ua);

            assertNotNull(result);
            assertEquals("Firefox", result.getBrowser().getName());
            assertTrue(result.getVersion().startsWith("121"));
            assertEquals("Gecko", result.getEngine().getName());
            assertFalse(result.isMobile());
        }

        @Test
        @DisplayName("解析 Edge 浏览器")
        void parseEdge() {
            String ua = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36 Edg/120.0.0.0";
            UserAgent result = UserAgentParser.parse(ua);

            assertNotNull(result);
            assertEquals("MSEdge", result.getBrowser().getName());
            assertEquals("120.0.0.0", result.getVersion());
            assertFalse(result.isMobile());
        }

        @Test
        @DisplayName("解析 Safari 浏览器 (macOS)")
        void parseSafariMac() {
            String ua = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.0 Safari/605.1.15";
            UserAgent result = UserAgentParser.parse(ua);

            assertNotNull(result);
            assertEquals("Safari", result.getBrowser().getName());
            assertEquals("17.0", result.getVersion());
            assertEquals("OSX", result.getOs().getName());
            assertTrue(result.getOs().isMacOS());
            assertFalse(result.isMobile());
        }
    }

    @Nested
    @DisplayName("移动端浏览器解析")
    class MobileBrowserTests {

        @Test
        @DisplayName("解析 iPhone Safari")
        void parseIPhoneSafari() {
            String ua = "Mozilla/5.0 (iPhone; CPU iPhone OS 17_0 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.0 Mobile/15E148 Safari/604.1";
            UserAgent result = UserAgentParser.parse(ua);

            assertNotNull(result);
            assertEquals("Safari", result.getBrowser().getName());
            assertEquals("iPhone", result.getOs().getName());
            assertEquals("iPhone", result.getPlatform().getName());
            assertTrue(result.getPlatform().isIos());
            assertTrue(result.isMobile());
        }

        @Test
        @DisplayName("解析 iPad Safari")
        void parseIPadSafari() {
            String ua = "Mozilla/5.0 (iPad; CPU OS 17_0 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.0 Mobile/15E148 Safari/604.1";
            UserAgent result = UserAgentParser.parse(ua);

            assertNotNull(result);
            assertEquals("iPad", result.getOs().getName());
            assertEquals("iPad", result.getPlatform().getName());
            assertTrue(result.getPlatform().isIPad());
            assertTrue(result.getPlatform().isIos());
            assertTrue(result.isMobile());
        }

        @Test
        @DisplayName("解析 Android Chrome")
        void parseAndroidChrome() {
            String ua = "Mozilla/5.0 (Linux; Android 14; SM-G998B) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36";
            UserAgent result = UserAgentParser.parse(ua);

            assertNotNull(result);
            assertEquals("Chrome", result.getBrowser().getName());
            assertEquals("Android", result.getOs().getName());
            assertEquals("Android", result.getPlatform().getName());
            assertTrue(result.getPlatform().isAndroid());
            assertTrue(result.isMobile());
        }
    }

    @Nested
    @DisplayName("国产浏览器/应用解析")
    class ChineseBrowserTests {

        @Test
        @DisplayName("解析微信浏览器")
        void parseWechat() {
            String ua = "Mozilla/5.0 (Linux; Android 14; SM-G998B Build/UP1A.231005.007) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/116.0.0.0 Mobile Safari/537.36 MicroMessenger/8.0.43.2480";
            UserAgent result = UserAgentParser.parse(ua);

            assertNotNull(result);
            assertEquals("MicroMessenger", result.getBrowser().getName());
            assertTrue(result.getBrowser().isMobile());
            assertTrue(result.isMobile());
        }

        @Test
        @DisplayName("解析钉钉")
        void parseDingTalk() {
            String ua = "Mozilla/5.0 (Linux; U; Android 14; zh-CN; SM-G998B Build/UP1A.231005.007) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/100.0.4896.127 Mobile Safari/537.36 AliApp(DingTalk/7.1.0)";
            UserAgent result = UserAgentParser.parse(ua);

            assertNotNull(result);
            assertEquals("DingTalk", result.getBrowser().getName());
            assertEquals("7.1.0", result.getVersion());
            assertTrue(result.getBrowser().isMobile());
        }

        @Test
        @DisplayName("解析 QQ 浏览器")
        void parseQQBrowser() {
            String ua = "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/70.0.3538.25 Safari/537.36 QQBrowser/12.0.5526.400";
            UserAgent result = UserAgentParser.parse(ua);

            assertNotNull(result);
            assertEquals("QQBrowser", result.getBrowser().getName());
            assertEquals("12.0.5526.400", result.getVersion());
        }

        @Test
        @DisplayName("解析 UC 浏览器")
        void parseUCBrowser() {
            String ua = "Mozilla/5.0 (Linux; U; Android 14; zh-CN; SM-G998B) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/100.0.4896.127 UCBrowser/15.0.0.1010 Mobile Safari/537.36";
            UserAgent result = UserAgentParser.parse(ua);

            assertNotNull(result);
            assertEquals("UCBrowser", result.getBrowser().getName());
            assertEquals("15.0.0.1010", result.getVersion());
        }
    }

    @Nested
    @DisplayName("操作系统解析")
    class OSTests {

        @Test
        @DisplayName("解析 Windows 版本")
        void parseWindowsVersions() {
            // Windows 10
            UserAgent win10 = UserAgentParser.parse("Mozilla/5.0 (Windows NT 10.0; Win64; x64) Chrome/120.0.0.0");
            assertNotNull(win10);
            assertEquals("Windows 10 or Windows Server 2016", win10.getOs().getName());

            // Windows 7
            UserAgent win7 = UserAgentParser.parse("Mozilla/5.0 (Windows NT 6.1; Win64; x64) Chrome/120.0.0.0");
            assertNotNull(win7);
            assertEquals("Windows 7 or Windows Server 2008R2", win7.getOs().getName());
        }

        @Test
        @DisplayName("解析 Linux")
        void parseLinux() {
            String ua = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36";
            UserAgent result = UserAgentParser.parse(ua);

            assertNotNull(result);
            assertEquals("Linux", result.getOs().getName());
            assertEquals("Linux", result.getPlatform().getName());
        }
    }

    @Nested
    @DisplayName("边界情况测试")
    class EdgeCaseTests {

        @Test
        @DisplayName("空字符串返回 null")
        void parseEmptyString() {
            assertNull(UserAgentParser.parse(""));
            assertNull(UserAgentParser.parse(null));
        }

        @Test
        @DisplayName("未知 User-Agent")
        void parseUnknown() {
            String ua = "SomeRandomBot/1.0";
            UserAgent result = UserAgentParser.parse(ua);

            assertNotNull(result);
            assertTrue(result.getBrowser().isUnknown());
            assertTrue(result.getOs().isUnknown());
        }

        @Test
        @DisplayName("macOS Safari 不应标记为移动设备")
        void macOsSafariNotMobile() {
            String ua = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.0 Safari/605.1.15";
            UserAgent result = UserAgentParser.parse(ua);

            assertNotNull(result);
            assertFalse(result.isMobile());
            assertTrue(result.getOs().isMacOS());
        }
    }

    @Nested
    @DisplayName("平台判断测试")
    class PlatformTests {

        @Test
        @DisplayName("iOS 平台判断")
        void testIosPlatform() {
            // iPhone
            UserAgent iphone = UserAgentParser.parse("Mozilla/5.0 (iPhone; CPU iPhone OS 17_0 like Mac OS X)");
            assertNotNull(iphone);
            assertTrue(iphone.getPlatform().isIos());
            assertTrue(iphone.getPlatform().isIPhoneOrIPod());

            // iPad
            UserAgent ipad = UserAgentParser.parse("Mozilla/5.0 (iPad; CPU OS 17_0 like Mac OS X)");
            assertNotNull(ipad);
            assertTrue(ipad.getPlatform().isIos());
            assertTrue(ipad.getPlatform().isIPad());
        }

        @Test
        @DisplayName("Android 平台判断")
        void testAndroidPlatform() {
            UserAgent android = UserAgentParser.parse("Mozilla/5.0 (Linux; Android 14; Pixel 8)");
            assertNotNull(android);
            assertTrue(android.getPlatform().isAndroid());
            assertTrue(android.getPlatform().isMobile());
        }
    }
}
