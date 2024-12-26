package ext.library.security.util.useragent;

import lombok.experimental.UtilityClass;

/**
 * User-Agent 工具类
 */
@UtilityClass
public class UserAgentUtil {

    /**
     * 解析 User-Agent
     *
     * @param userAgentString User-Agent 字符串
     * @return {@link UserAgent}
     */
    public UserAgent parse(String userAgentString) {
        return UserAgentParser.parse(userAgentString);
    }

}
