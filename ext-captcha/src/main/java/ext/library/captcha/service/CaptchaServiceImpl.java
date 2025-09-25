package ext.library.captcha.service;

import ext.library.captcha.cache.CaptchaCache;
import ext.library.captcha.config.properties.CaptchaProperties;
import ext.library.captcha.core.Captcha;
import ext.library.tool.util.ObjectUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.OutputStream;

/**
 * 验证码服务
 */
public class CaptchaServiceImpl implements ICaptchaService {
    private final Logger log = LoggerFactory.getLogger(getClass());

    private final CaptchaProperties properties;

    private final CaptchaCache captchaCache;

    private final Captcha captcha;

    public CaptchaServiceImpl(CaptchaProperties properties, CaptchaCache captchaCache, Captcha captcha) {
        this.properties = properties;
        this.captchaCache = captchaCache;
        this.captcha = captcha;
    }

    @Override
    public void generate(String uuid, OutputStream outputStream) {
        String generate = captcha.generate(() -> outputStream);
        captchaCache.put(properties.getCacheName(), uuid, generate);
        log.debug("[🔢] captcha uuid is {}, generate captcha is {}", uuid, generate);
    }

    @Override
    public boolean validate(String uuid, String userInputCaptcha) {
        log.debug("[🔢] validate captcha uuid is {}, input captcha is {}", uuid, userInputCaptcha);
        String code = captchaCache.getAndRemove(properties.getCacheName(), uuid);
        if (ObjectUtil.isEmpty(code)) {
            return false;
        }
        return captcha.validate(code, userInputCaptcha);
    }

}