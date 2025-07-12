package ext.library.captcha.core;

import lombok.experimental.UtilityClass;

import jakarta.annotation.Nonnull;
import java.awt.*;
import java.util.Random;

/**
 * 验证码工具类
 */
@UtilityClass
public class CaptchaUtil {

    /**
     * 生成指定范围的随机数
     */
    public int randNum(@Nonnull Random random, int min, int max) {
        int diff = max - min;
        int rand = random.nextInt(diff);
        return min + rand;
    }

    /**
     * 给定范围获得随机颜色
     */
    public Color randColor(Random random, int fc, int bc) {
        int colorMax = 255;
        if (fc > colorMax) {
            fc = 255;
        }
        if (bc > colorMax) {
            bc = 255;
        }
        int r = fc + random.nextInt(bc - fc);
        int g = fc + random.nextInt(bc - fc);
        int b = fc + random.nextInt(bc - fc);
        return new Color(r, g, b);
    }

}