[根目录](../CLAUDE.md) > **ext-mail**

# ext-mail 模块文档

## 模块职责

ext-mail 提供邮件发送服务，支持事件驱动和异步发送。

## 入口与启动

### 自动配置类
- **MailAutoConfig**: 邮件自动配置

## 核心组件

### 1. 配置类 (config/)
- **MailAutoConfig**: 邮件自动配置

### 2. 发送器 (sender/)
- **MailSender**: 邮件发送器接口
- **MailSenderImpl**: 邮件发送器实现

### 3. 模型 (model/)
- **MailDetails**: 邮件详情
- **MailSendInfo**: 邮件发送信息

### 4. 事件 (event/)
- **MailSendEvent**: 邮件发送事件

## 关键依赖

- **spring-boot-starter-mail**: 邮件发送支持

## 使用示例

### 发送邮件
```java
@Autowired
private MailSender mailSender;

MailDetails details = MailDetails.builder()
    .to("recipient@example.com")
    .subject("测试邮件")
    .content("这是一封测试邮件")
    .build();

mailSender.sendMail(details);
```

### 发送 HTML 邮件
```java
MailDetails details = MailDetails.builder()
    .to("recipient@example.com")
    .subject("HTML 邮件")
    .htmlContent("<h1>标题</h1><p>内容</p>")
    .build();

mailSender.sendMail(details);
```

### 事件驱动发送
```java
@Autowired
private ApplicationEventPublisher eventPublisher;

MailSendEvent event = new MailSendEvent(this, mailDetails);
eventPublisher.publishEvent(event);
```

## 常见问题 (FAQ)

### Q: 如何配置邮件服务器？
```yaml
spring:
  mail:
    host: smtp.example.com
    port: 587
    username: your-email@example.com
    password: your-password
```

## 相关文件清单

### 主要源码文件
- `src/main/java/ext/library/mail/config/MailAutoConfig.java`
- `src/main/java/ext/library/mail/sender/MailSender.java`
- `src/main/java/ext/library/mail/model/MailDetails.java`

### 配置文件
- `src/main/resources/META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`

## 变更记录 (Changelog)

### 2025-12-24
- 创建：模块文档
