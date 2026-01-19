package ext.library.tool.core;

import ext.library.tool.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 固定格式日志工具类
 * <p>
 * 自动获取调用类信息并按固定格式输出日志
 */
public final class Logs {

    /**
     * 获取调用者的类名
     *
     * @return 调用者类名
     */
    private static String getCallerClassName() {
        // 获取调用栈信息，跳过当前类和调用方法
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        // stackTrace[0] 是 getStackTrace 方法
        // stackTrace[1] 是当前 getCallerClassName 方法
        // stackTrace[2] 是 LogUtil 中的调用方法
        // stackTrace[3] 是实际调用 LogUtil 的类
        if (stackTrace.length > 3) {
            return stackTrace[3].getClassName();
        }
        return "Unknown";
    }

    /**
     * 打印 DEBUG 级别日志
     *
     * @param module  模块名称（对应 EmojiSymbol 中的模块）
     * @param message 日志内容
     */
    public static void debug(String module, String message) {
        Logger logger = LoggerFactory.getLogger(getCallerClassName());
        logger.debug("[{}] {}", module, message);
    }

    /**
     * 打印 DEBUG 级别日志
     *
     * @param module  模块名称（对应 EmojiSymbol 中的模块）
     * @param message 日志内容
     */
    public static void debug(String module, String message, Object... args) {
        debug(module, StringUtil.format(message, args));
    }

    /**
     * 打印 INFO 级别日志
     *
     * @param module  模块名称（对应 EmojiSymbol 中的模块）
     * @param message 日志内容
     */
    public static void info(String module, String message) {
        Logger logger = LoggerFactory.getLogger(getCallerClassName());
        logger.info("[{}] {}", module, message);
    }

    /**
     * 打印 INFO 级别日志
     *
     * @param module  模块名称（对应 EmojiSymbol 中的模块）
     * @param message 日志内容
     */
    public static void info(String module, String message, Object... args) {
        info(module, StringUtil.format(message, args));
    }

    /**
     * 打印 WARN 级别日志
     *
     * @param module  模块名称（对应 EmojiSymbol 中的模块）
     * @param message 日志内容
     */
    public static void warn(String module, String message) {
        Logger logger = LoggerFactory.getLogger(getCallerClassName());
        logger.warn("[{}] {}", module, message);
    }

    /**
     * 打印 WARN 级别日志
     *
     * @param module  模块名称（对应 EmojiSymbol 中的模块）
     * @param message 日志内容
     */
    public static void warn(String module, String message, Object... args) {
        warn(module, StringUtil.format(message, args));
    }

    /**
     * 打印 ERROR 级别日志
     *
     * @param module  模块名称（对应 EmojiSymbol 中的模块）
     * @param message 日志内容
     */
    public static void error(String module, String message) {
        Logger logger = LoggerFactory.getLogger(getCallerClassName());
        logger.error("[{}] {}", module, message);
    }

    /**
     * 打印 ERROR 级别日志
     *
     * @param module  模块名称（对应 EmojiSymbol 中的模块）
     * @param message 日志内容
     */
    public static void error(String module, String message, Object... args) {
        error(module, StringUtil.format(message, args));
    }

    /**
     * 打印 ERROR 级别日志
     *
     * @param module  模块名称（对应 EmojiSymbol 中的模块）
     * @param message 日志内容
     */
    public static void error(String module, Throwable throwable, String message, Object... args) {
        Logger logger = LoggerFactory.getLogger(getCallerClassName());
        logger.error(StringUtil.format("[{}] ", module) + message, args, throwable);
    }

}
