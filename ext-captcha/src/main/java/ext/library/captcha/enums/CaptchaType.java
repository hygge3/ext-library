package ext.library.captcha.enums;

import ext.library.captcha.draw.CaptchaDraw;
import ext.library.captcha.draw.MathCaptchaDraw;
import ext.library.captcha.draw.RandomCaptchaDraw;

/**
 * 验证码类型
 */
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

    CaptchaType(CaptchaDraw captchaDraw) {
        this.captchaDraw = captchaDraw;
    }

    public CaptchaDraw getCaptchaDraw() {
        return captchaDraw;
    }
}