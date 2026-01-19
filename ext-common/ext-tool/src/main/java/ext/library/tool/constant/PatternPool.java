package ext.library.tool.constant;

import java.util.regex.Pattern;

/**
 * 常用正则表达式池
 * <p>
 * 提供预编译的 Pattern 实例，避免重复编译开销
 *
 * @see <a href="https://any86.github.io/any-rule/">更多正则参考</a>
 */
public final class PatternPool {

    private PatternPool() {
        // 防止实例化
    }

    // region 基础字符类

    /**
     * 英文字母、数字和下划线
     */
    public static final Pattern GENERAL = Pattern.compile("^\\w+$");

    /**
     * 数字
     */
    public static final Pattern NUMBERS = Pattern.compile("\\d+");

    /**
     * 字母（大小写）
     */
    public static final Pattern WORD = Pattern.compile("[a-zA-Z]+");

    /**
     * 16 进制字符串
     */
    public static final Pattern HEX = Pattern.compile("^[a-fA-F0-9]+$");

    // endregion

    // region 中文相关

    /**
     * 中文汉字
     * <p>
     * 覆盖 CJK 统一汉字基本区及扩展区
     */
    public static final Pattern CHINESE = Pattern.compile("[\\u4E00-\\u9FFF]+");

    /**
     * 中文字、英文字母、数字和下划线
     */
    public static final Pattern GENERAL_WITH_CHINESE = Pattern.compile("^[\\u4E00-\\u9FFF\\w]+$");

    /**
     * 中文姓名
     * <p>
     * 2-60 位，支持中文和维吾尔族名字中的点（·）
     */
    public static final Pattern CHINESE_NAME = Pattern.compile("^[\\u4E00-\\u9FFF·]{2,60}$");

    // endregion

    // region 通信相关

    /**
     * 手机号（中国大陆）
     * <p>
     * 支持格式：13912345678, 8613912345678, +8613912345678
     */
    public static final Pattern MOBILE = Pattern.compile("(?:0|86|\\+86)?1[3-9]\\d{9}");

    /**
     * 座机号码
     * <p>
     * 格式：区号-号码，如 010-12345678
     */
    public static final Pattern TEL = Pattern.compile("(010|02\\d|0[3-9]\\d{2})-?(\\d{6,8})");

    /**
     * 邮箱
     * <p>
     * 符合 RFC 5322 规范
     *
     * @see <a href="http://emailregex.com/">emailregex.com</a>
     */
    public static final Pattern EMAIL = Pattern.compile(
            "(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)])",
            Pattern.CASE_INSENSITIVE);

    // endregion

    // region 网络相关

    /**
     * IPv4 地址
     */
    public static final Pattern IPV4 = Pattern.compile(
            "^(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)\\.(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)\\.(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)\\.(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)$");

    /**
     * IPv6 地址
     */
    public static final Pattern IPV6 = Pattern.compile(
            "(([0-9a-fA-F]{1,4}:){7}[0-9a-fA-F]{1,4}|([0-9a-fA-F]{1,4}:){1,7}:|([0-9a-fA-F]{1,4}:){1,6}:[0-9a-fA-F]{1,4}|([0-9a-fA-F]{1,4}:){1,5}(:[0-9a-fA-F]{1,4}){1,2}|([0-9a-fA-F]{1,4}:){1,4}(:[0-9a-fA-F]{1,4}){1,3}|([0-9a-fA-F]{1,4}:){1,3}(:[0-9a-fA-F]{1,4}){1,4}|([0-9a-fA-F]{1,4}:){1,2}(:[0-9a-fA-F]{1,4}){1,5}|[0-9a-fA-F]{1,4}:((:[0-9a-fA-F]{1,4}){1,6})|:((:[0-9a-fA-F]{1,4}){1,7}|:)|fe80:(:[0-9a-fA-F]{0,4}){0,4}%[0-9a-zA-Z]+|::(ffff(:0{1,4})?:)?((25[0-5]|(2[0-4]|1?[0-9])?[0-9])\\.){3}(25[0-5]|(2[0-4]|1?[0-9])?[0-9])|([0-9a-fA-F]{1,4}:){1,4}:((25[0-5]|(2[0-4]|1?[0-9])?[0-9])\\.){3}(25[0-5]|(2[0-4]|1?[0-9])?[0-9]))");

    /**
     * URL
     */
    public static final Pattern URL = Pattern.compile("[a-zA-Z]+://[\\w-+&@#/%?=~_|!:,.;]*[\\w-+&@#/%=~_|]");

    /**
     * HTTP/HTTPS/FTP/FILE URL
     *
     * @see <a href="http://urlregex.com/">urlregex.com</a>
     */
    public static final Pattern URL_HTTP = Pattern.compile(
            "(https?|ftp|file)://[\\w-+&@#/%?=~_|!:,.;]*[\\w-+&@#/%=~_|]",
            Pattern.CASE_INSENSITIVE);

    // endregion

    // region 证件相关

    /**
     * 18 位身份证号码
     */
    public static final Pattern CITIZEN_ID = Pattern.compile(
            "[1-9]\\d{5}[1-2]\\d{3}((0\\d)|(1[0-2]))(([012]\\d)|3[0-1])\\d{3}(\\d|X|x)");

    /**
     * 邮政编码（中国大陆）
     * <p>
     * 6 位数字，首位非 0
     */
    public static final Pattern ZIP_CODE = Pattern.compile("^[1-9]\\d{5}$");

    // endregion

    // region 其他

    /**
     * 分组变量
     * <p>
     * 匹配 $1, $2 等分组引用
     */
    public static final Pattern GROUP_VAR = Pattern.compile("\\$(\\d+)");

    /**
     * 时间
     * <p>
     * 格式：HH:mm 或 HH:mm:ss
     */
    public static final Pattern TIME = Pattern.compile("\\d{1,2}:\\d{1,2}(:\\d{1,2})?");

    /**
     * HTML 标签
     * <p>
     * 用于检测 XSS 攻击
     */
    public static final Pattern HTML_TAG = Pattern.compile("<[^>]+>", Pattern.CASE_INSENSITIVE);

    // endregion

}
