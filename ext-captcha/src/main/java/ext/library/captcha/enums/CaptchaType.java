package ext.library.captcha.enums;

import ext.library.captcha.draw.CaptchaDraw;
import ext.library.captcha.draw.MathCaptchaDraw;
import ext.library.captcha.draw.RandomCaptchaDraw;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 验证码类型
 */
@Getter
@RequiredArgsConstructor
public enum CaptchaType {

	/**
	 * 随机数
	 */
	RANDOM(new RandomCaptchaDraw()),
	/**
	 * 算术
	 */
	MATH(new MathCaptchaDraw());

	private final CaptchaDraw captchaDraw;

}
