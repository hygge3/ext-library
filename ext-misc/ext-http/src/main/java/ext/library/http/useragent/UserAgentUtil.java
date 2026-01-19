package ext.library.http.useragent;

/**
 * User-Agent 工具类
 * <p>
 * 提供 User-Agent 解析的便捷入口。
 * </p>
 *
 * @since 1.0.0
 */
public final class UserAgentUtil {

    private UserAgentUtil() {
    }

    /**
     * 解析 User-Agent 字符串
     *
     * @param userAgentString User-Agent 字符串
     * @return {@link UserAgent}
     */
    public static UserAgent parse(String userAgentString) {
        return UserAgentParser.parse(userAgentString);
    }
}
