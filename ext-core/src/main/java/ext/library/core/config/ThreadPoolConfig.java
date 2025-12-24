package ext.library.core.config;

import ext.library.core.properties.ThreadPoolProperties;
import ext.library.tool.constant.EmojiSymbol;
import ext.library.tool.constant.Holder;
import ext.library.tool.core.Logs;
import ext.library.tool.core.Threads;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import jakarta.annotation.PreDestroy;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 线程池配置
 **/
@AutoConfiguration
@EnableConfigurationProperties(ThreadPoolProperties.class)
public class ThreadPoolConfig {
    /**
     * 核心线程数 = cpu 核心数 + 1
     */
    private final int core = Holder.CPU_CORE_NUM + 1;
    private ScheduledExecutorService scheduledExecutorService;

    @ConditionalOnProperty(prefix = "thread-pool", name = "enabled", havingValue = "true")
    @Bean(name = "threadPoolTaskExecutor")
    public ThreadPoolTaskExecutor threadPoolTaskExecutor(ThreadPoolProperties threadPoolProperties) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setThreadNamePrefix("线程池-");
        executor.setCorePoolSize(threadPoolProperties.getCorePoolSize());
        executor.setMaxPoolSize(threadPoolProperties.getMaxPoolSize());
        executor.setQueueCapacity(threadPoolProperties.getQueueCapacity());
        executor.setKeepAliveSeconds(threadPoolProperties.getKeepAliveSeconds());
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.initialize();
        Logs.info(EmojiSymbol.CORE, "Spring 线程池模块载入成功");
        return executor;
    }

    /**
     * 执行周期性或定时任务
     */
    @Bean(name = "scheduledExecutorService")
    protected ScheduledExecutorService scheduledExecutorService() {
        final AtomicInteger threadNumber = new AtomicInteger(1);

        ScheduledThreadPoolExecutor scheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(core,
                r -> {
                    Thread t = new Thread(r, "调度线程-" + threadNumber.getAndIncrement());
                    t.setDaemon(true);
                    t.setUncaughtExceptionHandler(Threads::printException);
                    return t;
                });
        this.scheduledExecutorService = scheduledThreadPoolExecutor;
        Logs.info(EmojiSymbol.CORE, "载入模块:Spring 调度线程池");
        return scheduledThreadPoolExecutor;
    }

    /**
     * 销毁事件
     */
    @PreDestroy
    public void destroy() {
        try {
            Logs.info(EmojiSymbol.CORE, "关闭后台任务线程池");
            Threads.shutdownAndAwaitTermination(scheduledExecutorService);
        } catch (Exception e) {
            Logs.error(EmojiSymbol.CORE, e, e.getMessage());
        }
    }

}
