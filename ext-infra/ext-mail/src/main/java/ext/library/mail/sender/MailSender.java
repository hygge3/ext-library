package ext.library.mail.sender;

import ext.library.mail.model.MailDetails;
import ext.library.mail.model.MailSendResult;

import java.util.List;

/**
 * 邮件发送器
 */
public interface MailSender {

    /**
     * 发送邮件
     *
     * @param mailDetails 邮件详情
     * @return 发送结果
     */
    MailSendResult send(MailDetails mailDetails);

    /**
     * 发送纯文本邮件
     *
     * @param subject 主题
     * @param content 邮件正文
     * @param to      收件人
     * @return 发送结果
     */
    default MailSendResult sendText(String subject, String content, String... to) {
        MailDetails mailDetails = MailDetails.builder()
                .subject(subject)
                .text(content)
                .to(List.of(to))
                .build();
        return send(mailDetails);
    }

    /**
     * 发送纯文本邮件
     *
     * @param subject 主题
     * @param content 邮件正文
     * @param to      收件人列表
     * @return 发送结果
     */
    default MailSendResult sendText(String subject, String content, List<String> to) {
        MailDetails mailDetails = MailDetails.builder()
                .subject(subject)
                .text(content)
                .to(to)
                .build();
        return send(mailDetails);
    }

    /**
     * 发送 HTML 邮件
     *
     * @param subject     主题
     * @param htmlContent HTML 正文
     * @param to          收件人
     * @return 发送结果
     */
    default MailSendResult sendHtml(String subject, String htmlContent, String... to) {
        MailDetails mailDetails = MailDetails.builder()
                .subject(subject)
                .html(htmlContent)
                .to(List.of(to))
                .build();
        return send(mailDetails);
    }

    /**
     * 发送 HTML 邮件
     *
     * @param subject     主题
     * @param htmlContent HTML 正文
     * @param to          收件人列表
     * @return 发送结果
     */
    default MailSendResult sendHtml(String subject, String htmlContent, List<String> to) {
        MailDetails mailDetails = MailDetails.builder()
                .subject(subject)
                .html(htmlContent)
                .to(to)
                .build();
        return send(mailDetails);
    }
}
