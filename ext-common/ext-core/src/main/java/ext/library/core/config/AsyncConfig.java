package ext.library.core.config;

import ext.library.core.util.SpringUtil;
import ext.library.tool.constant.EmojiSymbol;
import ext.library.tool.runtime.Logs;
import ext.library.tool.util.ObjectUtil;
import ext.library.tool.util.StringUtil;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.core.task.VirtualThreadTaskExecutor;
import org.springframework.scheduling.annotation.AsyncConfigurer;

import java.util.Arrays;
import java.util.concurrent.Executor;

/**
 * 异步任务配置
 * <p>
 * 为 @Async 注解提供执行器配置：
 * <ul>
 *   <li>虚拟线程模式：使用 VirtualThreadTaskExecutor</li>
 *   <li>传统线程模式：使用 scheduledExecutorService</li>
 * </ul>
 */
@AutoConfiguration
public class AsyncConfig implements AsyncConfigurer {

    /**
     * 自定义 @Async 注解使用的执行器
     */
    @Override
    public Executor getAsyncExecutor() {
        if (SpringUtil.isVirtual()) {
            return new VirtualThreadTaskExecutor("异步任务-");
        }
        return SpringUtil.getBean("scheduledExecutorService");
    }

    /**
     * 异步执行异常处理
     * <p>
     * 异步方法抛出的未捕获异常无法传递给调用者，只能在此处理器中记录日志
     */
    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return (throwable, method, args) -> {
            String message = ObjectUtil.isNotEmpty(args)
                    ? StringUtil.format("异步方法 [{}] 抛出异常: {}, 参数: {}", method.getName(), throwable.getMessage(), Arrays.toString(args))
                    : StringUtil.format("异步方法 [{}] 抛出异常: {}", method.getName(), throwable.getMessage());
            Logs.error(EmojiSymbol.CORE, throwable, message);
        };
    }

}
