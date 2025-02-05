package ext.library.mail.model;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

/**
 * 邮件发送详情
 */
@Getter
public class MailSendInfo {

    public MailSendInfo(MailDetails mailDetails) {
        this.mailDetails = mailDetails;
    }

    /**
     * 邮件信息
     */
    private final MailDetails mailDetails;

    /**
     * 发送时间
     */
    @Setter
    private LocalDateTime sentDate;

    /**
     * 是否发送成功
     */
    @Setter
    private Boolean success;

    /**
     * 错误信息 errorMsg
     */
    @Setter
    private String errorMsg;

}
