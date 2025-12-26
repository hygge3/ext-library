package ext.library.http.useragent;

import java.io.Serial;
import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * User-Agent 信息基类
 * <p>
 * 提供基础的模式匹配和版本解析功能。
 * </p>
 *
 * @since 1.0.0
 */
public abstract class UserAgentInfo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 未知类型标识
     */
    protected static final String NAME_UNKNOWN = "Unknown";

    /**
     * 信息名称
     */
    private final String name;

    /**
     * 信息匹配模式
     */
    private final Pattern pattern;

    /**
     * 构造
     *
     * @param name  名称
     * @param regex 正则表达式
     */
    protected UserAgentInfo(String name, String regex) {
        this(name, (regex == null) ? null : Pattern.compile(regex, Pattern.CASE_INSENSITIVE));
    }

    /**
     * 构造
     *
     * @param name    名称
     * @param pattern 编译后的正则模式
     */
    protected UserAgentInfo(String name, Pattern pattern) {
        this.name = name;
        this.pattern = pattern;
    }

    /**
     * 获取组件名称
     *
     * @return 名称
     */
    public String getName() {
        return name;
    }

    /**
     * 获取匹配模式
     *
     * @return 正则模式
     */
    public Pattern getPattern() {
        return pattern;
    }

    /**
     * 检查是否匹配指定内容
     *
     * @param content User-Agent 字符串
     * @return 是否匹配
     */
    public boolean isMatch(String content) {
        return pattern != null && pattern.matcher(content).find();
    }

    /**
     * 是否为未知类型
     *
     * @return 是否为未知
     */
    public boolean isUnknown() {
        return NAME_UNKNOWN.equals(this.name);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        UserAgentInfo other = (UserAgentInfo) obj;
        if (name == null) {
            return other.name == null;
        }
        return name.equals(other.name);
    }

    @Override
    public String toString() {
        return this.name;
    }
}
