package ext.library.job.config;

import ext.library.job.CronJob;
import ext.library.job.JobDispatcher;
import ext.library.job.JobHandler;
import ext.library.lock.store.LockStore;
import ext.library.postgres.queue.PostgresQueue;
import ext.library.postgres.properties.PostgresProperties;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 定时任务注册器测试
 */
@DisplayName("定时任务注册器测试")
public class CronJobSchedulerTest {

    /**
     * 用于验证负载可默认构造
     */
    public record Payload() {
    }

    @Configuration
    static class Conf {

        @CronJob(cron = "*/1 * * * * *")
        @Bean
        JobHandler<Payload> cronHandler() {
            return JobHandler.of("cron-queue", Payload.class, payload -> {
            });
        }

        @Bean
        JobHandler<Payload> plainHandler() {
            return JobHandler.of("plain-queue", Payload.class, payload -> {
            });
        }
    }

    /**
     * 只记录投递、不碰数据库的队列桩
     */
    static class CapturingQueue extends PostgresQueue {

        final CountDownLatch latch = new CountDownLatch(1);

        volatile String lastQueue;

        CapturingQueue() {
            super(null, new PostgresProperties());
        }

        @Override
        public long enqueue(String queue, Object payload) {
            lastQueue = queue;
            latch.countDown();
            return 1L;
        }
    }

    /**
     * 可切换成抢锁成功/失败的锁桩
     */
    static class StubLockStore implements LockStore {

        volatile boolean granted = true;

        @Override
        public boolean tryLock(String lockKey, Duration timeout, Duration waitTime, Duration loopInterval) {
            return granted;
        }

        @Override
        public void unlock(String lockKey) {
        }
    }

    @Test
    @DisplayName("只注册标注了 @CronJob 的处理器，到点投递进对应队列")
    void schedulesAnnotatedHandlerOnly() throws Exception {
        try (AnnotationConfigApplicationContext context =
                     new AnnotationConfigApplicationContext(Conf.class)) {
            CapturingQueue queue = new CapturingQueue();
            JobHandler<?> cronHandler = context.getBean("cronHandler", JobHandler.class);
            JobHandler<?> plainHandler = context.getBean("plainHandler", JobHandler.class);
            JobDispatcher dispatcher = new JobDispatcher(queue, List.of(cronHandler, plainHandler));
            ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
            scheduler.initialize();

            try {
                new CronJobScheduler(context, dispatcher, scheduler, new StubLockStore()).afterSingletonsInstantiated();

                assertTrue(queue.latch.await(3, TimeUnit.SECONDS), "cron 未触发投递");
                assertEquals("cron-queue", queue.lastQueue);
            } finally {
                scheduler.shutdown();
            }
        }
    }

    @Test
    @DisplayName("抢不到锁的实例不投递")
    void skipsDispatchWhenLockDenied() throws Exception {
        try (AnnotationConfigApplicationContext context =
                     new AnnotationConfigApplicationContext(Conf.class)) {
            CapturingQueue queue = new CapturingQueue();
            JobHandler<?> cronHandler = context.getBean("cronHandler", JobHandler.class);
            JobDispatcher dispatcher = new JobDispatcher(queue, List.of(cronHandler));
            ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
            scheduler.initialize();

            StubLockStore lockStore = new StubLockStore();
            lockStore.granted = false;

            try {
                new CronJobScheduler(context, dispatcher, scheduler, lockStore).afterSingletonsInstantiated();

                // cron 每秒一跑，两个周期都没投递才能说明是被锁拦下的
                assertFalse(queue.latch.await(2, TimeUnit.SECONDS), "抢锁失败仍然投递了");
            } finally {
                scheduler.shutdown();
            }
        }
    }

}
