package ext.library.security.util.useragent;

import ext.library.tool.$;
import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.Nullable;

/**
 * User-Agent 解析器
 */
@UtilityClass
public class UserAgentParser {

    /**
     * 解析 User-Agent
     *
     * @param userAgentString User-Agent 字符串
     * @return {@link UserAgent}
     */
    @Nullable
    public UserAgent parse(String userAgentString) {
        if ($.isEmpty(userAgentString)) {
            return null;
        }
        final UserAgent userAgent = new UserAgent();

        // 浏览器
        final Browser browser = parseBrowser(userAgentString);
        userAgent.setBrowser(browser);
        userAgent.setVersion(browser.getVersion(userAgentString));

        // 浏览器引擎
        final Engine engine = parseEngine(userAgentString);
        userAgent.setEngine(engine);
        userAgent.setEngineVersion(engine.getVersion(userAgentString));

        // 操作系统
        final OS os = parseOS(userAgentString);
        userAgent.setOs(os);
        userAgent.setOsVersion(os.getVersion(userAgentString));

        // 平台
        final Platform platform = parsePlatform(userAgentString);
        userAgent.setPlatform(platform);

        // issue#IA74K2 MACOS 下的微信不属于移动平台
        if (platform.isMobile() || browser.isMobile()) {
            if (!os.isMacOS()) {
                userAgent.setMobile(true);
            }
        }

        return userAgent;
    }

    /**
     * 解析浏览器类型
     *
     * @param userAgentString User-Agent 字符串
     * @return 浏览器类型
     */
    private Browser parseBrowser(String userAgentString) {
        for (Browser browser : Browser.browers) {
            if (browser.isMatch(userAgentString)) {
                return browser;
            }
        }
        return Browser.Unknown;
    }

    /**
     * 解析引擎类型
     *
     * @param userAgentString User-Agent 字符串
     * @return 引擎类型
     */
    private Engine parseEngine(String userAgentString) {
        for (Engine engine : Engine.engines) {
            if (engine.isMatch(userAgentString)) {
                return engine;
            }
        }
        return Engine.Unknown;
    }

    /**
     * 解析系统类型
     *
     * @param userAgentString User-Agent 字符串
     * @return 系统类型
     */
    private OS parseOS(String userAgentString) {
        for (OS os : OS.oses) {
            if (os.isMatch(userAgentString)) {
                return os;
            }
        }
        return OS.Unknown;
    }

    /**
     * 解析平台类型
     *
     * @param userAgentString User-Agent 字符串
     * @return 平台类型
     */
    private Platform parsePlatform(String userAgentString) {
        for (Platform platform : Platform.platforms) {
            if (platform.isMatch(userAgentString)) {
                return platform;
            }
        }
        return Platform.Unknown;
    }

}
