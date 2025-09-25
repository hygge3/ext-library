package ext.library.mail.config;

import ext.library.mail.sender.MailSender;
import ext.library.mail.sender.MailSenderImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.mail.MailSenderAutoConfiguration;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.mail.javamail.JavaMailSender;

/**
 * 邮件自动配置
 */

@AutoConfiguration(after = MailSenderAutoConfiguration.class)
public class MailAutoConfig {
    private final Logger log = LoggerFactory.getLogger(getClass());

    @Bean
    @ConditionalOnMissingBean(MailSender.class)
    @ConditionalOnProperty(prefix = "spring.mail", name = "host")
    public MailSender mailSenderImpl(JavaMailSender javaMailSender, ApplicationEventPublisher applicationEventPublisher) {
        log.info("[📧] 邮件模块载入成功");
        return new MailSenderImpl(javaMailSender, applicationEventPublisher);
    }

}