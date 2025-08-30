package ext.library.mail.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 邮件发送详情
 */
@Getter
@Setter
@RequiredArgsConstructor
public class MailSendInfo {

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