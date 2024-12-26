package ext.library.mail.event;

import ext.library.mail.model.MailSendInfo;
import lombok.ToString;
import org.springframework.context.ApplicationEvent;

/**
 * 邮件发送事件
 */
@ToString
public class MailSendEvent extends ApplicationEvent {

	public MailSendEvent(MailSendInfo mailSendInfo) {
		super(mailSendInfo);
	}

}
