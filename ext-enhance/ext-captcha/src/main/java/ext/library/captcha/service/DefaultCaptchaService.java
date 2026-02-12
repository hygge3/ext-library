package ext.library.captcha.service;

import ext.library.captcha.CaptchaProperties;
import ext.library.captcha.cache.CaptchaCache;
import ext.library.captcha.core.Captcha;
import ext.library.tool.constant.EmojiSymbol;
import ext.library.tool.runtime.Logs;
import ext.library.tool.util.StringUtil;

import java.io.OutputStream;

/**
 * 验证码服务默认实现
 */
public class DefaultCaptchaService implements CaptchaWebService {

    private final CaptchaProperties properties;

    private final CaptchaCache captchaCache;

    private final Captcha captcha;

    public DefaultCaptchaService(CaptchaProperties properties, CaptchaCache captchaCache, Captcha captcha) {
        this.properties = properties;
        this.captchaCache = captchaCache;
        this.captcha = captcha;
    }

    @Override
    public void generate(String uuid, OutputStream outputStream) {
        String generate = captcha.generate(() -> outputStream);
        captchaCache.put(properties.getCacheName(), uuid, generate);
        Logs.debug(EmojiSymbol.CAPTCHA, "验证码 UUID 是 {}，生成验证码是 {}", uuid, generate);
    }

    @Override
    public boolean validate(String uuid, String userInputCaptcha) {
        Logs.debug(EmojiSymbol.CAPTCHA, "验证验证码 uuid 是 {}，输入验证码是 {}", uuid, userInputCaptcha);
        String code = captchaCache.getAndRemove(properties.getCacheName(), uuid);
        if (StringUtil.isEmpty(code)) {
            return false;
        }
        return captcha.validate(code, userInputCaptcha);
    }
}
