package ext.library.useragent;

import java.io.Serial;
import java.util.ArrayList;
import java.util.List;

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
     * 引擎定义：名称、匹配正则、版本正则
     */
    private static final String[][] ENGINE_DEFINITIONS = {
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
    public static final List<Engine> ENGINE_LIST;

    static {
        List<Engine> list = new ArrayList<>(ENGINE_DEFINITIONS.length);
        for (String[] def : ENGINE_DEFINITIONS) {
            list.add(new Engine(def[0], def[1], def[2]));
        }
        ENGINE_LIST = List.copyOf(list);
    }

    /**
     * 未知引擎
     */
    public static final Engine UNKNOWN = new Engine(NAME_UNKNOWN, null, null);

    /**
     * 构造引擎
     *
     * @param name         引擎名称
     * @param regex        匹配正则
     * @param versionRegex 版本解析正则
     */
    private Engine(String name, String regex, String versionRegex) {
        super(name, regex, versionRegex);
    }
}
