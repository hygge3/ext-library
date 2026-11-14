package ext.library.job.config;

import ext.library.tool.util.ClassUtil;
import ext.library.job.CronJob;
import ext.library.job.JobDispatcher;
import ext.library.job.JobHandler;
import ext.library.lock.store.LockStore;
import ext.library.tool.constant.EmojiSymbol;
import ext.library.tool.runtime.Logs;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronTrigger;

import java.time.Duration;
import java.time.ZoneId;

/**
 * 定时任务注册器
 * <p>
 * 扫描所有 {@link JobHandler} Bean，把标注了 {@link CronJob} 的按 cron 表达式挂到调度器上；
 * 到点只做一件事：向该处理器的队列投递默认构造的负载，执行仍由既有轮询执行器完成。
 *
 * @since 4.1.0
 */
public class CronJobScheduler implements SmartInitializingSingleton {

    /** 抢到锁后多久自动释放，仅用于实例崩溃时兼底 */
    private static final Duration LOCK_TIMEOUT = Duration.ofSeconds(30);

    private final ApplicationContext applicationContext;

    private final JobDispatcher dispatcher;

    private final TaskScheduler taskScheduler;

    private final LockStore lockStore;

    public CronJobScheduler(ApplicationContext applicationContext, JobDispatcher dispatcher,
                            TaskScheduler taskScheduler, LockStore lockStore) {
        this.applicationContext = applicationContext;
        this.dispatcher = dispatcher;
        this.taskScheduler = taskScheduler;
        this.lockStore = lockStore;
    }

    @Override
    public void afterSingletonsInstantiated() {
        for (String beanName : applicationContext.getBeanNamesForType(JobHandler.class)) {
            // findAnnotationOnBean 同时看实现类和 @Bean 工厂方法，两种注册方式都能识别
            CronJob cronJob = applicationContext.findAnnotationOnBean(beanName, CronJob.class);
            if (cronJob != null) {
                register(beanName, applicationContext.getBean(beanName, JobHandler.class), cronJob);
            }
        }
    }

    /**
     * 注册单个定时任务
     *
     * @param beanName Bean 名称
     * @param handler  处理器
     * @param cronJob  注解
     */
    private void register(String beanName, JobHandler<?> handler, CronJob cronJob) {
        String queueName = handler.queue();
        // 注册时先试建一次：负载类型不可实例化在启动阶段就暴露，不留到每轮调度再抛
        ClassUtil.newInstance(handler.payloadType());

        CronTrigger trigger = cronJob.zone().isBlank()
                ? new CronTrigger(cronJob.cron())
                : new CronTrigger(cronJob.cron(), ZoneId.of(cronJob.zone()));

        String lockKey = "ext:job:dispatch:" + queueName;
        taskScheduler.schedule(() -> {
            try {
                // 多实例同时到点会各投一次，抢锁让只有一个实例真正入队
                if (!lockStore.tryLock(lockKey, LOCK_TIMEOUT, Duration.ZERO, Duration.ZERO)) {
                    return;
                }
                try {
                    // 每轮新建负载：共享同一个实例，处理器改过的状态会带到下一轮
                    Object payload = ClassUtil.newInstance(handler.payloadType());
                    dispatcher.dispatch(queueName, payload);
                } finally {
                    lockStore.unlock(lockKey);
                }
            } catch (Exception e) {
                // 调度线程抛出异常会被取消后续执行，必须就地吞掉
                Logs.error(EmojiSymbol.JOB, e, "定时任务投递失败：queue={}", queueName);
            }
        }, trigger);

        Logs.info(EmojiSymbol.JOB, "载入定时任务：bean={}, queue={}, cron={}", beanName, queueName, cronJob.cron());
    }

}
