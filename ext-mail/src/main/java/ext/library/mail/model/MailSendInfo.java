package ext.library.mail.model;

import java.time.LocalDateTime;

/**
 * 邮件发送详情
 */
public class MailSendInfo {

    /**
     * 邮件信息
     */
    private final MailDetails mailDetails;
    /**
     * 发送时间
     */
    private LocalDateTime sentDate;
    /**
     * 是否发送成功
     */
    private Boolean success;
    /**
     * 错误信息 errorMsg
     */
    private String errorMsg;

    public MailSendInfo(MailDetails mailDetails) {
        this.mailDetails = mailDetails;
    }

    public void setSentDate(LocalDateTime sentDate) {
        this.sentDate = sentDate;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }
}