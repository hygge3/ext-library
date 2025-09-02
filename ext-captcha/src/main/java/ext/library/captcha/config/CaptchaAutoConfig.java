package ext.library.captcha.config;

import ext.library.captcha.cache.CaptchaCache;
import ext.library.captcha.config.properties.CaptchaProperties;
import ext.library.captcha.core.Captcha;
import ext.library.captcha.service.CaptchaServiceImpl;
import ext.library.captcha.service.ICaptchaService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import jakarta.annotation.Nonnull;

/**
 * 验证码自动配置
 */
@Slf4j
@AutoConfiguration
@EnableConfigurationProperties(CaptchaProperties.class)
public class CaptchaAutoConfig {

    @Bean
    @ConditionalOnMissingBean
    public Captcha imageCaptcha(@Nonnull CaptchaProperties captchaProperties) {
        return new Captcha(captchaProperties.getCaptchaType());
    }

    @Bean
    @ConditionalOnMissingBean
    public ICaptchaService imageCaptchaService(@Nonnull CaptchaProperties captchaProperties, CaptchaCache captchaCache, Captcha captcha) {
        log.info("[🔢] 验证码模块载入成功");
        return new CaptchaServiceImpl(captchaProperties, captchaCache, captcha);
    }

    @Bean
    @ConditionalOnMissingBean
    public CaptchaCache captchaCache(@Nonnull CaptchaProperties captchaProperties) {
        return new CaptchaCache(captchaProperties.getCacheStorage().getCacheStrategy());
    }

}