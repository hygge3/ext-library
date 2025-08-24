package ext.library.tool.core;

import lombok.experimental.UtilityClass;

import java.io.File;
import java.nio.charset.Charset;

/**
 * 系统工具类
 */
@UtilityClass
public class Systems {

    /**
     * 当前系统是否为 Windows 系统，参考以下系统 API
     *
     * @return boolean
     */
    public boolean isWindows() {
        return osName().contains("Windows");
    }

    public boolean isLinux() {
        return osName().contains("Linux");
    }

    public boolean isMacX() {
        return osName().contains("OS X");
    }

    public boolean isMac() {
        return osName().contains("Mac OS");
    }

    public boolean isAix() {
        return osName().contains("AIX");
    }

    public String osName() {
        return System.getProperty("os.name");
    }

    /**
     * 获取系统字符集
     */
    public Charset charset() {
        return Charset.forName(System.getProperty("sun.jnu.encoding"));
    }

    public String lineSeparator() {
        return System.lineSeparator();
    }

    public String fileSeparator() {
        return File.separator;
    }

    public String username() {
        return System.getProperty("user.name");
    }

}