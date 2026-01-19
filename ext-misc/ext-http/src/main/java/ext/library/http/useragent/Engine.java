package ext.library.http.useragent;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 渲染引擎信息
 * <p>
 * 识别浏览器使用的渲染引擎及其版本。
 * </p>
 *
 * @since 1.0.0
 */
public class Engine extends UserAgentInfo {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 引擎名称到版本正则的映射
     */
    private static final String[][] ENGINE_VERSION_PATTERNS = {
            {"Trident", "trident", "trident[/\\- ]([\\d\\w.\\-]+)"},
            {"Webkit", "webkit", "webkit[/\\- ]?([\\d\\w.\\-]+)"},
            {"Chrome", "chrome", "chrome[/\\- ]?([\\d\\w.\\-]+)"},
            {"Opera", "opera", "opera[/\\- ]?([\\d\\w.\\-]+)"},
            {"Presto", "presto", "presto[/\\- ]?([\\d\\w.\\-]+)"},
            {"Gecko", "gecko", "gecko[/\\- ]?([\\d\\w.\\-]+)"},
            {"KHTML", "khtml", "khtml[/\\- ]?([\\d\\w.\\-]+)"},
            {"Konqueror", "konqueror", "konqueror[/\\- ]?([\\d\\w.\\-]+)"},
            {"MIDP", "MIDP", "MIDP[/\\- ]?([\\d\\w.\\-]+)"}
    };

    /**
     * 所有支持的引擎列表
     */
    public static final List<Engine> ENGINES;

    static {
        List<Engine> list = new ArrayList<>(ENGINE_VERSION_PATTERNS.length);
        for (String[] info : ENGINE_VERSION_PATTERNS) {
            list.add(new Engine(info[0], info[1], info[2]));
        }
        ENGINES = List.copyOf(list);
    }

    /**
     * 未知引擎
     */
    public static final Engine UNKNOWN = new Engine(NAME_UNKNOWN, (String) null, (String) null);

    /**
     * 版本解析模式
     */
    private final Pattern versionPattern;

    /**
     * 构造引擎
     *
     * @param name         引擎名称
     * @param regex        匹配正则
     * @param versionRegex 版本解析正则
     */
    private Engine(String name, String regex, String versionRegex) {
        super(name, regex);
        if (versionRegex != null) {
            this.versionPattern = Pattern.compile(versionRegex, Pattern.CASE_INSENSITIVE);
        } else {
            this.versionPattern = null;
        }
    }

    /**
     * 获取引擎版本
     *
     * @param userAgentString User-Agent 字符串
     * @return 版本号，未知返回 null
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
}
