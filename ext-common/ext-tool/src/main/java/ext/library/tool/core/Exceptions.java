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
     * parse error to string
     *
     * @param e 异常
     *
     * @return 要打印的异常栈信息
     */
    public static String print(Throwable e) {
        StringWriter stringWriter = new StringWriter();
        e.printStackTrace(new PrintWriter(stringWriter));
        return stringWriter.toString();
    }

    /**
     * 将 CheckedException 转换为 UncheckedException.
     *
     * @param e Throwable
     *
     * @return {RuntimeException}
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
        }
        return runtime(e);
    }

    /**
     * 不采用 RuntimeException 包装，直接抛出，使异常更加精准
     *
     * @param throwable Throwable
     * @param <T>       泛型标记
     *
     * @return Throwable
     *
     * @throws T 泛型
     */
    @SuppressWarnings("unchecked")
    private static <T extends Throwable> T runtime(Throwable throwable) throws T {
        throw (T) throwable;
    }

    /**
     * 代理异常解包
     *
     * @param wrapped 包装过得异常
     *
     * @return 解包后的异常
     */
    public static Throwable unwrap(Throwable wrapped) {
        Throwable unwrapped = wrapped;
        while (true) {
            if (unwrapped instanceof InvocationTargetException exception) {
                unwrapped = exception.getTargetException();
            } else if (unwrapped instanceof UndeclaredThrowableException exception) {
                unwrapped = exception.getUndeclaredThrowable();
            } else {
                return unwrapped;
            }
        }
    }

    /**
     * 打印异常信息
     *
     * @param e 异常
     */
    public static void log(Throwable e) {
        Throwable cause = e.getCause();
        String message = (cause != null) ? cause.getMessage() : e.getMessage();
        Logs.error(EmojiSymbol.EXT, message != null ? message : e.toString());
    }


}
