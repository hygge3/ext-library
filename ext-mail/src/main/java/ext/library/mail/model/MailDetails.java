package ext.library.mail.model;

import java.io.File;
import lombok.Data;

/**
 * 邮件详细信息
 */
@Data
public class MailDetails {

	/**
	 * 发件人
	 */
	 String from;

	/**
	 * 收件人
	 */
	 String[] to;

	/**
	 * 邮件主题
	 */
	 String subject;

	/**
	 * 是否渲染 html
	 */
	 Boolean showHtml;

	/**
	 * 邮件内容
	 */
	 String content;

	/**
	 * 抄送
	 */
	 String[] cc;

	/**
	 * 密送
	 */
	 String[] bcc;

	/**
	 * 附件
	 */
	 File[] files;

}
