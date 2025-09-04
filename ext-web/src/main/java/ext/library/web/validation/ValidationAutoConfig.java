package ext.library.web.validation;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.validator.HibernateValidator;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnResource;
import org.springframework.boot.autoconfigure.validation.ValidationAutoConfiguration;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Role;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import jakarta.validation.MessageInterpolator;
import jakarta.validation.Validator;
import jakarta.validation.executable.ExecutableValidator;
import java.util.Properties;

/**
 * Validation 自动配置类，扩展支持使用 {} 占位替换默认消息
 */
@Slf4j
@AutoConfiguration(before = ValidationAutoConfiguration.class)
@ConditionalOnClass(ExecutableValidator.class)
@ConditionalOnResource(resources = "classpath:META-INF/services/javax.validation.spi.ValidationProvider")
public class ValidationAutoConfig {

    @Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    @ConditionalOnMissingBean(Validator.class)
    @ConditionalOnBean(MessageInterpolator.class)
    public static LocalValidatorFactoryBean defaultValidator(MessageInterpolator messageInterpolator) {
        LocalValidatorFactoryBean factoryBean = new LocalValidatorFactoryBean();
        factoryBean.setMessageInterpolator(messageInterpolator);
        return factoryBean;
    }

    @Bean
    @ConditionalOnMissingBean({Validator.class, MessageInterpolator.class})
    public EmptyCurlyToDefaultMessageInterpolator messageInterpolator() {
        return new EmptyCurlyToDefaultMessageInterpolator();
    }

    /**
     * 配置校验框架 快速返回模式
     */
    @Bean
    public Validator validator(MessageSource messageSource) {
        try (LocalValidatorFactoryBean factoryBean = new LocalValidatorFactoryBean()) {
            // 国际化
            factoryBean.setValidationMessageSource(messageSource);
            // 设置使用 HibernateValidator 校验器
            factoryBean.setProviderClass(HibernateValidator.class);
            Properties properties = new Properties();
            // 设置 快速异常返回
            properties.setProperty("hibernate.validator.fail_fast", "true");
            factoryBean.setValidationProperties(properties);
            // 加载配置
            factoryBean.afterPropertiesSet();
            log.info("[🛂] 校验模块载入成功");
            return factoryBean.getValidator();
        }
    }
}