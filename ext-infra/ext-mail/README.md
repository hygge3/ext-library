# ext-mail（邮件发送）

## 功能

- 简单邮件发送
- 带附件邮件发送
- HTML 邮件支持
- 邮件发送事件
- 灵活的邮件模型
- 异步发送支持

## 依赖引用

### Maven

```xml
<dependency>
    <groupId>ext.library</groupId>
    <artifactId>ext-mail</artifactId>
    <version>${version}</version>
</dependency>
```

### Gradle

```groovy
compile("ext.library:ext-mail:${version}")
```

## 配置项

| 配置项 | 默认值 | 说明 |
|-------|-------|------|
| spring.mail.host | - | SMTP 服务器地址 |
| spring.mail.port | 25 | SMTP 端口 |
| spring.mail.username | - | 邮箱用户名 |
| spring.mail.password | - | 邮箱密码 |
| spring.mail.properties.mail.smtp.auth | true | 启用认证 |
| spring.mail.properties.mail.smtp.starttls.enable | true | 启用 TLS |

## 核心类说明

| 类名 | 说明 |
|-----|------|
| MailSender | 邮件发送接口 |
| MailSenderImpl | 邮件发送实现 |
| MailDetails | 邮件详情模型 |
| MailSendInfo | 邮件发送信息 |
| MailSendEvent | 邮件发送事件 |

## 使用示例

### 简单邮件

```java
@Autowired
private MailSender mailSender;

public void sendSimpleMail(String to, String subject, String content) {
    MailDetails details = MailDetails.builder()
        .to(to)
        .subject(subject)
        .text(content)
        .build();
    mailSender.send(details);
}
```

### HTML 邮件

```java
public void sendHtmlMail(String to, String subject, String htmlContent) {
    MailDetails details = MailDetails.builder()
        .to(to)
        .subject(subject)
        .html(htmlContent)
        .build();
    mailSender.send(details);
}
```

### 带附件邮件

```java
public void sendAttachmentMail(String to, String subject, String content, File attachment) {
    MailDetails details = MailDetails.builder()
        .to(to)
        .subject(subject)
        .text(content)
        .addAttachment(attachment.getName(), attachment)
        .build();
    mailSender.send(details);
}
```

### 批量发送

```java
public void sendBatchMail(List<String> toList, String subject, String content) {
    MailDetails details = MailDetails.builder()
        .to(toList)
        .subject(subject)
        .text(content)
        .build();
    mailSender.send(details);
}
```

### 事件监听

```java
@Component
public class MailEventListener {

    @EventListener
    public void onMailSend(MailSendEvent event) {
        log.info("邮件发送成功: {}, to: {}",
            event.getMailDetails().getSubject(),
            event.getMailDetails().getTo());
    }
}
```
