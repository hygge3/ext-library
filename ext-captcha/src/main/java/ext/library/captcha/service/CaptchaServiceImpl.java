package ext.library.captcha.service;

import ext.library.captcha.cache.CaptchaCache;
import ext.library.captcha.config.properties.CaptchaProperties;
import ext.library.captcha.core.Captcha;
import ext.library.tool.$;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.OutputStream;

/**
 * 验证码服务
 */
@Slf4j
@RequiredArgsConstructor
public class CaptchaServiceImpl implements ICaptchaService {

    final CaptchaProperties properties;

    final CaptchaCache captchaCache;

    final Captcha captcha;

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
        if ($.isEmpty(code)) {
            return false;
        }
        return captcha.validate(code, userInputCaptcha);
    }

}