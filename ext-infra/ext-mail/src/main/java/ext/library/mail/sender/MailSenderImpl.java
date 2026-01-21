package ext.library.mail.sender;

import ext.library.mail.event.MailSendEvent;
import ext.library.mail.model.MailDetails;
import ext.library.mail.model.MailSendResult;
import ext.library.tool.constant.EmojiSymbol;
import ext.library.tool.exception.ExtException;
import ext.library.tool.runtime.Logs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import jakarta.mail.MessagingException;

/**
 * 邮件发送器实现
 */
public class MailSenderImpl implements MailSender {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private final JavaMailSender mailSender;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * 默认发件人（配置文件中的邮箱用户名）
     */
    @Value("${spring.mail.username}")
    private String defaultFrom;

    public MailSenderImpl(JavaMailSender mailSender, ApplicationEventPublisher eventPublisher) {
        this.mailSender = mailSender;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public MailSendResult send(MailDetails mailDetails) {
        MailSendResult result = null;
        try {
            // 1. 校验邮件
            checkMail(mailDetails);
            // 2. 发送邮件
            sendMimeMessage(mailDetails);
            result = MailSendResult.success(mailDetails);
            Logs.info(EmojiSymbol.MAIL, "邮件发送成功: subject={}, to={}", mailDetails.getSubject(), mailDetails.getTo());
        } catch (MailSendException | MessagingException e) {
            result = MailSendResult.failure(mailDetails, e.getMessage());
            Logs.error(EmojiSymbol.MAIL, e, "邮件发送失败: {}", e.getMessage());
            throw new ExtException(EmojiSymbol.MAIL, "邮件发送失败");
        } finally {
            // 发布邮件发送事件
            if (result != null) {
                eventPublisher.publishEvent(new MailSendEvent(result));
            }
        }
        return result;
    }

    /**
     * 检查邮件是否符合发送标准
     *
     * @param mailDetails 邮件详情
     */
    private void checkMail(MailDetails mailDetails) {
        boolean noTo = CollectionUtils.isEmpty(mailDetails.getTo());
        boolean noCc = CollectionUtils.isEmpty(mailDetails.getCc());
        boolean noBcc = CollectionUtils.isEmpty(mailDetails.getBcc());
        if (noTo && noCc && noBcc) {
            throw new ExtException(EmojiSymbol.MAIL, "邮件应该至少有一个收件人");
        }
        if (!StringUtils.hasText(mailDetails.getSubject())) {
            throw new ExtException(EmojiSymbol.MAIL, "邮件主题不能为空");
        }
        if (!StringUtils.hasText(mailDetails.getContent())) {
            throw new ExtException(EmojiSymbol.MAIL, "邮件内容不能为空");
        }
    }

    /**
     * 构建并发送 MIME 邮件
     *
     * @param mailDetails 邮件详情
     */
    private void sendMimeMessage(MailDetails mailDetails) throws MessagingException {
        // true 表示支持复杂类型（附件等）
        MimeMessageHelper helper = new MimeMessageHelper(mailSender.createMimeMessage(), true);

        // 发件人
        String from = StringUtils.hasText(mailDetails.getFrom()) ? mailDetails.getFrom() : this.defaultFrom;
        helper.setFrom(from);

        // 主题
        helper.setSubject(mailDetails.getSubject());

        // 收件人
        if (!CollectionUtils.isEmpty(mailDetails.getTo())) {
            helper.setTo(mailDetails.getTo().toArray(new String[0]));
        }

        // 抄送
        if (!CollectionUtils.isEmpty(mailDetails.getCc())) {
            helper.setCc(mailDetails.getCc().toArray(new String[0]));
        }

        // 密送
        if (!CollectionUtils.isEmpty(mailDetails.getBcc())) {
            helper.setBcc(mailDetails.getBcc().toArray(new String[0]));
        }

        // 正文
        helper.setText(mailDetails.getContent(), mailDetails.isHtml());

        // 附件
        if (!CollectionUtils.isEmpty(mailDetails.getAttachments())) {
            for (MailDetails.Attachment attachment : mailDetails.getAttachments()) {
                helper.addAttachment(attachment.name(), attachment.source());
            }
        }

        mailSender.send(helper.getMimeMessage());
    }
}
