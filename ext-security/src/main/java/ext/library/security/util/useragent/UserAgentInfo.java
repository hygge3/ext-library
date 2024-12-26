package ext.library.security.util.useragent;

import java.io.Serial;
import java.io.Serializable;
import java.util.regex.Pattern;

import lombok.Getter;
import org.jetbrains.annotations.Contract;

/**
 * User-agent 信息
 */
@Getter
public class UserAgentInfo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 未知类型
     */
    public static final String NameUnknown = "Unknown";

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
     * @param name  名字
     * @param regex 表达式
     */
    public UserAgentInfo(String name, String regex) {
        this(name, (null == regex) ? null : Pattern.compile(regex, Pattern.CASE_INSENSITIVE));
    }

    /**
     * 构造
     *
     * @param name    名字
     * @param pattern 匹配模式
     */
    @Contract(pure = true)
    public UserAgentInfo(String name, Pattern pattern) {
        this.name = name;
        this.pattern = pattern;
    }

    /**
     * 指定内容中是否包含匹配此信息的内容
     *
     * @param content User-Agent 字符串
     * @return 是否包含匹配此信息的内容
     */
    public boolean isMatch(String content) {
        return pattern.matcher(content).find();
    }

    /**
     * 是否为 Unknown
     *
     * @return 是否为 Unknown
     */
    public boolean isUnknown() {
        return NameUnknown.equals(this.name);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        return result;
    }

    @Override
    @Contract(value = "null->false", pure = true)
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final UserAgentInfo other = (UserAgentInfo) obj;
        if (name == null) {
            return other.name == null;
        } else {return name.equals(other.name);}
    }

    @Override
    public String toString() {
        return this.name;
    }

}
