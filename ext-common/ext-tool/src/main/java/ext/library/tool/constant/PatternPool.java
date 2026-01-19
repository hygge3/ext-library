package ext.library.tool.constant;

import java.util.regex.Pattern;


/**
 * 常用正则表达式集合。更多正则见:<br>
 * <a href="https://any86.github.io/any-rule/">https://any86.github.io/any-rule/</a>
 */
public interface PatternPool {

    /**
     * 英文字母、数字和下划线。
     */
    Pattern GENERAL = Pattern.compile("^\\w+$");

    /**
     * 数字。
     */
    Pattern NUMBERS = Pattern.compile("\\d+");

    /**
     * 字母。
     */
    Pattern WORD = Pattern.compile("[a-zA-Z]+");

    /**
     * 单个中文汉字。
     */
    Pattern CHINESE = Pattern.compile("[\u2E80-\u2EFF\u2F00-\u2FDF\u31C0-\u31EF\u3400-\u4DBF\u4E00-\u9FFF\uF900-\uFAFF\uD840\uDC00-\uD869\uDEDF\uD869\uDF00-\uD86D\uDF3F\uD86D\uDF40-\uD86E\uDC1F\uD86E\uDC20-\uD873\uDEAF\uD87E\uDC00-\uD87E\uDE1F]");

    /**
     * 中文汉字。
     */
    Pattern CHINESES = Pattern.compile("[\u2E80-\u2EFF\u2F00-\u2FDF\u31C0-\u31EF\u3400-\u4DBF\u4E00-\u9FFF\uF900-\uFAFF\uD840\uDC00-\uD869\uDEDF\uD869\uDF00-\uD86D\uDF3F\uD86D\uDF40-\uD86E\uDC1F\uD86E\uDC20-\uD873\uDEAF\uD87E\uDC00-\uD87E\uDE1F]+");

    /**
     * 分组。
     */
    Pattern GROUP_VAR = Pattern.compile("\\$(\\d+)");

    /**
     * IP v4.
     */
    Pattern IPV4 = Pattern.compile("^(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)\\.(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)\\.(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)\\.(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)$");

    /**
     * IP v6.
     */
    Pattern IPV6 = Pattern.compile("(([0-9a-fA-F]{1,4}:){7}[0-9a-fA-F]{1,4}|([0-9a-fA-F]{1,4}:){1,7}:|([0-9a-fA-F]{1,4}:){1,6}:[0-9a-fA-F]{1,4}|([0-9a-fA-F]{1,4}:){1,5}(:[0-9a-fA-F]{1,4}){1,2}|([0-9a-fA-F]{1,4}:){1,4}(:[0-9a-fA-F]{1,4}){1,3}|([0-9a-fA-F]{1,4}:){1,3}(:[0-9a-fA-F]{1,4}){1,4}|([0-9a-fA-F]{1,4}:){1,2}(:[0-9a-fA-F]{1,4}){1,5}|[0-9a-fA-F]{1,4}:((:[0-9a-fA-F]{1,4}){1,6})|:((:[0-9a-fA-F]{1,4}){1,7}|:)|fe80:(:[0-9a-fA-F]{0,4}){0,4}%[0-9a-zA-Z]+|::(ffff(:0{1,4})?:)?((25[0-5]|(2[0-4]|1?[0-9])?[0-9])\\.){3}(25[0-5]|(2[0-4]|1?[0-9])?[0-9])|([0-9a-fA-F]{1,4}:){1,4}:((25[0-5]|(2[0-4]|1?[0-9])?[0-9])\\.){3}(25[0-5]|(2[0-4]|1?[0-9])?[0-9]))");

    /**
     * 邮件。符合 RFC 5322
     * 规范，正则来自：<a href="http://emailregex.com/">http://emailregex.com/</a><br>
     * <a href=
     * "https://stackoverflow.com/questions/386294/what-is-the-maximum-length-of-a-valid-email-address/44317754">https://stackoverflow.com/questions/386294/what-is-the-maximum-length-of-a-valid-email-address/44317754</a>
     * 注意 email 要宽松一点。比如 jetz.chong@hutool.cn、jetz-chong@
     * hutool.cn、jetz_chong@hutool.cn、dazhi.duan@hutool.cn 宽松一点把，都算是正常的邮箱
     */
     Pattern EMAIL = Pattern.compile("(?:[a-z0-9\\u4e00-\\u9fa5!#$%&'*+/=?^_`{|}~-]+\\.[a-z0-9\\u4e00-\\u9fa5!#$%&'*+/=?^_`{|}~-]+ *|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:[a-z0-9\\u4e00-\\u9fa5](?:[a-z0-9\\u4e00-\\u9fa5-]*[a-z0-9\\u4e00-\\u9fa5])?\\. +[a-z0-9\\u4e00-\\u9fa5](?:[a-z0-9\\u4e00-\\u9fa5-]*[a-z0-9\\u4e00-\\u9fa5])?|\\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9\\u4e00-\\u9fa5-]*[a-z0-9\\u4e00-\\u9fa5]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)])", Pattern.CASE_INSENSITIVE);

    /**
     * 移动电话。<br>
     * eg: 中国大陆： +86 180 4953 1399，2 位区域码标示 +13 位数字
     */
     Pattern MOBILE = Pattern.compile("(?:0|86|\\+86)?1[3-9]\\d{9}");

    /**
     * 座机号码。
     */
     Pattern TEL = Pattern.compile("(010|02\\d|0[3-9]\\d{2})-?(\\d{6,8})");

    /**
     * 18 位身份证号码。
     */
    public static final Pattern CITIZEN_ID = Pattern.compile("[1-9]\\d{5}[1-2]\\d{3}((0\\d)|(1[0-2]))(([012]\\d)|3[0-1])\\d{3}(\\d|X|x)");

    /**
     * 邮编，兼容港澳台。
     */
    Pattern ZIP_CODE = Pattern.compile("^(0[1-7]|1[0-356]|2[0-7]|3[0-6]|4[0-7]|5[0-7]|6[0-7]|7[0-5]|8[0-9]|9[0-8])\\d{4}|99907[78]$");

    /**
     * URL.
     */
     Pattern URL = Pattern.compile("[a-zA-Z]+://[\\w-+&@#/%?=~_|!:,.;]*[\\w-+&@#/%=~_|]");

    /**
     * Http URL.（来自：<a href="http://urlregex.com/">http://urlregex.com/</a>） <br>
     * 此正则同时支持 FTP、File 等协议的 URL
     */
    Pattern URL_HTTP = Pattern.compile("(https?|ftp|file)://[\\w-+&@#/%?=~_|!:,.;]*[\\w-+&@#/%=~_|]", Pattern.CASE_INSENSITIVE);

    /**
     * 中文字、英文字母、数字和下划线。
     */
     Pattern GENERAL_WITH_CHINESE = Pattern.compile("^[\u4E00-\u9FFF\\w]+$");

    /**
     * 16 进制字符串。
     */
    Pattern HEX = Pattern.compile("^[a-fA-F0-9]+$");

    /**
     * 时间正则。
     */
     Pattern TIME = Pattern.compile("\\d{1,2}:\\d{1,2}(:\\d{1,2})?");

    /**
     * 中文姓名。维吾尔族姓名里面的点是 · 输入法中文状态下，键盘左上角数字 1 前面的那个符号；<br>
     * 错误字符：{@code ．.。．.}<br>
     * 正确维吾尔族姓名： <pre>
     * 霍加阿卜杜拉·麦提喀斯木
     * 玛合萨提别克·哈斯木别克
     * 阿布都热依木江·艾斯卡尔
     * 阿卜杜尼亚孜·毛力尼亚孜
     * </pre> <pre>
     * ----------
     * 错误示例：孟  伟                reason: 有空格
     * 错误示例：连逍遥 0               reason: 数字
     * 错误示例：依帕古丽 - 艾则孜        reason: 特殊符号
     * 错误示例：牙力空。买提萨力        reason: 新疆人的点不对
     * 错误示例：王建鹏 2002-3-2        reason: 有数字、特殊符号
     * 错误示例：雷金默 (雷皓添）reason: 有括号
     * 错误示例：翟冬：亮               reason: 有特殊符号
     * 错误示例：李                   reason: 少于 2 位
     * ----------
     * </pre> 总结中文姓名：2-60 位，只能是中文和维吾尔族的点· 放宽汉字范围：如生僻姓名 刘欣䶮 yǎn
     */
     Pattern CHINESE_NAME = Pattern.compile("^[\u2E80-\u9FFF·]{2,60}$");

    /**
     * HTML 标签正则。
     */
    Pattern RE_HTML_MARK = Pattern.compile("(<[^<]*?>)|(<\\s*?/[^<]*?>)|(<[^<]*?/\\s*?>)", Pattern.CASE_INSENSITIVE);

}
