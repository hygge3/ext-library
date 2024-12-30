package ext.library.captcha.service;

import java.io.OutputStream;

import ext.library.captcha.cache.ICaptchaCache;
import ext.library.captcha.config.properties.CaptchaProperties;
import ext.library.captcha.core.Captcha;
import ext.library.tool.$;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 验证码服务
 */
@Slf4j
@RequiredArgsConstructor
public class CaptchaServiceImpl implements ICaptchaService {

	private final CaptchaProperties properties;

	private final ICaptchaCache captchaCache;

	private final Captcha captcha;

	@Override
	public void generate(String uuid, OutputStream outputStream) {
		String generate = captcha.generate(() -> outputStream);
		captchaCache.put(properties.getCacheName(), uuid, generate);
	}

	@Override
	public boolean validate(String uuid, String userInputCaptcha) {
		log.debug("[🔢] validate captcha uuid is {}, userInputCaptcha is {}", uuid, userInputCaptcha);
		String code = captchaCache.getAndRemove(properties.getCacheName(), uuid);
		if ($.isEmpty(code)) {
			return false;
		}
		return captcha.validate(code, userInputCaptcha);
	}

}
