package ext.library.core.config;

import ext.library.core.util.SpringUtil;
import ext.library.tool.constant.EmojiSymbol;
import ext.library.tool.runtime.Logs;
import ext.library.tool.util.StringUtil;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.core.task.VirtualThreadTaskExecutor;
import org.springframework.scheduling.annotation.AsyncConfigurer;

import java.util.Arrays;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

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
@ConditionalOnMissingBean(AsyncConfigurer.class)
public class AsyncConfiguration implements AsyncConfigurer {

    /**
     * 虚拟线程执行器缓存
     * <p>
     * {@link #getAsyncExecutor()} 由 Spring 每次调用都可能新建一个 {@link VirtualThreadTaskExecutor}，
     * 而它 {@code afterPropertiesSet()} 时建了 {@code ExecutorService} 却从不 shutdown，重复获取会一直泄漏。
     * <p>
     * 只经 {@link #virtualThreadExecutor()} 访问，因此不需要 volatile。
     */
    private Executor virtualThreadExecutor;

    /**
     * 自定义 @Async 注解使用的执行器
     */
    @Override
    public Executor getAsyncExecutor() {
        return SpringUtil.isVirtual() ? virtualThreadExecutor() : scheduledExecutor();
    }

    private synchronized Executor virtualThreadExecutor() {
        if (this.virtualThreadExecutor == null) {
            this.virtualThreadExecutor = new VirtualThreadTaskExecutor("异步任务-");
        }
        return this.virtualThreadExecutor;
    }

    /**
     * 查找调度执行器
     * <p>
     * 按类型而不是按名字找：消费方自定义了同名 Bean、或排除了自动配置时，
     * {@code getBean("scheduledExecutorService")} 会硬失败并拖垮整个应用启动。
     *
     * @return 调度执行器，找不到时回退到默认执行器
     */
    private static Executor scheduledExecutor() {
        String[] names = SpringUtil.getBeanNamesForType(ScheduledExecutorService.class);
        return names.length > 0 ? SpringUtil.getBean(names[0])
                : Executors.newSingleThreadScheduledExecutor();
    }

    /**
     * 异步执行异常处理
     * <p>
     * 异步方法抛出的未捕获异常无法传递给调用者，只能在此处理器中记录日志
     */
    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return (throwable, method, args) -> {
            // args 是 Object[]，不能走基于 Collection 的 isNotEmpty，否则参数永远不会被记录
            String params = args != null && args.length > 0 ? "，参数：" + Arrays.toString(args) : "";
            Logs.error(EmojiSymbol.CORE, throwable,
                    StringUtil.format("异步方法 [{}] 抛出异常：{}{}", method.getName(), throwable.getMessage(), params));
        };
    }

}
