package ext.library.mail.model;

import java.io.File;

/**
 * 邮件详细信息
 */
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

    /**
     * 获取发件人
     *
     * @return 发件人
     */
    public String getFrom() {
        return from;
    }

    /**
     * 设置发件人
     *
     * @param from 发件人
     */
    public void setFrom(String from) {
        this.from = from;
    }

    /**
     * 获取收件人
     *
     * @return 收件人
     */
    public String[] getTo() {
        return to;
    }

    /**
     * 设置收件人
     *
     * @param to 收件人
     */
    public void setTo(String[] to) {
        this.to = to;
    }

    /**
     * 获取邮件主题
     *
     * @return 邮件主题
     */
    public String getSubject() {
        return subject;
    }

    /**
     * 设置邮件主题
     *
     * @param subject 邮件主题
     */
    public void setSubject(String subject) {
        this.subject = subject;
    }

    /**
     * 获取是否渲染 html
     *
     * @return 是否渲染 html
     */
    public Boolean getShowHtml() {
        return showHtml;
    }

    /**
     * 设置是否渲染 html
     *
     * @param showHtml 是否渲染 html
     */
    public void setShowHtml(Boolean showHtml) {
        this.showHtml = showHtml;
    }

    /**
     * 获取邮件内容
     *
     * @return 邮件内容
     */
    public String getContent() {
        return content;
    }

    /**
     * 设置邮件内容
     *
     * @param content 邮件内容
     */
    public void setContent(String content) {
        this.content = content;
    }

    /**
     * 获取抄送
     *
     * @return 抄送
     */
    public String[] getCc() {
        return cc;
    }

    /**
     * 设置抄送
     *
     * @param cc 抄送
     */
    public void setCc(String[] cc) {
        this.cc = cc;
    }

    /**
     * 获取密送
     *
     * @return 密送
     */
    public String[] getBcc() {
        return bcc;
    }

    /**
     * 设置密送
     *
     * @param bcc 密送
     */
    public void setBcc(String[] bcc) {
        this.bcc = bcc;
    }

    /**
     * 获取附件
     *
     * @return 附件
     */
    public File[] getFiles() {
        return files;
    }

    /**
     * 设置附件
     *
     * @param files 附件
     */
    public void setFiles(File[] files) {
        this.files = files;
    }
}
