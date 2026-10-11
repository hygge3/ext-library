package ext.library.mail.event;

import ext.library.mail.model.MailSendResult;
import org.springframework.context.ApplicationEvent;

/**
 * 邮件发送事件
 */
public class MailSendEvent extends ApplicationEvent {

    public MailSendEvent(MailSendResult result) {
        super(result);
    }

    /**
     * 获取邮件发送结果
     *
     * @return 发送结果
     */
    public MailSendResult getResult() {
        return (MailSendResult) getSource();
    }

    @Override
    public String toString() {
        return "MailSendEvent{result=" + source + '}';
    }
}
