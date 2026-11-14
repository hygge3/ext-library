package ext.library.job;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 定时任务注解
 * <p>
 * 标注在 {@link JobHandler} 实现类或其 {@code @Bean} 工厂方法上，到点后向该处理器的队列投递一个
 * 默认构造的负载，由既有轮询执行器消费——因此定时任务同样具备持久化、重试与多实例抢占，
 * 不另造一套内存调度。
 * <p>
 * 负载类型必须能用 {@code ClassUtil.newInstance} 构造（record 或带无参构造器的类）。
 *
 * <pre>{@code
 * @CronJob(cron = "0 0 2 * * *")
 * @Bean
 * JobHandler<DailyReport> dailyReportHandler(ReportService service) {
 *     return JobHandler.of(DailyReport.class, service::generate);
 * }
 *
 * record DailyReport() {}
 * }</pre>
 *
 * @since 4.1.0
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CronJob {

    /**
     * cron 表达式（Spring 六段式：秒 分 时 日 月 周）
     *
     * @return cron 表达式
     */
    String cron();

    /**
     * 时区
     *
     * @return 时区 ID，默认 JVM 时区
     */
    String zone() default "";

}
