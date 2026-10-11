package ext.library.mail.model;

import org.springframework.core.io.InputStreamSource;

import java.util.ArrayList;
import java.util.List;

/**
 * 邮件详情
 */
public class MailDetails {

    /**
     * 发件人
     */
    private String from;

    /**
     * 收件人
     */
    private List<String> to = new ArrayList<>();

    /**
     * 抄送
     */
    private List<String> cc = new ArrayList<>();

    /**
     * 密送
     */
    private List<String> bcc = new ArrayList<>();

    /**
     * 邮件主题
     */
    private String subject;

    /**
     * 邮件内容
     */
    private String content;

    /**
     * 是否为 HTML 内容
     */
    private boolean html;

    /**
     * 附件列表
     */
    private List<Attachment> attachments = new ArrayList<>();

    // ==================== Getters/Setters ====================

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public List<String> getTo() {
        return to;
    }

    public void setTo(List<String> to) {
        this.to = to;
    }

    public List<String> getCc() {
        return cc;
    }

    public void setCc(List<String> cc) {
        this.cc = cc;
    }

    public List<String> getBcc() {
        return bcc;
    }

    public void setBcc(List<String> bcc) {
        this.bcc = bcc;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public boolean isHtml() {
        return html;
    }

    public void setHtml(boolean html) {
        this.html = html;
    }

    public List<Attachment> getAttachments() {
        return attachments;
    }

    public void setAttachments(List<Attachment> attachments) {
        this.attachments = attachments;
    }

    // ==================== Builder ====================

    /**
     * 创建 Builder
     *
     * @return Builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * 邮件详情构建器
     */
    public static class Builder {
        private final MailDetails details = new MailDetails();

        /**
         * 设置发件人
         */
        public Builder from(String from) {
            details.from = from;
            return this;
        }

        /**
         * 添加收件人
         */
        public Builder to(String recipient) {
            details.to.add(recipient);
            return this;
        }

        /**
         * 设置收件人列表
         */
        public Builder to(List<String> recipients) {
            details.to.addAll(recipients);
            return this;
        }

        /**
         * 添加抄送人
         */
        public Builder cc(String recipient) {
            details.cc.add(recipient);
            return this;
        }

        /**
         * 设置抄送人列表
         */
        public Builder cc(List<String> recipients) {
            details.cc.addAll(recipients);
            return this;
        }

        /**
         * 添加密送人
         */
        public Builder bcc(String recipient) {
            details.bcc.add(recipient);
            return this;
        }

        /**
         * 设置密送人列表
         */
        public Builder bcc(List<String> recipients) {
            details.bcc.addAll(recipients);
            return this;
        }

        /**
         * 设置主题
         */
        public Builder subject(String subject) {
            details.subject = subject;
            return this;
        }

        /**
         * 设置纯文本内容
         */
        public Builder text(String text) {
            details.content = text;
            details.html = false;
            return this;
        }

        /**
         * 设置 HTML 内容
         */
        public Builder html(String htmlContent) {
            details.content = htmlContent;
            details.html = true;
            return this;
        }

        /**
         * 设置内容
         */
        public Builder content(String content) {
            details.content = content;
            return this;
        }

        /**
         * 添加附件
         */
        public Builder addAttachment(String name, InputStreamSource source) {
            details.attachments.add(new Attachment(name, source));
            return this;
        }

        /**
         * 构建邮件详情
         */
        public MailDetails build() {
            return details;
        }
    }

    // ==================== Attachment ====================

    /**
     * 附件信息
     *
     * @param name   附件名称
     * @param source 附件内容源
     */
    public record Attachment(String name, InputStreamSource source) {
    }

    @Override
    public String toString() {
        return "MailDetails{" +
                "from='" + from + '\'' +
                ", to=" + to +
                ", cc=" + cc +
                ", bcc=" + bcc +
                ", subject='" + subject + '\'' +
                ", html=" + html +
                ", attachments=" + attachments.size() +
                '}';
    }
}
