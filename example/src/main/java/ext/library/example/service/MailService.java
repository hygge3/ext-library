package ext.library.example.service;

import ext.library.i18n.I18nMessageManager;
import ext.library.mail.model.MailDetails;
import ext.library.mail.model.MailSendResult;
import ext.library.mail.sender.MailSender;
import ext.library.resilience.degrade.Degrade;
import ext.library.resilience.retry.Retry;
import ext.library.tool.runtime.Logs;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

/**
 * 邮件出口：文案走 ext-i18n，发送失败由 ext-resilience 重试，连续失败走降级。
 * <p>
 * 降级方法签名必须与原方法一致（DegradeAspect 按参数类型找同签名方法）。
 */
@Service
@RequiredArgsConstructor
public class MailService {

    private static final String MODULE = "MAIL";

    private final ObjectProvider<MailSender> senderProvider;

    private final I18nMessageManager i18n;

    /** 邮箱验证码。文案随请求语言。 */
    public boolean sendVerifyCode(String email, String code, String ip, Locale locale) {
        String subject = i18n.get("mail.verify.subject", locale);
        String html = "<p>" + i18n.get("mail.verify.body", locale, code) + "</p><p>IP: " + ip + "</p>";
        return send(email, subject, html);
    }

    /** 分享链接通知。 */
    public boolean sendShareNotice(String email, String noteTitle, String url, Locale locale) {
        String subject = i18n.get("mail.share.subject", locale);
        String html = "<p>" + i18n.get("mail.share.body", locale, noteTitle) + "</p><p><a href=\"" + url + "\">"
                + url + "</a></p>";
        return send(email, subject, html);
    }

    @Retry(maxAttempts = 3, initialDelayMillis = 200, assumeIdempotent = true)
    @Degrade(name = "mail-send", minCalls = 5, failureRate = 50, window = 10, openDuration = 30,
            fallback = "sendFallback")
    public boolean send(String email, String subject, String html) {
        MailSender sender = senderProvider.getIfAvailable();
        if (sender == null) {
            // 没配 spring.mail.host 时 ext-mail 不装配 Bean，这里当作失败交给降级分支
            throw new IllegalStateException("未配置发件人（spring.mail.host）");
        }
        MailDetails details = MailDetails.builder().subject(subject).html(html).to(email).build();
        MailSendResult result = sender.send(details);
        if (!result.success()) {
            throw new IllegalStateException("邮件发送失败：" + result.errorMsg());
        }
        Logs.info(MODULE, "邮件已发送：to={} subject={}", email, subject);
        return true;
    }

    /** 熔断打开或重试耗尽后落到这里：不阻塞主流程，邮件内容只记日志。 */
    boolean sendFallback(String email, String subject, String html) {
        Logs.warn(MODULE, "邮件降级未发送：to={} subject={} html={}", email, subject, html);
        return false;
    }
}
