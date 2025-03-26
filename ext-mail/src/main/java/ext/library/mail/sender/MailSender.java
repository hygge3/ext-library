package ext.library.mail.sender;

import jakarta.annotation.Nonnull;

import ext.library.mail.model.MailDetails;
import ext.library.mail.model.MailSendInfo;
import java.util.List;
import org.springframework.mail.MailSendException;
import org.springframework.util.StringUtils;

/**
 * 邮件发送器
 */
public interface MailSender {

    /**
     * 发送邮件
     *
     * @param mailDetails 邮件信息
     * @return MailSendInfo
     */
    MailSendInfo sendMail(MailDetails mailDetails);

    /**
     * 发送邮件
     *
     * @param subject  主题
     * @param content  邮件正文
     * @param showHtml 是否将正文渲染为 html
     * @param to       收件人，多个邮箱使用，号间隔
     * @return MailSendInfo
     */
    default MailSendInfo sendMail(String subject, String content, boolean showHtml, String... to) {
        MailDetails mailDetails = new MailDetails();
        mailDetails.setShowHtml(showHtml);
        mailDetails.setSubject(subject);
        mailDetails.setContent(content);
        mailDetails.setTo(to);
        return sendMail(mailDetails);
    }

    /**
     * 发送普通文本邮件
     *
     * @param subject 主题
     * @param content 邮件正文
     * @param to      收件人
     * @return MailSendInfo
     */
    default MailSendInfo sendTextMail(String subject, String content, String... to) {
        return sendMail(subject, content, false, to);
    }

    /**
     * 发送普通文本邮件
     *
     * @param subject 主题
     * @param content 邮件正文
     * @param to      收件人
     * @return MailSendInfo
     */
    default MailSendInfo sendTextMail(String subject, String content, @Nonnull List<String> to) {
        return sendMail(subject, content, false, to.toArray(new String[0]));
    }

    /**
     * 发送 Html 邮件
     *
     * @param subject 主题
     * @param content 邮件正文
     * @param to      收件人
     * @return MailSendInfo
     */
    default MailSendInfo sendHtmlMail(String subject, String content, String... to) {
        return sendMail(subject, content, true, to);
    }

    /**
     * 发送 Html 邮件
     *
     * @param subject 主题
     * @param content 邮件正文
     * @param to      收件人
     * @return MailSendInfo 邮件发送结果信息
     */
    default MailSendInfo sendHtmlMail(String subject, String content, @Nonnull List<String> to) {
        return sendHtmlMail(subject, content, to.toArray(new String[0]));
    }

    /**
     * 检查邮件是否符合标准
     *
     * @param mailDetails 邮件信息
     */
    default void checkMail(@Nonnull MailDetails mailDetails) {
        boolean noTo = mailDetails.getTo() == null || mailDetails.getTo().length == 0;
        boolean noCc = mailDetails.getCc() == null || mailDetails.getCc().length == 0;
        boolean noBcc = mailDetails.getBcc() == null || mailDetails.getBcc().length == 0;
        if (noTo && noCc && noBcc) {
            throw new MailSendException("邮件应该至少有一个收件人");
        }
        if (!StringUtils.hasText(mailDetails.getSubject())) {
            throw new MailSendException("邮件主题不能为空");
        }
        if (!StringUtils.hasText(mailDetails.getContent())) {
            throw new MailSendException("邮件内容不能为空");
        }
    }

}
