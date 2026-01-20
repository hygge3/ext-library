package ext.library.captcha.service;

import ext.library.captcha.cache.CaptchaCache;
import ext.library.captcha.core.Captcha;
import ext.library.captcha.properties.CaptchaProperties;
import ext.library.tool.constant.EmojiSymbol;
import ext.library.tool.runtime.Logs;
import ext.library.tool.util.ObjectUtil;

import java.io.OutputStream;

/**
 * 验证码服务
 */
public class CaptchaServiceImpl implements ICaptchaService {

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
        Logs.debug(EmojiSymbol.CAPTCHA, "验证码 UUID 是 {}，生成验证码是 {}", uuid, generate);
    }

    @Override
    public boolean validate(String uuid, String userInputCaptcha) {
        Logs.debug(EmojiSymbol.CAPTCHA, "验证验证码 uuid 是 {}，输入验证码是 {}", uuid, userInputCaptcha);
        String code = captchaCache.getAndRemove(properties.getCacheName(), uuid);
        if (ObjectUtil.isEmpty(code)) {
            return false;
        }
        return captcha.validate(code, userInputCaptcha);
    }

}
