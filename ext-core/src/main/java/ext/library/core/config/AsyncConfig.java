package ext.library.core.config;

import ext.library.core.util.SpringUtil;
import ext.library.tool.core.Exceptions;
import ext.library.tool.util.ObjectUtil;
import ext.library.tool.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.core.task.VirtualThreadTaskExecutor;
import org.springframework.scheduling.annotation.AsyncConfigurer;

import java.util.Arrays;
import java.util.concurrent.Executor;

/**
 * 异步配置
 * <p>
 * 如果未使用虚拟线程则生效
 */
@Slf4j
@AutoConfiguration
public class AsyncConfig implements AsyncConfigurer {

    /**
     * 自定义 @Async 注解使用系统线程池
     */
    @Override
    public Executor getAsyncExecutor() {
        if (SpringUtil.isVirtual()) {
            return new VirtualThreadTaskExecutor("async-task-");
        }
        return SpringUtil.getBean("scheduledExecutorService");
    }

    /**
     * 异步执行异常处理
     */
    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return (throwable, method, objects) -> {
            Exceptions.log(throwable);
            String str = StringUtil.format("Exception message:{}, Method name:{}", throwable.getMessage(), method.getName());
            if (ObjectUtil.isNotEmpty(objects)) {
                str = str.concat(", Parameter value:[").concat(Arrays.toString(objects)).concat("]");
            }
            throw new RuntimeException(str);
        };
    }

}