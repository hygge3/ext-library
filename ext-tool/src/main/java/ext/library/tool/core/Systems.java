package ext.library.tool.core;

import java.io.File;
import java.nio.charset.Charset;

/**
 * 系统工具类
 */
public class Systems {

    /**
     * 当前系统是否为 Windows 系统，参考以下系统 API
     *
     * @return boolean
     */
    public static boolean isWindows() {
        return osName().contains("Windows");
    }

    public static boolean isLinux() {
        return osName().contains("Linux");
    }

    public static boolean isMacX() {
        return osName().contains("OS X");
    }

    public static boolean isMac() {
        return osName().contains("Mac OS");
    }

    public static boolean isAix() {
        return osName().contains("AIX");
    }

    public static String osName() {
        return System.getProperty("os.name");
    }

    /**
     * 获取系统字符集
     */
    public static Charset charset() {
        return Charset.forName(System.getProperty("sun.jnu.encoding"));
    }

    public static String lineSeparator() {
        return System.lineSeparator();
    }

    public static String fileSeparator() {
        return File.separator;
    }

    public static String username() {
        return System.getProperty("user.name");
    }

}