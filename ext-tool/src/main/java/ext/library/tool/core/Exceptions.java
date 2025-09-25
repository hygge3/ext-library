package ext.library.tool.core;


import ext.library.tool.biz.exception.BizException;
import ext.library.tool.util.ObjectUtil;
import ext.library.tool.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.annotation.Nonnull;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.UndeclaredThrowableException;

/**
 * 异常处理工具类
 */
public class Exceptions {
    private static final Logger log = LoggerFactory.getLogger(Exceptions.class);

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
     * 主动抛出异常
     *
     * @param message 错误消息模板
     * @param args    参数
     *
     * @return {RuntimeException}
     */
    public static BizException throwOut(@Nonnull String message, Object... args) {
        if (ObjectUtil.isEmpty(args)) {
            return new BizException(message);
        }
        return new BizException(StringUtil.format(message, args));
    }

    /**
     * 主动抛出异常
     *
     * @param message the pattern string
     * @param args    object(s) to format
     *
     * @return {RuntimeException}
     */
    public static BizException throwOut(Exception e, @Nonnull String message, Object... args) {
        if (ObjectUtil.isEmpty(args)) {
            return new BizException(message);
        }
        return new BizException(StringUtil.format(message, args), e);
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
    public static void log(@Nonnull Throwable e) {
        // 在 getMessage() 获取异常名称的基础上，添加了异常原因
        log.error(e.getCause().getMessage());
    }


}