package ext.library.tool.core;

import ext.library.tool.constant.EmojiSymbol;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.UndeclaredThrowableException;

/**
 * 异常处理工具类
 */
public final class Exceptions {

    /**
     * 将异常堆栈转换为字符串
     *
     * @param e 异常
     * @return 异常堆栈信息字符串
     */
    public static String print(Throwable e) {
        StringWriter stringWriter = new StringWriter();
        e.printStackTrace(new PrintWriter(stringWriter));
        return stringWriter.toString();
    }

    /**
     * 将 CheckedException 转换为 UncheckedException
     *
     * @param e 异常
     * @return RuntimeException
     */
    public static RuntimeException unchecked(Throwable e) {
        if (e instanceof Error error) {
            throw error;
        } else if (e instanceof IllegalAccessException || e instanceof IllegalArgumentException || e instanceof NoSuchMethodException) {
            return new IllegalArgumentException(e);
        } else if (e instanceof InvocationTargetException exception) {
            return runtime(exception.getTargetException());
        } else if (e instanceof RuntimeException exception) {
            return exception;
        } else if (e instanceof InterruptedException) {
            Thread.currentThread().interrupt();
            return new IllegalStateException(e);
        }
        return runtime(e);
    }

    /**
     * 不采用 RuntimeException 包装，直接抛出，使异常更加精准
     *
     * @param throwable 异常
     * @param <T>       泛型标记
     * @return 原异常
     * @throws T 原异常类型
     */
    @SuppressWarnings("unchecked")
    private static <T extends Throwable> T runtime(Throwable throwable) throws T {
        throw (T) throwable;
    }

    /**
     * 代理异常解包
     *
     * @param wrapped 包装过的异常
     * @return 解包后的异常
     */
    public static Throwable unwrap(Throwable wrapped) {
        Throwable unwrapped = wrapped;
        while (true) {
            if (unwrapped instanceof InvocationTargetException exception) {
                Throwable target = exception.getTargetException();
                if (target == null) {
                    return unwrapped;
                }
                unwrapped = target;
            } else if (unwrapped instanceof UndeclaredThrowableException exception) {
                Throwable undeclared = exception.getUndeclaredThrowable();
                if (undeclared == null) {
                    return unwrapped;
                }
                unwrapped = undeclared;
            } else {
                return unwrapped;
            }
        }
    }

    /**
     * 打印异常信息（含堆栈）
     *
     * @param e 异常
     */
    public static void log(Throwable e) {
        Throwable cause = e.getCause();
        Throwable target = (cause != null) ? cause : e;
        Logs.error(EmojiSymbol.EXT, target, target.getMessage());
    }

}
