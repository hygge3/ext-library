package ext.library.job.config;

import ext.library.job.JobDispatcher;
import ext.library.job.JobExecutor;
import ext.library.job.JobHandler;
import ext.library.lock.store.LockStore;
import ext.library.postgres.queue.PostgresQueue;
import ext.library.tool.constant.EmojiSymbol;
import ext.library.tool.runtime.Logs;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.List;

/**
 * 任务执行自动配置
 * <p>
 * 依赖 {@code ext-postgres} 与容器中的 {@link PostgresQueue}。
 * 未注册任何 {@link JobHandler} 时只注册投递器与执行器，不启动轮询。
 *
 * @since 4.1.0
 */
@AutoConfiguration
@EnableScheduling
@EnableConfigurationProperties(JobProperties.class)
@ConditionalOnProperty(prefix = JobProperties.PREFIX, name = "enabled", havingValue = "true",
        matchIfMissing = true)
public class JobAutoConfiguration {

    /** 定时任务只做「投递进队列」，2 个线程足够，压力在执行器那边。 */
    private static final int SCHEDULER_POOL_SIZE = 2;

    /**
     * 调度器
     * <p>
     * 自己建而不是等 Spring Boot 的 {@code TaskSchedulingAutoConfiguration}：它只在容器里已存在调度注解处理器时才建
     * {@code TaskScheduler}，而 {@code @EnableScheduling} 声明在自动配置上，处理时机在其后判断，拿到的是「没有」。
     * 消费方已注册自己的 {@code TaskScheduler} 时本方法退让。
     *
     * @return 调度器
     */
    @Bean
    @ConditionalOnMissingBean
    public TaskScheduler extTaskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(SCHEDULER_POOL_SIZE);
        scheduler.setThreadNamePrefix("ext-job-");
        return scheduler;
    }

    /**
     * 任务投递器
     *
     * @param queue    队列
     * @param handlers 处理器
     *
     * @return 投递器
     */
    @Bean
    @ConditionalOnMissingBean
    public JobDispatcher jobDispatcher(PostgresQueue queue, List<JobHandler<?>> handlers) {
        return new JobDispatcher(queue, handlers);
    }

    /**
     * 轮询执行器
     *
     * @param queue      队列
     * @param dispatcher 投递器
     * @param properties 配置
     *
     * @return 执行器
     */
    @Bean
    @ConditionalOnMissingBean
    public JobExecutor jobExecutor(PostgresQueue queue, JobDispatcher dispatcher, JobProperties properties) {
        return new JobExecutor(queue, dispatcher.handlers(), properties.getBatchSize());
    }

    /**
     * 定时任务注册器
     * <p>
     * 把标注了 {@code @CronJob} 的处理器按 cron 表达式挂到调度器上，到点投递进队列。
     *
     * @param applicationContext 容器
     * @param dispatcher         投递器
     * @param taskScheduler      调度器
     * @param lockStore          分布式锁
     *
     * @return 注册器
     */
    @Bean
    @ConditionalOnMissingBean
    public CronJobScheduler cronJobScheduler(ApplicationContext applicationContext, JobDispatcher dispatcher,
                                             TaskScheduler taskScheduler, LockStore lockStore) {
        return new CronJobScheduler(applicationContext, dispatcher, taskScheduler, lockStore);
    }

    /**
     * 轮询任务
     *
     * @param properties 配置
     * @param executor   执行器
     * @param dispatcher 投递器
     *
     * @return 轮询任务
     */
    @Bean
    @ConditionalOnMissingBean
    public JobPollingTask jobPollingTask(JobProperties properties, JobExecutor executor,
            JobDispatcher dispatcher) {
        return new JobPollingTask(properties, executor, dispatcher);
    }

    /**
     * 轮询任务
     * <p>
     * 启动恢复放到 {@link ApplicationReadyEvent}：它要读写队列表，而建表在
     * {@code PostgresSchemaInitializer} 的 {@code InitializingBean} 里，两者谁先谁后取决于 Bean 注册顺序
     * （自动配置按类名排序，job 在 postgres 之前），在 {@code afterPropertiesSet} 里查表会撞上「关系不存在」。
     */
    public static class JobPollingTask implements ApplicationListener<ApplicationReadyEvent> {

        private final JobProperties properties;

        private final JobExecutor executor;

        private final JobDispatcher dispatcher;

        public JobPollingTask(JobProperties properties, JobExecutor executor, JobDispatcher dispatcher) {
            this.properties = properties;
            this.executor = executor;
            this.dispatcher = dispatcher;
        }

        @Override
        public void onApplicationEvent(ApplicationReadyEvent event) {
            if (dispatcher.isEmpty()) {
                Logs.info(EmojiSymbol.JOB, "未注册任务处理器，跳过启动恢复");
                return;
            }
            if (!properties.isRecoverOnStartup()) {
                return;
            }
            if (properties.isCleanupOnStartup()) {
                cleanup();
            }
            for (String queue : dispatcher.queues()) {
                dispatcher.resetStuck(queue, properties.getStuckTimeout());
            }
            Logs.info(EmojiSymbol.JOB, "任务启动恢复完成：queues={}", dispatcher.queues());
        }

        /**
         * 清理过期的已完成任务与死信
         * <p>
         * 不清理就会无限堆积：COMPLETED 按完成时间算，FAILED 按创建时间算。
         */
        @Scheduled(fixedDelayString = "${ext.job.cleanup-interval-millis:3600000}")
        public void cleanup() {
            if (dispatcher.isEmpty()) {
                return;
            }
            for (String queue : dispatcher.queues()) {
                int completed = dispatcher.cleanup(queue, properties.getCompletedRetention());
                int failed = dispatcher.cleanupFailed(queue, properties.getFailedRetention());
                if (completed + failed > 0) {
                    Logs.info(EmojiSymbol.JOB, "清理任务：queue={}, completed={}, failed={}", queue, completed, failed);
                }
            }
        }

        /**
         * 轮询执行一轮
         * <p>
         * 整轮异常必须就地吞掉，否则 Spring 调度器会取消后续所有执行。
         */
        @Scheduled(fixedDelayString = "${ext.job.poll-interval-millis:500}")
        public void poll() {
            if (dispatcher.isEmpty()) {
                return;
            }
            try {
                executor.runOnce();
            } catch (Exception e) {
                Logs.error(EmojiSymbol.JOB, e, "任务轮询异常");
            }
        }

    }

}
