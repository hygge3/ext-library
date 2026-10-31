package ext.library.core.config;

import ext.library.core.properties.ThreadPoolProperties;
import ext.library.tool.constant.EmojiSymbol;
import ext.library.tool.runtime.Logs;
import ext.library.tool.runtime.Runtimes;
import ext.library.tool.runtime.Threads;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 线程池配置
 */
@AutoConfiguration
@EnableConfigurationProperties(ThreadPoolProperties.class)
public class ThreadPoolConfiguration {

    @ConditionalOnProperty(prefix = "ext.thread-pool", name = "enabled", havingValue = "true")
    @Bean(name = "threadPoolTaskExecutor")
    public ThreadPoolTaskExecutor threadPoolTaskExecutor(@NonNull ThreadPoolProperties threadPoolProperties) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setThreadNamePrefix("pool-");
        executor.setCorePoolSize(threadPoolProperties.getCorePoolSize());
        executor.setMaxPoolSize(threadPoolProperties.getMaxPoolSize());
        executor.setQueueCapacity(threadPoolProperties.getQueueCapacity());
        executor.setKeepAliveSeconds(threadPoolProperties.getKeepAliveSeconds());
        executor.setRejectedExecutionHandler(new RejectAndLogPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(threadPoolProperties.getAwaitTerminationSeconds());
        // 不要手动 initialize()：@Bean 容器会在 afterPropertiesSet() 里再调一次，
        // 第二次会直接 new ThreadPoolExecutor 覆盖字段，第一次创建的实例被丢弃且从未 shutdown。
        Logs.info(EmojiSymbol.CORE, "载入模块：Spring 线程池");
        return executor;
    }

    /**
     * 执行周期性或定时任务
     */
    @Bean(name = "scheduledExecutorService", destroyMethod = "shutdown")
    @ConditionalOnMissingBean(name = "scheduledExecutorService")
    public ScheduledExecutorService scheduledExecutorService() {
        final int corePoolSize = Runtimes.getCpuNum() + 1;
        final AtomicInteger threadNumber = new AtomicInteger(1);

        ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(corePoolSize, threadFactory(threadNumber));
        // 已取消的延迟/周期任务立即出队，否则会滞留到延迟到期才被 GC
        executor.setRemoveOnCancelPolicy(true);
        executor.setExecuteExistingDelayedTasksAfterShutdownPolicy(false);
        executor.setContinueExistingPeriodicTasksAfterShutdownPolicy(false);
        // 调度任务不允许静默丢弃
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy());
        Logs.info(EmojiSymbol.CORE, "载入模块：Spring 调度线程池");
        return executor;
    }

    private static ThreadFactory threadFactory(AtomicInteger threadNumber) {
        return r -> {
            // 非 daemon：它是 @Async 的默认执行器，daemon 线程会在 JVM 退出时被直接杀掉，
            // 配合 destroyMethod="shutdown"（只发信号不等待）会导致未完成的异步任务无声丢失。
            Thread t = new Thread(r, "调度线程-" + threadNumber.getAndIncrement());
            t.setUncaughtExceptionHandler(Threads::printException);
            return t;
        };
    }

    /**
     * 拒绝策略：执行器已关闭时明确报错，否则回退到调用线程执行
     * <p>
     * JDK 的 {@link ThreadPoolExecutor.CallerRunsPolicy} 在 {@code isShutdown()} 时
     * 什么都不做也不抛异常——关闭期提交的任务被静默丢弃，"执行器已死"被伪装成"执行成功"。
     */
    private static final class RejectAndLogPolicy implements RejectedExecutionHandler {

        @Override
        public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
            if (executor.isShutdown()) {
                Logs.error(EmojiSymbol.CORE, "线程池已关闭，任务被拒绝：{}", r);
                throw new RejectedExecutionException("线程池已关闭");
            }
            Logs.warn(EmojiSymbol.CORE, "线程池饱和，任务回退到调用线程执行：{}", r);
            if (!executor.getQueue().offer(r)) {
                r.run();
            }
        }
    }

}
