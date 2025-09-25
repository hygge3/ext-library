package ext.library.mail.event;

import ext.library.mail.model.MailSendInfo;
import org.springframework.context.ApplicationEvent;

/**
 * 邮件发送事件
 */
public class MailSendEvent extends ApplicationEvent {

    public MailSendEvent(MailSendInfo mailSendInfo) {
        super(mailSendInfo);
    }

    @Override
    public String toString() {
        return "MailSendEvent{" +
                "source=" + source +
                '}';
    }
}