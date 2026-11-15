package ext.library.job.config;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import ext.library.job.JobHandler;
import ext.library.lock.store.LockStore;
import ext.library.postgres.properties.PostgresProperties;
import ext.library.postgres.queue.Job;
import ext.library.postgres.queue.PostgresQueue;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

/**
 * 自动配置自给调度器：容器里没有 {@link TaskScheduler} 时也能装配。
 * <p>
 * Spring Boot 的 {@code TaskSchedulingAutoConfiguration} 只在调度注解处理器已注册时才建调度器，而 {@code @EnableScheduling}
 * 声明在本自动配置上，判断发生在其后，因此这里必须自己兜住。
 */
@DisplayName("任务自动配置装配测试")
class JobAutoConfigurationWiringTest {

    record Payload() {
    }

    /** 不碰数据库的桩：出队永远为空，清理与恢复都是空操作。 */
    static class NoopQueue extends PostgresQueue {

        NoopQueue() {
            super(null, new PostgresProperties());
        }

        @Override
        public List<Job> dequeue(String queue, int limit) {
            return List.of();
        }

        @Override
        public Optional<Job> dequeue(String queue) {
            return Optional.empty();
        }

        @Override
        public int cleanupCompleted(String queue, Duration olderThan) {
            return 0;
        }

        @Override
        public int resetStuckJobs(String queue, Duration timeout) {
            return 0;
        }
    }

    static class NoopLockStore implements LockStore {

        @Override
        public boolean tryLock(String key, Duration timeout, Duration waitTime, Duration loopInterval) {
            return true;
        }

        @Override
        public void unlock(String key) {
        }
    }

    @Test
    void wiresWithoutBootProvidedTaskScheduler() {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext()) {
            context.registerBean(NoopQueue.class, NoopQueue::new);
            context.registerBean(LockStore.class, NoopLockStore::new);
            context.registerBean("noopHandler", JobHandler.class,
                    () -> JobHandler.of("wiring-queue", Payload.class, _ -> {
                    }));
            context.register(JobAutoConfiguration.class);
            context.refresh();

            assertNotNull(context.getBean(TaskScheduler.class), "容器里应当有调度器");
            assertNotNull(context.getBean(CronJobScheduler.class), "定时任务注册器应当装配成功");
        }
    }

    @Test
    void backsOffWhenConsumerProvidesScheduler() {
        ThreadPoolTaskScheduler mine = new ThreadPoolTaskScheduler();
        mine.setPoolSize(1);
        mine.initialize();

        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext()) {
            context.registerBean(NoopQueue.class, NoopQueue::new);
            context.registerBean(LockStore.class, NoopLockStore::new);
            context.registerBean(TaskScheduler.class, () -> mine);
            context.register(JobAutoConfiguration.class);
            context.refresh();

            assertSame(mine, context.getBean(TaskScheduler.class), "消费方的调度器优先");
        }
    }
}
