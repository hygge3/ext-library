package ext.library.useragent;

import java.io.Serial;
import java.util.ArrayList;
import java.util.List;

/**
 * 操作系统信息
 * <p>
 * 识别操作系统类型及其版本。
 * </p>
 *
 * @since 1.0.0
 */
public class OS extends UserAgentInfo {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 操作系统定义：名称、匹配正则、版本正则
     */
    private static final String[][] OS_DEFINITIONS = {
            {"Windows 10 or Windows Server 2016", "windows nt 10\\.0", "windows nt (10\\.0)"},
            {"Windows 8.1 or Windows Server 2012R2", "windows nt 6\\.3", "windows nt (6\\.3)"},
            {"Windows 8 or Windows Server 2012", "windows nt 6\\.2", "windows nt (6\\.2)"},
            {"Windows Vista", "windows nt 6\\.0", "windows nt (6\\.0)"},
            {"Windows 7 or Windows Server 2008R2", "windows nt 6\\.1", "windows nt (6\\.1)"},
            {"Windows 2003", "windows nt 5\\.2", "windows nt (5\\.2)"},
            {"Windows XP", "windows nt 5\\.1", "windows nt (5\\.1)"},
            {"Windows 2000", "windows nt 5\\.0", "windows nt (5\\.0)"},
            {"Windows Phone", "windows (ce|phone|mobile)( os)?", "windows (?:ce|phone|mobile) (\\d+([._]\\d+)*)"},
            {"Windows", "windows", null},
            {"OSX", "os x (\\d+)[._](\\d+)", "os x (\\d+([._]\\d+)*)"},
            {"Android", "Android", "Android (\\d+([._]\\d+)*)"},
            {"Android", "XiaoMi|MI\\s+", "\\(X(\\d+([._]\\d+)*)"},
            {"Linux", "linux", null},
            {"Wii", "wii", "wii libnup/(\\d+([._]\\d+)*)"},
            {"PS3", "playstation 3", "playstation 3; (\\d+([._]\\d+)*)"},
            {"PSP", "playstation portable", "Portable\\); (\\d+([._]\\d+)*)"},
            {"iPad", "\\(iPad.*os (\\d+)[._](\\d+)", "\\(iPad.*os (\\d+([._]\\d+)*)"},
            {"iPhone", "\\(iPhone.*os (\\d+)[._](\\d+)", "\\(iPhone.*os (\\d+([._]\\d+)*)"},
            {"iPod", "iPod touch[\\s\\;]+iPhone.*os (\\d+)[._](\\d+)", "iPod touch[\\s\\;]+iPhone.*os (\\d+([._]\\d+)*)"},
            {"Symbian", "symbian(os)?", null},
            {"Darwin", "Darwin\\/([\\d\\w\\.\\-]+)", "Darwin\\/([\\d\\w\\.\\-]+)"},
            {"Adobe Air", "AdobeAir\\/([\\d\\w\\.\\-]+)", "AdobeAir\\/([\\d\\w\\.\\-]+)"},
            {"Java", "Java[\\s]+([\\d\\w\\.\\-]+)", "Java[\\s]+([\\d\\w\\.\\-]+)"}
    };

    /**
     * 所有支持的操作系统列表
     */
    public static final List<OS> OS_LIST;

    static {
        List<OS> list = new ArrayList<>(OS_DEFINITIONS.length);
        for (String[] def : OS_DEFINITIONS) {
            list.add(new OS(def[0], def[1], def[2]));
        }
        OS_LIST = List.copyOf(list);
    }

    /**
     * 未知操作系统
     */
    public static final OS UNKNOWN = new OS(NAME_UNKNOWN, null, null);

    /**
     * 构造操作系统
     *
     * @param name         操作系统名称
     * @param regex        匹配正则
     * @param versionRegex 版本解析正则
     */
    private OS(String name, String regex, String versionRegex) {
        super(name, regex, versionRegex);
    }

    /**
     * 是否为 macOS
     *
     * @return 是否为 macOS
     */
    public boolean isMacOS() {
        return "OSX".equals(getName());
    }
}
