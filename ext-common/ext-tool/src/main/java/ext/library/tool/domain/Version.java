package ext.library.tool.domain;

import java.util.Objects;

/**
 * 版本号比较工具
 * <p>
 * 思路来源于：<a href="https://github.com/hotoo/versioning/blob/master/versioning.js">hotoo/versioning</a>
 *
 * <p>使用示例：
 * <pre>{@code
 * // 完整模式（默认）
 * Version.of("v0.1.1").eq("v0.1.2");  // false
 * Version.of("1.9").lt("1.10");       // true（数字比较）
 *
 * // 不完整模式
 * Version.of("v0.1").incomplete().eq("v0.1.2");  // true
 * }</pre>
 *
 * @since 2025.01.01
 */
public final class Version {

    private static final String delimiter = "\\.";

    /** 版本号 */
    private final String version;

    /** 是否完整模式，默认使用完整模式 */
    private boolean complete = true;

    private Version(String version) {
        this.version = version;
    }

    /**
     * 创建 Version 实例
     *
     * @param version 版本号字符串
     *
     * @return Version 实例
     */
    public static Version of(String version) {
        return new Version(version);
    }

    /**
     * 比较两个版本号
     *
     * @param v1       第一个版本号
     * @param v2       第二个版本号
     * @param complete 是否完整比较（true: 比较所有段，false: 只比较公共段）
     *
     * @return 比较结果：v1 < v2 返回负数，v1 == v2 返回 0，v1 > v2 返回正数
     */
    private static int compare(String v1, String v2, boolean complete) {
        // null 视为最小版本
        if (Objects.equals(v1, v2)) {
            return 0;
        } else if (v1 == null) {
            return -1;
        } else if (v2 == null) {
            return 1;
        }

        // 去除空格
        v1 = v1.trim();
        v2 = v2.trim();
        if (v1.equals(v2)) {
            return 0;
        }

        String[] v1s = v1.split(delimiter);
        String[] v2s = v2.split(delimiter);
        int v1sLen = v1s.length;
        int v2sLen = v2s.length;
        int len = complete ? Math.max(v1sLen, v2sLen) : Math.min(v1sLen, v2sLen);

        for (int i = 0; i < len; i++) {
            String c1 = i >= v1sLen ? "" : v1s[i];
            String c2 = i >= v2sLen ? "" : v2s[i];

            int result = compareSegment(c1, c2);
            if (result != 0) {
                return result;
            }
        }

        return 0;
    }

    /**
     * 比较版本号的单个段
     * <p>
     * 优先尝试数字比较，失败时回退到字符串比较
     *
     * @param s1 第一个段
     * @param s2 第二个段
     *
     * @return 比较结果
     */
    private static int compareSegment(String s1, String s2) {
        // 尝试数字比较
        try {
            int n1 = parseVersionNumber(s1);
            int n2 = parseVersionNumber(s2);
            return Integer.compare(n1, n2);
        } catch (NumberFormatException e) {
            // 回退到字符串比较
            return s1.compareTo(s2);
        }
    }

    /**
     * 解析版本号段为数字
     * <p>
     * 支持带前缀的版本号（如 "v1"、"r2"）
     *
     * @param segment 版本号段
     *
     * @return 解析后的数字
     *
     * @throws NumberFormatException 如果无法解析为数字
     */
    private static int parseVersionNumber(String segment) {
        if (segment.isEmpty()) {
            return 0;
        }
        // 移除常见前缀（v, r, V, R）
        String numeric = segment.replaceFirst("^[vVrR]", "");
        return Integer.parseInt(numeric);
    }

    /**
     * 设置为不完整模式
     * <p>
     * 不完整模式下，只比较两个版本号的公共部分。
     * 例如 "1.0" 和 "1.0.1" 在不完整模式下视为相等。
     *
     * @return 当前 Version 实例
     */
    public Version incomplete() {
        this.complete = false;
        return this;
    }

    /**
     * 判断版本号是否相等
     *
     * @param version 要比较的版本号
     *
     * @return 如果相等返回 true
     */
    public boolean eq(String version) {
        return compare(version) == 0;
    }

    /**
     * 判断版本号是否不相等
     *
     * @param version 要比较的版本号
     *
     * @return 如果不相等返回 true
     */
    public boolean ne(String version) {
        return compare(version) != 0;
    }

    /**
     * 判断是否大于指定版本
     *
     * @param version 要比较的版本号
     *
     * @return 如果大于返回 true
     */
    public boolean gt(String version) {
        return compare(version) > 0;
    }

    /**
     * 判断是否大于或等于指定版本
     *
     * @param version 要比较的版本号
     *
     * @return 如果大于或等于返回 true
     */
    public boolean gte(String version) {
        return compare(version) >= 0;
    }

    /**
     * 判断是否小于指定版本
     *
     * @param version 要比较的版本号
     *
     * @return 如果小于返回 true
     */
    public boolean lt(String version) {
        return compare(version) < 0;
    }

    /**
     * 判断是否小于或等于指定版本
     *
     * @param version 要比较的版本号
     *
     * @return 如果小于或等于返回 true
     */
    public boolean lte(String version) {
        return compare(version) <= 0;
    }

    /**
     * 与另一个版本号进行比较
     *
     * @param version 要比较的版本号
     *
     * @return 比较结果：小于返回负数，等于返回 0，大于返回正数
     */
    private int compare(String version) {
        return compare(this.version, version, complete);
    }
}
