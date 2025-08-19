package ext.library.tool.constant;

/**
 * 符号常量
 */
public interface Symbol {

    /** 空格 */
    char C_SPACE = ' ';

    /** 制表符 */
    char C_TAB = '\t';

    /** 点 */
    char C_DOT = '.';

    /** 单引号 */
    char C_QUOTE = '\'';

    /** 双引号 */
    char C_DOUBLE_QUOTE = '"';

    /** 斜杠 */
    char C_SLASH = '/';

    /** 反斜杠 */
    char C_BACKSLASH = '\\';

    /** 回车 */
    char C_CR = '\r';

    /** 换行 */
    char C_LF = '\n';

    /** 下划线 */
    char C_UNDERLINE = '_';

    /** 连字号 */
    char C_DASHED = '-';

    /** 逗号 */
    char C_COMMA = ',';

    /** 小于号 */
    char C_LESS_THEN = '<';

    /** 大于号 */
    char C_GREATER_THEN = '>';

    /** 左圆括号 */
    char C_LEFT_PARENTHESES = '(';

    /** 右圆括号 */
    char C_RIGHT_PARENTHESES = ')';

    /** 左大括号 */
    char C_LEFT_BRACES = '{';

    /** 右大括号 */
    char C_RIGHT_BRACES = '}';

    /** 左方括号 */
    char C_LEFT_BRACKET = '[';

    /** 右方括号 */
    char C_RIGHT_BRACKET = ']';

    /** 冒号 */
    char C_COLON = ':';

    /** 分号 */
    char C_SEMICOLON = ';';

    /** 井字号 */
    char C_SHARP = '#';

    /** 星号 */
    char C_ASTERISK = '*';

    /** 艾特符 */
    char C_AT = '@';

    /** 波浪线 */
    char C_TILDE = '~';

    /** 美元符 */
    char C_DOLLAR = '$';

    /** 问号 */
    char C_QUESTION = '?';

    /** 百分号 */
    char C_PERCENT = '%';

    /** 等号 */
    char C_EQUAL = '=';

    /** 和 */
    char C_AND = '&';

    /** 单竖线 */
    char C_PIPE = '|';

    /** 空格 */
    String SPACE = " ";

    /** 制表符 */
    String TAB = "\t";

    /** 点 */
    String DOT = ".";

    /** 双点 */
    String DOUBLE_DOT = "..";

    /** 单引号 */
    String QUOTE = "'";

    /** 双引号 */
    String DOUBLE_QUOTE = "\"";

    /**
     * 斜杆
     * use as The Unix directory separator character.
     */
    String SLASH = "/";

    /**
     * 反斜杆
     * use as The Windows directory separator character.
     */
    String BACKSLASH = "\\";

    /** 空 */
    String EMPTY = "";

    /** 回车 */
    String CR = "\r";

    /**
     * 换行
     * use as The Unix line separator string.
     */
    String LF = "\n";

    /**
     * 回车 + 换行
     * use as The Windows line separator string.
     */
    String CRLF = "\r\n";

    /** 下划线 */
    String UNDERLINE = "_";

    /** 连接线 */
    String DASHED = "-";

    /** 逗点 */
    String COMMA = ",";

    /** 小于 */
    String LESS_THEN = "<";

    /** 大于 */
    String GREATER_THEN = ">";

    /** 左圆括号 */
    String LEFT_PARENTHESES = "(";

    /** 右圆括号 */
    String RIGHT_PARENTHESES = ")";

    /** 左大括号 */
    String LEFT_BRACES = "{";

    /** 右大括号 */
    String RIGHT_BRACES = "}";

    /** 左方括号 */
    String LEFT_BRACKET = "[";

    /** 右方括号 */
    String RIGHT_BRACKET = "]";

    /** 冒号 */
    String COLON = ":";

    /** 分号 */
    String SEMICOLON = ";";

    /** 井号 */
    String SHARP = "#";

    /** 星号 */
    String ASTERISK = "*";

    /** 艾特 */
    String AT = "@";

    /** 波浪线 */
    String TILDE = "~";

    /** 美元符 */
    String DOLLAR = "$";

    /** 问号 */
    String QUESTION = "?";

    /** 百分号 */
    String PERCENT = "%";

    /** 等号 */
    String EQUAL = "=";

    /** 和 */
    String AND = "&";

    /** 中划线 */
    String PIPE = "|";

    /** html 空格 */
    String HTML_NBSP = "&nbsp;";

    /** HTML 与号 */
    String HTML_AMP = "&amp;";

    /** HTML 引号 */
    String HTML_QUOTE = "&quot;";

    /** HTML 撇号 */
    String HTML_APOS = "&apos;";

    /** HTML 小于号 */
    String HTML_LT = "&lt;";

    /** HTML 大于号 */
    String HTML_GT = "&gt;";

    /** null */
    String NULL = "null";

    /** 空 JSON */
    String EMPTY_JSON = "{}";

    /** 十六进制字符大写字符串 */
    String HEX_CHAR_STRING_UPPER = "0123456789ABCDEF";

    /** 十六进制字符小写字符串 */
    String HEX_CHAR_STRING_LOWER = "0123456789abcdef";

    /** Hex Char 数组大写 */
    char[] HEX_CHAR_ARRAY_UPPER = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

    /** hex char 数组小写 */
    char[] HEX_CHAR_ARRAY_LOWER = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

}