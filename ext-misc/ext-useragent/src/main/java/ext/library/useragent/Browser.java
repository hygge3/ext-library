package ext.library.useragent;

import java.io.Serial;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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
    private static final Set<String> MOBILE_BROWSERS = Set.of(
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
    public static final List<Browser> BROWSER_LIST;

    static {
        List<Browser> list = new ArrayList<>(BROWSER_DEFINITIONS.length);
        for (String[] def : BROWSER_DEFINITIONS) {
            String name = def[0];
            String regex = def[1];
            String versionRegex = def[2];
            // 当使用通用版本正则时，需要在前面加上浏览器名称来限定匹配
            if (VERSION_REGEX.equals(versionRegex)) {
                versionRegex = name + versionRegex;
            }
            list.add(new Browser(name, regex, versionRegex));
        }
        BROWSER_LIST = List.copyOf(list);
    }

    /**
     * 未知浏览器
     */
    public static final Browser UNKNOWN = new Browser(NAME_UNKNOWN, null, null);

    /**
     * 构造浏览器
     *
     * @param name         浏览器名称
     * @param regex        匹配正则
     * @param versionRegex 版本解析正则
     */
    private Browser(String name, String regex, String versionRegex) {
        super(name, regex, versionRegex);
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
