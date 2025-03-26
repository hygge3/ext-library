package ext.library.mail.sender;

import jakarta.annotation.Nonnull;
import jakarta.mail.MessagingException;

import ext.library.mail.event.MailSendEvent;
import ext.library.mail.model.MailDetails;
import ext.library.mail.model.MailSendInfo;
import java.io.File;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.util.StringUtils;

/**
 * 邮件发送器实现
 */
@Slf4j
@RequiredArgsConstructor
public class MailSenderImpl implements MailSender {

    final JavaMailSender mailSender;

    final ApplicationEventPublisher eventPublisher;

    /**
     * 配置文件中我的邮箱
     */
    @Value("${spring.mail.username}")
    private String defaultFrom;

    /**
     * 发送邮件
     *
     * @param mailDetails 邮件参数
     * @return boolean 发送是否成功
     */
    @Override
    public MailSendInfo sendMail(MailDetails mailDetails) {
        MailSendInfo mailSendInfo = new MailSendInfo(mailDetails);
        mailSendInfo.setSentDate(LocalDateTime.now());

        try {
            // 1.检测邮件
            checkMail(mailDetails);
            // 2.发送邮件
            sendMimeMail(mailDetails);
            mailSendInfo.setSuccess(true);
        } catch (Exception e) {
            mailSendInfo.setSuccess(false);
            mailSendInfo.setErrorMsg(e.getMessage());
            log.error("[📧] Sending email failed:[{}]", mailDetails, e);
        } finally {
            // 发布邮件发送事件
            eventPublisher.publishEvent(new MailSendEvent(mailSendInfo));
        }
        return mailSendInfo;
    }

    /**
     * 构建复杂邮件信息类
     *
     * @param mailDetails 邮件发送设置
     */
    private void sendMimeMail(@Nonnull MailDetails mailDetails) throws MessagingException {
        // true 表示支持复杂类型
        MimeMessageHelper messageHelper = new MimeMessageHelper(mailSender.createMimeMessage(), true);
        String from = StringUtils.hasText(mailDetails.getFrom()) ? mailDetails.getFrom() : this.defaultFrom;
        messageHelper.setFrom(from);
        messageHelper.setSubject(mailDetails.getSubject());
        if (mailDetails.getTo() != null && mailDetails.getTo().length > 0) {
            messageHelper.setTo(mailDetails.getTo());
        }
        if (mailDetails.getCc() != null && mailDetails.getCc().length > 0) {
            messageHelper.setCc(mailDetails.getCc());
        }
        if (mailDetails.getBcc() != null && mailDetails.getBcc().length > 0) {
            messageHelper.setBcc(mailDetails.getBcc());
        }
        // 是否展示 html
        boolean showHtml = mailDetails.getShowHtml() != null && mailDetails.getShowHtml();
        messageHelper.setText(mailDetails.getContent(), showHtml);
        if (mailDetails.getFiles() != null) {
            for (File file : mailDetails.getFiles()) {
                messageHelper.addAttachment(file.getName(), file);
            }
        }

        mailSender.send(messageHelper.getMimeMessage());
        log.info("[📧] 邮件发送成功:[{}]", mailDetails);
    }

}
