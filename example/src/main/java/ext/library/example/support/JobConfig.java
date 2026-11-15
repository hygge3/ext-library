package ext.library.example.support;

import ext.library.example.service.MailService;
import ext.library.example.service.ShareService;
import ext.library.job.CronJob;
import ext.library.job.JobHandler;
import java.util.Locale;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * ext-job：两条路径都用上。
 * <ul>
 *   <li>定时：每天 4 点清过期分享链接（多实例只有一个节点抢到）</li>
 *   <li>队列：分享通知邮件丢进队列异步发，接口不跟着 SMTP 慢</li>
 * </ul>
 * 两个处理器必须在不同队列上：队列名相同的后注册者会顶掉前者。
 */
@Configuration
public class JobConfig {

    public static final String QUEUE_MAIL = "share-mail";

    public static final String QUEUE_CLEANUP = "share-cleanup";

    /** 队列负载：只记必要字段，出队时再查库。 */
    public record ShareMail(String email, String noteTitle, String url) {
    }

    /** 定时任务负载，无参数。 */
    public record CleanupShares() {
    }

    @Bean
    public JobHandler<ShareMail> shareMailHandler(MailService mailService) {
        return JobHandler.of(QUEUE_MAIL, ShareMail.class,
                mail -> mailService.sendShareNotice(mail.email(), mail.noteTitle(), mail.url(), Locale.getDefault()));
    }

    @CronJob(cron = "0 0 4 * * *")
    @Bean
    public JobHandler<CleanupShares> cleanupSharesHandler(ShareService shareService) {
        return JobHandler.of(QUEUE_CLEANUP, CleanupShares.class, _ -> shareService.cleanupExpired());
    }
}
