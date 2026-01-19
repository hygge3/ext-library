package ext.library.http.useragent;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 浏览器信息
 * <p>
 * 识别浏览器类型、版本及是否为移动浏览器。
 * </p>
 *
 * @since 1.0.0
 */
public class Browser extends UserAgentInfo {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 移动浏览器名称列表
     */
    private static final java.util.Set<String> MOBILE_BROWSERS = java.util.Set.of(
            "PSP", "Yammer Mobile", "Android Browser", "IEMobile",
            "MicroMessenger", "miniProgram", "DingTalk"
    );

    /**
     * 版本解析正则表达式
     */
    private static final String VERSION_REGEX = "[\\/ ]([\\d\\w\\.\\-]+)";

    /**
     * 浏览器定义：名称、匹配正则、版本正则
     */
    private static final String[][] BROWSER_DEFINITIONS = {
            {"wxwork", "wxwork", "wxwork\\/([\\d\\w\\.\\-]+)"},
            {"WindowsWechat", "WindowsWechat", "MicroMessenger" + VERSION_REGEX},
            {"MicroMessenger", "MicroMessenger", VERSION_REGEX},
            {"miniProgram", "miniProgram", VERSION_REGEX},
            {"QQBrowser", "QQBrowser", "QQBrowser\\/([\\d\\w\\.\\-]+)"},
            {"DingTalk-win", "dingtalk-win", "DingTalk\\(([\\d\\w\\.\\-]+)\\)"},
            {"DingTalk", "DingTalk", "AliApp\\(DingTalk\\/([\\d\\w\\.\\-]+)\\)"},
            {"Alipay", "AlipayClient", "AliApp\\(AP\\/([\\d\\w\\.\\-]+)\\)"},
            {"Taobao", "taobao", "AliApp\\(TB\\/([\\d\\w\\.\\-]+)\\)"},
            {"UCBrowser", "UC?Browser", "UC?Browser\\/([\\d\\w\\.\\-]+)"},
            {"MiuiBrowser", "MiuiBrowser|mibrowser", "MiuiBrowser\\/([\\d\\w\\.\\-]+)"},
            {"Quark", "Quark", VERSION_REGEX},
            {"Lenovo", "SLBrowser", "SLBrowser/([\\d\\w\\.\\-]+)"},
            {"MSEdge", "Edge|Edg", "(?:edge|Edg|EdgA)\\/([\\d\\w\\.\\-]+)"},
            {"Chrome", "chrome|(iphone.*crios.*safari)", "(?:Chrome|CriOS)\\/([\\d\\w\\.\\-]+)"},
            {"Firefox", "firefox", VERSION_REGEX},
            {"IEMobile", "iemobile", VERSION_REGEX},
            {"Android Browser", "android", "version\\/([\\d\\w\\.\\-]+)"},
            {"Safari", "safari", "version\\/([\\d\\w\\.\\-]+)"},
            {"Opera", "opera", VERSION_REGEX},
            {"Konqueror", "konqueror", VERSION_REGEX},
            {"PS3", "playstation 3", "([\\d\\w\\.\\-]+)\\)\\s*$"},
            {"PSP", "playstation portable", "([\\d\\w\\.\\-]+)\\)?\\s*$"},
            {"Lotus", "lotus.notes", "Lotus-Notes\\/([\\w.]+)"},
            {"Thunderbird", "thunderbird", VERSION_REGEX},
            {"Netscape", "netscape", VERSION_REGEX},
            {"Seamonkey", "seamonkey", VERSION_REGEX},
            {"Outlook", "microsoft.outlook", VERSION_REGEX},
            {"Evolution", "evolution", VERSION_REGEX},
            {"MSIE", "msie", "msie ([\\d\\w\\.\\-]+)"},
            {"MSIE11", "rv:11", "rv:([\\d\\w\\.\\-]+)"},
            {"Gabble", "Gabble", VERSION_REGEX},
            {"Yammer Desktop", "AdobeAir", "([\\d\\w\\.\\-]+)\\/Yammer"},
            {"Yammer Mobile", "Yammer[\\s]+([\\d\\w\\.\\-]+)", "Yammer[\\s]+([\\d\\w\\.\\-]+)"},
            {"Apache HTTP Client", "Apache\\\\-HttpClient", "Apache\\-HttpClient\\/([\\d\\w\\.\\-]+)"},
            {"BlackBerry", "BlackBerry", "BlackBerry[\\d]+\\/([\\d\\w\\.\\-]+)"},
            {"Baidu", "Baidu", "baiduboxapp\\/([\\d\\w\\.\\-]+)"}
    };

    /**
     * 所有支持的浏览器列表
     */
    public static final List<Browser> BROWSERS;

    static {
        List<Browser> list = new ArrayList<>(BROWSER_DEFINITIONS.length);
        for (String[] def : BROWSER_DEFINITIONS) {
            String name = def[0];
            String regex = def[1];
            String versionRegex = def[2];
            if (!VERSION_REGEX.equals(versionRegex)) {
                versionRegex = name + versionRegex;
            }
            list.add(new Browser(name, regex, versionRegex));
        }
        BROWSERS = List.copyOf(list);
    }

    /**
     * 未知浏览器
     */
    public static final Browser UNKNOWN = new Browser(NAME_UNKNOWN, (String) null, (String) null);

    /**
     * 版本解析模式
     */
    private final Pattern versionPattern;

    /**
     * 构造浏览器
     *
     * @param name         浏览器名称
     * @param regex        匹配正则
     * @param versionRegex 版本解析正则
     */
    private Browser(String name, String regex, String versionRegex) {
        super(name, regex);
        if (versionRegex != null) {
            this.versionPattern = Pattern.compile(versionRegex, Pattern.CASE_INSENSITIVE);
        } else {
            this.versionPattern = null;
        }
    }

    /**
     * 获取浏览器版本
     *
     * @param userAgentString User-Agent 字符串
     * @return 版本号
     */
    public String getVersion(String userAgentString) {
        if (isUnknown() || versionPattern == null) {
            return null;
        }
        Matcher m = versionPattern.matcher(userAgentString);
        if (m.find()) {
            return m.group(1);
        }
        return "";
    }

    /**
     * 是否为移动浏览器
     *
     * @return 是否为移动浏览器
     */
    public boolean isMobile() {
        return MOBILE_BROWSERS.contains(getName());
    }
}
