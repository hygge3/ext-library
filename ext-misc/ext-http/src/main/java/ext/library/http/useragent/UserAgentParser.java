package ext.library.http.useragent;

import ext.library.tool.util.ObjectUtil;

/**
 * User-Agent 解析器
 * <p>
 * 负责将 User-Agent 字符串解析为结构化的 {@link UserAgent} 对象。
 * </p>
 *
 * @since 1.0.0
 */
public final class UserAgentParser {

    private UserAgentParser() {
    }

    /**
     * 解析 User-Agent 字符串
     *
     * @param userAgentString User-Agent 字符串
     * @return 解析结果，解析失败返回 null
     */
    public static UserAgent parse(String userAgentString) {
        if (ObjectUtil.isEmpty(userAgentString)) {
            return null;
        }

        UserAgent userAgent = new UserAgent();

        Browser browser = parseBrowser(userAgentString);
        userAgent.setBrowser(browser);
        userAgent.setVersion(browser.getVersion(userAgentString));

        Engine engine = parseEngine(userAgentString);
        userAgent.setEngine(engine);
        userAgent.setEngineVersion(engine.getVersion(userAgentString));

        OS os = parseOS(userAgentString);
        userAgent.setOs(os);
        userAgent.setOsVersion(os.getVersion(userAgentString));

        Platform platform = parsePlatform(userAgentString);
        userAgent.setPlatform(platform);

        if (platform.isMobile() || browser.isMobile()) {
            if (!os.isMacOS()) {
                userAgent.setMobile(true);
            }
        }

        return userAgent;
    }

    private static Browser parseBrowser(String userAgentString) {
        for (Browser browser : Browser.BROWSERS) {
            if (browser.isMatch(userAgentString)) {
                return browser;
            }
        }
        return Browser.UNKNOWN;
    }

    private static Engine parseEngine(String userAgentString) {
        for (Engine engine : Engine.ENGINES) {
            if (engine.isMatch(userAgentString)) {
                return engine;
            }
        }
        return Engine.UNKNOWN;
    }

    private static OS parseOS(String userAgentString) {
        for (OS os : OS.OSES) {
            if (os.isMatch(userAgentString)) {
                return os;
            }
        }
        return OS.UNKNOWN;
    }

    private static Platform parsePlatform(String userAgentString) {
        for (Platform platform : Platform.PLATFORMS) {
            if (platform.isMatch(userAgentString)) {
                return platform;
            }
        }
        return Platform.UNKNOWN;
    }
}
