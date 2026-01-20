package ext.library.tool.runtime;

import java.io.File;
import java.nio.charset.Charset;

/**
 * 系统信息工具类
 */
public final class Systems {

    /**
     * 判断当前操作系统是否为 Windows 系统
     *
     * @return 如果是 Windows 系统返回 true，否则返回 false
     */
    public static boolean isWindows() {
        return osName().contains("Windows");
    }

    /**
     * 判断当前操作系统是否为 Linux 系统
     * <p>
     * 通过获取操作系统名称并检查是否包含 "Linux" 字符串来判断
     *
     * @return 如果操作系统是 Linux, 返回 true; 否则返回 false
     */
    public static boolean isLinux() {
        return osName().contains("Linux");
    }

    /**
     * 判断当前操作系统是否为 macOS 的 X 版本 (OS X)
     * <p>
     * 通过获取操作系统名称并检查是否包含 "OS X" 来判断当前系统是否为 macOS 的 X 版本。
     *
     * @return 如果当前操作系统是 macOS 的 X 版本，返回 true; 否则返回 false
     */
    public static boolean isMacX() {
        return osName().contains("OS X");
    }

    /**
     * 判断当前操作系统是否为 Mac OS
     * <p>
     * 通过获取操作系统名称并检查是否包含 "Mac OS" 来判断当前系统是否为 Mac 操作系统
     *
     * @return 如果当前操作系统是 Mac OS, 返回 true; 否则返回 false
     */
    public static boolean isMac() {
        return osName().contains("Mac OS");
    }

    /**
     * 判断当前操作系统是否为 AIX 系统
     * <p>
     * 通过获取操作系统名称并检查是否包含 "AIX" 字符串来判断系统类型
     *
     * @return 如果当前操作系统是 AIX, 返回 true; 否则返回 false
     */
    public static boolean isAix() {
        return osName().contains("AIX");
    }

    /**
     * 获取操作系统的名称
     * <p>
     * 该方法用于返回当前运行环境的操作系统名称。
     *
     * @return 操作系统名称
     */
    public static String osName() {
        return System.getProperty("os.name");
    }

    /**
     * 获取系统字符集
     * <p>
     * 优先使用 JVM 内部编码属性，若不可用则回退到系统默认字符集
     *
     * @return 系统字符集
     */
    public static Charset charset() {
        String encoding = System.getProperty("sun.jnu.encoding");
        return encoding != null ? Charset.forName(encoding) : Charset.defaultCharset();
    }

    /**
     * 获取系统默认的行分隔符
     * <p>
     * 返回当前操作系统对应的行分隔符，例如在 Windows 中返回 "\r\n", 在 Unix 系统中返回 "\n"
     *
     * @return 系统默认的行分隔符字符串
     */
    public static String lineSeparator() {
        return System.lineSeparator();
    }

    /**
     * 获取系统文件分隔符
     * <p>
     * 返回当前操作系统所使用的文件路径分隔符，例如 Windows 系统返回 "\", Linux 系统返回 "/"
     *
     * @return 系统文件分隔符
     */
    public static String fileSeparator() {
        return File.separator;
    }

    /**
     * 获取当前系统的用户名
     * <p>
     * 通过系统属性获取当前登录用户的名称
     *
     * @return 当前系统的用户名
     */
    public static String username() {
        return System.getProperty("user.name");
    }

}
