package ext.library.captcha;

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
    RANDOM {
        @Override
        public CaptchaDraw newDraw(int codeSize) {
            return new RandomCaptchaDraw(codeSize);
        }
    },

    /**
     * 算术
     */
    MATH {
        @Override
        public CaptchaDraw newDraw(int codeSize) {
            // 算术式固定为 a op b，字符长度不适用
            return new MathCaptchaDraw();
        }
    };

    /**
     * 按字符长度创建绘制器
     *
     * @param codeSize 验证码字符长度，算术类型忽略
     *
     * @return 绘制器
     */
    public abstract CaptchaDraw newDraw(int codeSize);
}
