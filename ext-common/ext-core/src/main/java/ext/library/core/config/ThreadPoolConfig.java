package ext.library.core.config;

import ext.library.core.properties.ThreadPoolProperties;
import ext.library.tool.constant.EmojiSymbol;
import ext.library.tool.runtime.Logs;
import ext.library.tool.runtime.Runtimes;
import ext.library.tool.runtime.Threads;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 线程池配置
 */
@AutoConfiguration
@EnableConfigurationProperties(ThreadPoolProperties.class)
public class ThreadPoolConfig {

    @ConditionalOnProperty(prefix = "ext.thread-pool", name = "enabled", havingValue = "true")
    @Bean(name = "threadPoolTaskExecutor")
    public ThreadPoolTaskExecutor threadPoolTaskExecutor(ThreadPoolProperties threadPoolProperties) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setThreadNamePrefix("pool-");
        executor.setCorePoolSize(threadPoolProperties.getCorePoolSize());
        executor.setMaxPoolSize(threadPoolProperties.getMaxPoolSize());
        executor.setQueueCapacity(threadPoolProperties.getQueueCapacity());
        executor.setKeepAliveSeconds(threadPoolProperties.getKeepAliveSeconds());
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.initialize();
        Logs.info(EmojiSymbol.CORE, "载入模块: Spring 线程池");
        return executor;
    }

    /**
     * 执行周期性或定时任务
     */
    @Bean(name = "scheduledExecutorService", destroyMethod = "shutdown")
    protected ScheduledExecutorService scheduledExecutorService() {
        final int corePoolSize = Runtimes.getCpuNum() + 1;
        final AtomicInteger threadNumber = new AtomicInteger(1);

        ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(corePoolSize,
                r -> {
                    Thread t = new Thread(r, "调度线程-" + threadNumber.getAndIncrement());
                    t.setDaemon(true);
                    t.setUncaughtExceptionHandler(Threads::printException);
                    return t;
                });
        Logs.info(EmojiSymbol.CORE, "载入模块: Spring 调度线程池");
        return executor;
    }

}
