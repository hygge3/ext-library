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
	private String from;

	/**
	 * 收件人
	 */
	private String[] to;

	/**
	 * 邮件主题
	 */
	private String subject;

	/**
	 * 是否渲染 html
	 */
	private Boolean showHtml;

	/**
	 * 邮件内容
	 */
	private String content;

	/**
	 * 抄送
	 */
	private String[] cc;

	/**
	 * 密送
	 */
	private String[] bcc;

	/**
	 * 附件
	 */
	private File[] files;

}
