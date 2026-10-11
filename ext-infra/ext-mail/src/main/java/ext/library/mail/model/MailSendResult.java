package ext.library.mail.model;

import java.time.LocalDateTime;

/**
 * 邮件发送结果
 *
 * @param mailDetails 邮件详情
 * @param sentDate    发送时间
 * @param success     是否发送成功
 * @param errorMsg    错误信息
 */
public record MailSendResult(
        MailDetails mailDetails,
        LocalDateTime sentDate,
        boolean success,
        String errorMsg
) {

    /**
     * 创建成功结果
     *
     * @param mailDetails 邮件详情
     * @return 成功结果
     */
    public static MailSendResult success(MailDetails mailDetails) {
        return new MailSendResult(mailDetails, LocalDateTime.now(), true, null);
    }

    /**
     * 创建失败结果
     *
     * @param mailDetails 邮件详情
     * @param errorMsg    错误信息
     * @return 失败结果
     */
    public static MailSendResult failure(MailDetails mailDetails, String errorMsg) {
        return new MailSendResult(mailDetails, LocalDateTime.now(), false, errorMsg);
    }
}
