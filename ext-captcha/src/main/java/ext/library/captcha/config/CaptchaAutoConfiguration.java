package ext.library.captcha.config;

import ext.library.captcha.cache.ICaptchaCache;
import ext.library.captcha.cache.SpringCacheCaptchaCache;
import ext.library.captcha.config.properties.CaptchaProperties;
import ext.library.captcha.core.Captcha;
import ext.library.captcha.service.CaptchaServiceImpl;
import ext.library.captcha.service.ICaptchaService;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import jakarta.annotation.Nonnull;

/**
 * 验证码自动配置
 */
@AutoConfiguration
@ConditionalOnProperty(prefix = CaptchaProperties.PREFIX, name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(CaptchaProperties.class)
public class CaptchaAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public Captcha imageCaptcha(@Nonnull CaptchaProperties properties) {
        return new Captcha(properties.getCaptchaType());
    }

    @Bean
    @ConditionalOnMissingBean
    public ICaptchaService imageCaptchaService(CaptchaProperties properties, ICaptchaCache captchaCache,
                                               Captcha captcha) {
        return new CaptchaServiceImpl(properties, captchaCache, captcha);
    }

    @Bean
    @ConditionalOnMissingBean
    public ICaptchaCache captchaCache(CaptchaProperties properties) {
        return new SpringCacheCaptchaCache(properties);
    }

}