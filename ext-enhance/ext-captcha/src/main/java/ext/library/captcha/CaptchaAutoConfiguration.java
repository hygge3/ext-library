package ext.library.captcha;

import ext.library.captcha.cache.CaptchaCache;
import ext.library.captcha.core.Captcha;
import ext.library.captcha.core.DefaultCaptcha;
import ext.library.captcha.service.CaptchaWebService;
import ext.library.captcha.service.DefaultCaptchaService;
import ext.library.tool.constant.EmojiSymbol;
import ext.library.tool.runtime.Logs;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * 验证码自动配置
 */
@AutoConfiguration
@EnableConfigurationProperties(CaptchaProperties.class)
public class CaptchaAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public Captcha captcha(CaptchaProperties captchaProperties) {
        return new DefaultCaptcha(captchaProperties);
    }

    @Bean
    @ConditionalOnMissingBean
    public CaptchaWebService captchaService(CaptchaProperties captchaProperties, CaptchaCache captchaCache, Captcha captcha) {
        Logs.info(EmojiSymbol.CAPTCHA, "载入模块：验证码");
        return new DefaultCaptchaService(captchaProperties, captchaCache, captcha);
    }

    @Bean
    @ConditionalOnMissingBean
    public CaptchaCache captchaCache(CaptchaProperties captchaProperties) {
        return new CaptchaCache(captchaProperties.getCacheStorage().getCacheStrategy(), captchaProperties.getExpireTime());
    }
}
