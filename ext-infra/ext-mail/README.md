# ext-mail

> 邮件发送模块 - 提供邮件发送服务封装

## 简介

`ext-mail` 是 ext-library 的邮件发送模块，封装了 Spring Boot Mail，提供简洁的邮件发送 API 和事件支持。

## 快速开始

### Maven

```xml
<dependency>
    <groupId>ext.library</groupId>
    <artifactId>ext-mail</artifactId>
</dependency>
```

### Gradle

```groovy
implementation("ext.library:ext-mail")
```

## 依赖说明

| 依赖 | 说明 |
|------|------|
| spring-boot-starter-mail | Spring Mail 支持 |

## 功能特性

- 简单邮件发送
- HTML 邮件支持
- 带附件邮件发送
- 批量发送支持
- 邮件发送事件

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
|------|------|
| `MailSender` | 邮件发送接口 |
| `MailSenderImpl` | 邮件发送实现 |
| `MailDetails` | 邮件详情模型 |
| `MailSendResult` | 邮件发送结果 |
| `MailSendEvent` | 邮件发送事件 |

## 使用示例

### 配置邮件服务

```yaml
spring:
  mail:
    host: smtp.example.com
    port: 587
    username: your-email@example.com
    password: your-password
    properties:
      mail:
        smtp:
          auth: true
          starttls:
            enable: true
```

### 简单邮件

```java
@Autowired
private MailSender mailSender;

public void sendSimpleMail() {
    MailDetails details = MailDetails.builder()
        .to("recipient@example.com")
        .subject("测试邮件")
        .text("这是一封测试邮件")
        .build();
    mailSender.send(details);
}
```

### 快捷方法

```java
// 发送纯文本邮件
mailSender.sendText("主题", "内容", "recipient@example.com");

// 发送 HTML 邮件
mailSender.sendHtml("主题", "<h1>HTML内容</h1>", "recipient@example.com");
```

### HTML 邮件

```java
public void sendHtmlMail() {
    String htmlContent = """
        <html>
        <body>
            <h1>欢迎注册</h1>
            <p>您的验证码是：<strong>123456</strong></p>
        </body>
        </html>
        """;

    MailDetails details = MailDetails.builder()
        .to("recipient@example.com")
        .subject("验证码")
        .html(htmlContent)
        .build();
    mailSender.send(details);
}
```

### 带附件邮件

```java
public void sendAttachmentMail(File attachment) {
    MailDetails details = MailDetails.builder()
        .to("recipient@example.com")
        .subject("附件邮件")
        .text("请查收附件")
        .addAttachment(attachment.getName(), new FileSystemResource(attachment))
        .build();
    mailSender.send(details);
}
```

### 批量发送

```java
public void sendBatchMail(List<String> recipients) {
    MailDetails details = MailDetails.builder()
        .to(recipients)
        .subject("群发邮件")
        .text("这是一封群发邮件")
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
        MailSendResult result = event.getResult();
        if (result.success()) {
            log.info("邮件发送成功: {}", result.mailDetails().getSubject());
        } else {
            log.error("邮件发送失败: {}", result.errorMsg());
        }
    }
}
```

## 许可证

[Apache License 2.0](../../LICENSE)
