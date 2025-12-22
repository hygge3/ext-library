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

    /**
     * 获取邮件信息
     *
     * @return 邮件信息
     */
    public MailDetails getMailDetails() {
        return mailDetails;
    }

    /**
     * 获取发送时间
     *
     * @return 发送时间
     */
    public LocalDateTime getSentDate() {
        return sentDate;
    }

    /**
     * 设置发送时间
     *
     * @param sentDate 发送时间
     */
    public void setSentDate(LocalDateTime sentDate) {
        this.sentDate = sentDate;
    }

    /**
     * 获取是否发送成功
     *
     * @return 是否发送成功
     */
    public Boolean getSuccess() {
        return success;
    }

    /**
     * 设置是否发送成功
     *
     * @param success 是否发送成功
     */
    public void setSuccess(Boolean success) {
        this.success = success;
    }

    /**
     * 获取错误信息
     *
     * @return 错误信息
     */
    public String getErrorMsg() {
        return errorMsg;
    }

    /**
     * 设置错误信息
     *
     * @param errorMsg 错误信息
     */
    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }
}
