package ext.library.captcha.config;

import ext.library.captcha.cache.CaptchaCache;
import ext.library.captcha.config.properties.CaptchaProperties;
import ext.library.captcha.core.Captcha;
import ext.library.captcha.service.CaptchaServiceImpl;
import ext.library.captcha.service.ICaptchaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * 验证码自动配置
 */
@AutoConfiguration
@EnableConfigurationProperties(CaptchaProperties.class)
public class CaptchaAutoConfig {
    private final Logger log = LoggerFactory.getLogger(getClass());

    @Bean
    @ConditionalOnMissingBean
    public Captcha imageCaptcha(CaptchaProperties captchaProperties) {
        return new Captcha(captchaProperties.getCaptchaType());
    }

    @Bean
    @ConditionalOnMissingBean
    public ICaptchaService imageCaptchaService(CaptchaProperties captchaProperties, CaptchaCache captchaCache, Captcha captcha) {
        log.info("[🔢] 验证码模块载入成功");
        return new CaptchaServiceImpl(captchaProperties, captchaCache, captcha);
    }

    @Bean
    @ConditionalOnMissingBean
    public CaptchaCache captchaCache(CaptchaProperties captchaProperties) {
        return new CaptchaCache(captchaProperties.getCacheStorage().getCacheStrategy());
    }

}