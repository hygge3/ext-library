package ext.library.tool.runtime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 固定格式日志工具类
 * <p>
 * 自动获取调用类信息并按固定格式输出日志
 */
public final class Logs {

    private static final StackWalker stackWalker = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE);

    /**
     * 获取调用者的类名
     * <p>
     * 使用 StackWalker 跳过 Logs 类的所有方法，获取实际调用者
     *
     * @return 调用者类名
     */
    private static String getCallerClassName() {
        return stackWalker.walk(frames -> frames
                .map(StackWalker.StackFrame::getClassName)
                .filter(className -> !className.equals(Logs.class.getName()))
                .findFirst()
                .orElse("Unknown"));
    }

    /**
     * 获取指定调用者的 Logger
     */
    private static Logger getLogger() {
        return LoggerFactory.getLogger(getCallerClassName());
    }

    // region DEBUG

    /**
     * 打印 DEBUG 级别日志
     *
     * @param module  模块名称（对应 EmojiSymbol 中的模块）
     * @param message 日志内容
     */
    public static void debug(String module, String message) {
        getLogger().debug("[{}] {}", module, message);
    }

    /**
     * 打印 DEBUG 级别日志
     *
     * @param module  模块名称（对应 EmojiSymbol 中的模块）
     * @param message 日志内容（支持 {} 占位符）
     * @param args    格式化参数
     */
    public static void debug(String module, String message, Object... args) {
        Object[] allArgs = prependModule(module, args);
        getLogger().debug("[{}] " + message, allArgs);
    }

    /**
     * 打印 DEBUG 级别日志（带异常堆栈）
     *
     * @param module    模块名称（对应 EmojiSymbol 中的模块）
     * @param throwable 异常对象
     * @param message   日志内容（支持 {} 占位符）
     * @param args      格式化参数
     */
    public static void debug(String module, Throwable throwable, String message, Object... args) {
        Object[] allArgs = prependModuleAppendThrowable(module, throwable, args);
        getLogger().debug("[{}] " + message, allArgs);
    }

    /**
     * 打印 DEBUG 级别日志（不带模块名）
     *
     * @param message 日志内容
     */
    public static void debug(String message) {
        getLogger().debug(message);
    }

    /**
     * 打印 DEBUG 级别日志（不带模块名，带异常堆栈）
     *
     * @param throwable 异常对象
     * @param message   日志内容（支持 {} 占位符）
     * @param args      格式化参数
     */
    public static void debug(Throwable throwable, String message, Object... args) {
        Object[] allArgs = appendThrowable(throwable, args);
        getLogger().debug(message, allArgs);
    }

    // endregion

    // region INFO

    /**
     * 打印 INFO 级别日志
     *
     * @param module  模块名称（对应 EmojiSymbol 中的模块）
     * @param message 日志内容
     */
    public static void info(String module, String message) {
        getLogger().info("[{}] {}", module, message);
    }

    /**
     * 打印 INFO 级别日志
     *
     * @param module  模块名称（对应 EmojiSymbol 中的模块）
     * @param message 日志内容（支持 {} 占位符）
     * @param args    格式化参数
     */
    public static void info(String module, String message, Object... args) {
        Object[] allArgs = prependModule(module, args);
        getLogger().info("[{}] " + message, allArgs);
    }

    /**
     * 打印 INFO 级别日志（带异常堆栈）
     *
     * @param module    模块名称（对应 EmojiSymbol 中的模块）
     * @param throwable 异常对象
     * @param message   日志内容（支持 {} 占位符）
     * @param args      格式化参数
     */
    public static void info(String module, Throwable throwable, String message, Object... args) {
        Object[] allArgs = prependModuleAppendThrowable(module, throwable, args);
        getLogger().info("[{}] " + message, allArgs);
    }

    /**
     * 打印 INFO 级别日志（不带模块名）
     *
     * @param message 日志内容
     */
    public static void info(String message) {
        getLogger().info(message);
    }

    /**
     * 打印 INFO 级别日志（不带模块名，带异常堆栈）
     *
     * @param throwable 异常对象
     * @param message   日志内容（支持 {} 占位符）
     * @param args      格式化参数
     */
    public static void info(Throwable throwable, String message, Object... args) {
        Object[] allArgs = appendThrowable(throwable, args);
        getLogger().info(message, allArgs);
    }

    // endregion

    // region WARN

    /**
     * 打印 WARN 级别日志
     *
     * @param module  模块名称（对应 EmojiSymbol 中的模块）
     * @param message 日志内容
     */
    public static void warn(String module, String message) {
        getLogger().warn("[{}] {}", module, message);
    }

    /**
     * 打印 WARN 级别日志
     *
     * @param module  模块名称（对应 EmojiSymbol 中的模块）
     * @param message 日志内容（支持 {} 占位符）
     * @param args    格式化参数
     */
    public static void warn(String module, String message, Object... args) {
        Object[] allArgs = prependModule(module, args);
        getLogger().warn("[{}] " + message, allArgs);
    }

    /**
     * 打印 WARN 级别日志（带异常堆栈）
     *
     * @param module    模块名称（对应 EmojiSymbol 中的模块）
     * @param throwable 异常对象
     * @param message   日志内容（支持 {} 占位符）
     * @param args      格式化参数
     */
    public static void warn(String module, Throwable throwable, String message, Object... args) {
        Object[] allArgs = prependModuleAppendThrowable(module, throwable, args);
        getLogger().warn("[{}] " + message, allArgs);
    }

    /**
     * 打印 WARN 级别日志（不带模块名）
     *
     * @param message 日志内容
     */
    public static void warn(String message) {
        getLogger().warn(message);
    }

    /**
     * 打印 WARN 级别日志（不带模块名，带异常堆栈）
     *
     * @param throwable 异常对象
     * @param message   日志内容（支持 {} 占位符）
     * @param args      格式化参数
     */
    public static void warn(Throwable throwable, String message, Object... args) {
        Object[] allArgs = appendThrowable(throwable, args);
        getLogger().warn(message, allArgs);
    }

    // endregion

    // region ERROR

    /**
     * 打印 ERROR 级别日志
     *
     * @param module  模块名称（对应 EmojiSymbol 中的模块）
     * @param message 日志内容
     */
    public static void error(String module, String message) {
        getLogger().error("[{}] {}", module, message);
    }

    /**
     * 打印 ERROR 级别日志
     *
     * @param module  模块名称（对应 EmojiSymbol 中的模块）
     * @param message 日志内容（支持 {} 占位符）
     * @param args    格式化参数
     */
    public static void error(String module, String message, Object... args) {
        Object[] allArgs = prependModule(module, args);
        getLogger().error("[{}] " + message, allArgs);
    }

    /**
     * 打印 ERROR 级别日志（带异常堆栈）
     *
     * @param module    模块名称（对应 EmojiSymbol 中的模块）
     * @param throwable 异常对象
     * @param message   日志内容（支持 {} 占位符）
     * @param args      格式化参数
     */
    public static void error(String module, Throwable throwable, String message, Object... args) {
        Object[] allArgs = prependModuleAppendThrowable(module, throwable, args);
        getLogger().error("[{}] " + message, allArgs);
    }

    /**
     * 打印 ERROR 级别日志（不带模块名）
     *
     * @param message 日志内容
     */
    public static void error(String message) {
        getLogger().error(message);
    }

    /**
     * 打印 ERROR 级别日志（不带模块名，带异常堆栈）
     *
     * @param throwable 异常对象
     * @param message   日志内容（支持 {} 占位符）
     * @param args      格式化参数
     */
    public static void error(Throwable throwable, String message, Object... args) {
        Object[] allArgs = appendThrowable(throwable, args);
        getLogger().error(message, allArgs);
    }

    // endregion

    // region 辅助方法

    /**
     * 将 throwable 添加到参数数组末尾
     * <p>
     * SLF4J 会将最后一个 Throwable 参数特殊处理，打印堆栈
     */
    private static Object[] appendThrowable(Throwable throwable, Object[] args) {
        Object[] allArgs = new Object[args.length + 1];
        System.arraycopy(args, 0, allArgs, 0, args.length);
        allArgs[args.length] = throwable;
        return allArgs;
    }

    /**
     * 将 module 添加到参数数组开头
     */
    private static Object[] prependModule(String module, Object[] args) {
        Object[] allArgs = new Object[args.length + 1];
        allArgs[0] = module;
        System.arraycopy(args, 0, allArgs, 1, args.length);
        return allArgs;
    }

    /**
     * 将 module 添加到参数数组开头，throwable 添加到末尾
     * <p>
     * SLF4J 会将最后一个 Throwable 参数特殊处理，打印堆栈
     */
    private static Object[] prependModuleAppendThrowable(String module, Throwable throwable, Object[] args) {
        Object[] allArgs = new Object[args.length + 2];
        allArgs[0] = module;
        System.arraycopy(args, 0, allArgs, 1, args.length);
        allArgs[allArgs.length - 1] = throwable;
        return allArgs;
    }

    // endregion

}
