package ext.library.http.useragent;

/**
 * User-Agent 工具类
 */
public class UserAgentUtil {

    /**
     * 解析 User-Agent
     *
     * @param userAgentString User-Agent 字符串
     *
     * @return {@link UserAgent}
     */
    public static UserAgent parse(String userAgentString) {
        return UserAgentParser.parse(userAgentString);
    }

}