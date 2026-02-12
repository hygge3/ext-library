package ext.library.mail.config;

import ext.library.mail.sender.MailSender;
import ext.library.mail.sender.MailSenderImpl;
import ext.library.tool.constant.EmojiSymbol;
import ext.library.tool.runtime.Logs;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.mail.autoconfigure.MailSenderAutoConfiguration;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.mail.javamail.JavaMailSender;

/**
 * 邮件自动配置
 */

@AutoConfiguration(after = MailSenderAutoConfiguration.class)
public class MailAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(MailSender.class)
    @ConditionalOnProperty(prefix = "spring.mail", name = "host")
    public MailSender mailSender(JavaMailSender javaMailSender, ApplicationEventPublisher eventPublisher) {
        Logs.info(EmojiSymbol.MAIL, "载入模块：邮件");
        return new MailSenderImpl(javaMailSender, eventPublisher);
    }

}
