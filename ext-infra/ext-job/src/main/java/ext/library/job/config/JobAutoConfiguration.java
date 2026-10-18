package ext.library.job.config;

import ext.library.job.JobDispatcher;
import ext.library.job.JobExecutor;
import ext.library.job.JobHandler;
import ext.library.postgres.queue.PostgresQueue;
import ext.library.tool.constant.EmojiSymbol;
import ext.library.tool.runtime.Logs;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
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
     */
    public static class JobPollingTask implements InitializingBean {

        private final JobProperties properties;

        private final JobExecutor executor;

        private final JobDispatcher dispatcher;

        public JobPollingTask(JobProperties properties, JobExecutor executor, JobDispatcher dispatcher) {
            this.properties = properties;
            this.executor = executor;
            this.dispatcher = dispatcher;
        }

        @Override
        public void afterPropertiesSet() {
            if (dispatcher.isEmpty()) {
                Logs.info(EmojiSymbol.JOB, "未注册任务处理器，跳过启动恢复");
                return;
            }
            if (!properties.isRecoverOnStartup()) {
                return;
            }
            for (String queue : dispatcher.queues()) {
                if (properties.isCleanupOnStartup()) {
                    dispatcher.cleanup(queue, properties.getCompletedRetention());
                }
                dispatcher.resetStuck(queue, properties.getStuckTimeout());
            }
            Logs.info(EmojiSymbol.JOB, "任务启动恢复完成：queues={}", dispatcher.queues());
        }

        /**
         * 轮询执行一轮
         * <p>
         * 整轮异常必须就地吞掉，否则 Spring 调度器会取消后续所有执行。
         */
        @Scheduled(fixedDelayString = "#{@jobProperties.pollIntervalMillis}")
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
