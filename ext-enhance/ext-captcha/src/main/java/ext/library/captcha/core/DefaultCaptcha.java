package ext.library.captcha.core;

import ext.library.captcha.draw.BackgroundDraw;
import ext.library.captcha.draw.CaptchaDraw;
import ext.library.captcha.draw.CubicCurveInterferenceDraw;
import ext.library.captcha.draw.InterferenceDraw;
import ext.library.captcha.draw.RandomCaptchaDraw;
import ext.library.captcha.draw.SmallCharBackgroundDraw;
import ext.library.captcha.CaptchaProperties;
import ext.library.captcha.CaptchaType;
import ext.library.tool.constant.EmojiSymbol;
import ext.library.tool.constant.Singletons;
import ext.library.tool.exception.ExtException;
import org.springframework.core.io.ClassPathResource;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.function.Supplier;

/**
 * 验证码默认实现
 */
public class DefaultCaptcha implements Captcha {

    /**
     * 默认图像宽度
     */
    private static final int DEFAULT_WIDTH = 130;

    /**
     * 默认图像高度
     */
    private static final int DEFAULT_HEIGHT = 48;

    /**
     * 默认图像格式
     */
    private static final String DEFAULT_IMAGE_FORMAT = "JPEG";

    private static final String[] FONT_NAMES = new String[]{"marker.ttf", "american.ttf", "papyrus.ttf"};

    private final Font[] fonts;
    private final int width;
    private final int height;
    private final String imageFormat;
    private BackgroundDraw backgroundDraw;
    private CaptchaDraw captchaDraw;
    private InterferenceDraw interferenceDraw;
    private Random random;

    public DefaultCaptcha() {
        this(new RandomCaptchaDraw());
    }

    public DefaultCaptcha(CaptchaType type) {
        this(type.getCaptchaDraw());
    }

    public DefaultCaptcha(CaptchaDraw captchaDraw) {
        this(SmallCharBackgroundDraw.INSTANCE, captchaDraw, CubicCurveInterferenceDraw.INSTANCE, Singletons.SECURE_RANDOM,
                DEFAULT_WIDTH, DEFAULT_HEIGHT, DEFAULT_IMAGE_FORMAT);
    }

    public DefaultCaptcha(CaptchaProperties properties) {
        this(SmallCharBackgroundDraw.INSTANCE, properties.getCaptchaType().getCaptchaDraw(),
                CubicCurveInterferenceDraw.INSTANCE, Singletons.SECURE_RANDOM,
                properties.getWidth(), properties.getHeight(), properties.getImageFormat());
    }

    public DefaultCaptcha(BackgroundDraw backgroundDraw, CaptchaDraw captchaDraw, InterferenceDraw interferenceDraw,
                          Random random, int width, int height, String imageFormat) {
        this.backgroundDraw = backgroundDraw;
        this.captchaDraw = captchaDraw;
        this.interferenceDraw = interferenceDraw;
        this.random = random;
        this.width = width;
        this.height = height;
        this.imageFormat = imageFormat;
        this.fonts = loadAndRegisterFont();
    }

    private static Graphics2D initGraphics(BufferedImage image) {
        // 获取图形上下文
        Graphics2D graphics = image.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        // 图形抗锯齿
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        // 字体抗锯齿
        graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        return graphics;
    }

    private static Font[] loadAndRegisterFont() {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        List<Font> fontList = new ArrayList<>();
        for (String fontName : FONT_NAMES) {
            String path = "fonts/" + fontName;
            // 加载字体
            Font font = loadFont(new ClassPathResource(path));
            // 注册字体
            ge.registerFont(font);
            fontList.add(font);
        }
        return fontList.toArray(new Font[0]);
    }

    private static Font loadFont(ClassPathResource resource) {
        try {
            return Font.createFont(Font.TRUETYPE_FONT, resource.getInputStream());
        } catch (FontFormatException | IOException e) {
            throw new ExtException(EmojiSymbol.CAPTCHA, "加载字体失败");
        }
    }

    public void setBackgroundDraw(BackgroundDraw backgroundDraw) {
        this.backgroundDraw = Objects.requireNonNull(backgroundDraw, "BackgroundDraw 为 null");
    }

    public void setCaptchaDraw(CaptchaDraw captchaDraw) {
        this.captchaDraw = Objects.requireNonNull(captchaDraw, "CaptchaDraw 为 null");
    }

    public void setInterferenceDraw(InterferenceDraw interferenceDraw) {
        this.interferenceDraw = Objects.requireNonNull(interferenceDraw, "InterferenceDraw 为 null");
    }

    public void setRandom(Random random) {
        this.random = Objects.requireNonNull(random, "Random 为 null");
    }

    @Override
    public String generate(Supplier<OutputStream> supplier) {
        // 初始化画布
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = initGraphics(image);
        // 背景层
        backgroundDraw.draw(graphics, width, height, fonts, random);
        // 验证码层
        String captcha = captchaDraw.draw(graphics, width, height, fonts, random);
        // 干扰层
        interferenceDraw.draw(graphics, width, height, fonts, random);
        try (OutputStream os = supplier.get()) {
            ImageIO.write(image, imageFormat, os);
            return captcha;
        } catch (IOException e) {
            throw new ExtException(EmojiSymbol.CAPTCHA, "生成验证码失败");
        } finally {
            graphics.dispose();
        }
    }

    @Override
    public boolean validate(String code, String userInputCaptcha) {
        return captchaDraw.validate(code, userInputCaptcha);
    }
}
