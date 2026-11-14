package ext.library.oss.util;

import ext.library.tool.constant.EmojiSymbol;
import ext.library.tool.exception.ExtException;
import ext.library.tool.util.ImageIOUtil;
import org.jspecify.annotations.Nullable;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * 图片处理工具类
 * <p>
 * 图片能力统一入口：读取、写出、等比缩放、格式探测、缩略图编码器注册表。
 * <p>
 * 可用的图片格式取决于运行时的 ImageIO 插件：JDK 内置 JPEG、PNG、GIF、BMP、WBMP、TIFF；
 * WebP 由 ext-oss 依赖的 webp-imageio 提供，AVIF/JPEG XL 等格式挂上对应插件即自动生效。
 * 可用 {@link #readableFormats()} 确认当前运行时支持哪些格式，或用 {@link #isReadable(String)}
 * 在调用前判断，避免拿到异常后再兜底。
 *
 * @since 2025.01.01
 */
public final class ImageUtil {

    /**
     * 缩略图编码器注册表，键为小写格式名
     * <p>
     * 内置项按 ImageIO 可写能力自动探测，业务方注册同格式实现即覆盖。
     */
    private static final Map<String, ThumbnailEncoder> ENCODERS = new LinkedHashMap<>();

    static {
        // webp 走 webp-imageio；jar 不在 classpath 时不要把它暴露成「支持」
        if (ImageIOUtil.isWritable("webp")) {
            ENCODERS.put("webp", new ImageIoThumbnailEncoder("webp"));
        }
        for (String format : ImageIOUtil.writableFormats()) {
            ENCODERS.putIfAbsent(format, new ImageIoThumbnailEncoder(format));
        }
    }

    private ImageUtil() {
        // 防止实例化
    }

    // region 缩略图

    /**
     * 注册缩略图编码器，同格式覆盖内置实现
     *
     * @param encoder 编码器
     */
    public static synchronized void registerEncoder(ThumbnailEncoder encoder) {
        ENCODERS.put(normalize(encoder.format()), encoder);
    }

    /**
     * 当前支持的缩略图输出格式
     *
     * @return 格式名到编码器的映射（不可变）
     */
    public static Map<String, ThumbnailEncoder> encoders() {
        return Map.copyOf(ENCODERS);
    }

    /**
     * 按格式取缩略图编码器
     *
     * @param format 格式名，大小写不敏感
     * @return 编码器
     */
    public static Optional<ThumbnailEncoder> encoder(@Nullable String format) {
        if (format == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(ENCODERS.get(normalize(format)));
    }

    private static String normalize(String formatName) {
        return formatName.toLowerCase(Locale.ROOT);
    }

    /**
     * 生成缩略图：解码、等比缩到长边 {@code maxEdge}、按格式编码
     *
     * @param source  原图字节
     * @param maxEdge 长边上限（像素），只缩不放
     * @param format  输出格式，需在 {@link #encoders()} 内
     * @param quality 压缩质量，0.0 到 1.0，无损格式忽略
     * @return 缩略图字节
     * @throws ExtException 格式不支持或编解码失败
     */
    public static byte[] thumbnail(byte[] source, int maxEdge, String format, float quality) {
        ThumbnailEncoder encoder = encoder(format).orElseThrow(() -> new ExtException(EmojiSymbol.OSS,
                "不支持的缩略图格式：{}，可用格式：{}", format, encoders().keySet()));
        return encoder.encode(scale(ImageIOUtil.read(source), maxEdge), quality);
    }

    // endregion

    // region 缩放图片

    /**
     * 等比缩放到长边不超过 {@code maxEdge}
     * <p>
     * 只缩不放：原图长边已经不超过 {@code maxEdge} 时原样返回。透明通道保留。
     *
     * @param source  原图
     * @param maxEdge 长边上限（像素），小于等于 0 时原样返回
     * @return 缩放后的图片
     */
    public static BufferedImage scale(BufferedImage source, int maxEdge) {
        int width = source.getWidth();
        int height = source.getHeight();
        int longEdge = Math.max(width, height);
        if (maxEdge <= 0 || longEdge <= maxEdge) {
            return source;
        }
        double ratio = (double) maxEdge / longEdge;
        int targetWidth = Math.max(1, (int) Math.round(width * ratio));
        int targetHeight = Math.max(1, (int) Math.round(height * ratio));
        BufferedImage target = new BufferedImage(targetWidth, targetHeight, source.getColorModel().hasAlpha()
                ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = target.createGraphics();
        try {
            graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            graphics.drawImage(source, 0, 0, targetWidth, targetHeight, null);
        } finally {
            graphics.dispose();
        }
        return target;
    }

    // endregion

    // region 写出图片

    /**
     * 写出图片到 ImageOutputStream
     *
     * @param image      待写出的图片
     * @param formatName 图片格式名称（如 "png", "jpg"）
     * @param output     目标输出流
     * @throws ExtException 如果写出失败或找不到合适的 writer
     */
    public static void write(RenderedImage image, String formatName, ImageOutputStream output) {
        try {
            requireWritten(ImageIO.write(image, formatName, output), formatName);
        } catch (IOException e) {
            throw new ExtException(EmojiSymbol.OSS, e);
        }
    }

    /**
     * 写出图片到文件
     *
     * @param image      待写出的图片
     * @param formatName 图片格式名称（如 "png", "jpg"）
     * @param output     目标文件
     * @throws ExtException 如果写出失败或找不到合适的 writer
     */
    /**
     * 写出图片到输出流
     * <p>
     * 调用方负责关闭输出流，本方法不会关闭它。
     *
     * @param image      待写出的图片
     * @param formatName 图片格式名称（如 "png", "jpg"）
     * @param output     目标输出流
     * @throws ExtException 如果写出失败或找不到合适的 writer
     */
    /**
     * ImageIO.write 在找不到 writer 时只返回 false、什么都不写，静默丢数据比抛异常危险得多，
     * 因此统一转成异常
     */
    private static void requireWritten(boolean written, String formatName) {
        if (!written) {
            throw new ExtException(EmojiSymbol.OSS,
                    "不支持的图片格式：{}，当前可写格式：{}", formatName, ImageIOUtil.writableFormats());
        }
    }

    /**
     * 将图片写出为字节数组
     *
     * @param image      待写出的图片
     * @param formatName 图片格式名称（如 "png", "jpg"）
     * @return 图片字节数组
     * @throws ExtException 如果写出失败或找不到合适的 writer
     */
    /**
     * 将图片写出为字节数组，并指定压缩质量
     * <p>
     * 质量只在编码器声明支持压缩时生效（jpeg、webp 等有损格式），无损格式会被忽略。
     *
     * @param image      待写出的图片
     * @param formatName 图片格式名称（如 "png", "jpg", "webp"）
     * @param quality    压缩质量，0.0 到 1.0
     * @return 图片字节数组
     * @throws ExtException 如果写出失败或找不到合适的 writer
     */
    public static byte[] writeAsBytes(RenderedImage image, String formatName, float quality) {
        String format = normalize(formatName);
        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName(format);
        if (!writers.hasNext()) {
            throw new ExtException(EmojiSymbol.OSS,
                    "不支持的图片格式：{}，当前可写格式：{}", formatName, ImageIOUtil.writableFormats());
        }
        ImageWriter writer = writers.next();
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try (ImageOutputStream stream = ImageIO.createImageOutputStream(output)) {
            writer.setOutput(stream);
            ImageWriteParam param = writer.getDefaultWriteParam();
            if (param.canWriteCompressed()) {
                param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                // webp 这类编码器要求先选压缩类型（Lossy/Lossless）才能设质量
                String[] types = param.getCompressionTypes();
                if (param.getCompressionType() == null && types != null && types.length > 0) {
                    param.setCompressionType(types[0]);
                }
                param.setCompressionQuality(Math.clamp(quality, 0f, 1f));
            }
            writer.write(null, new IIOImage(image, null, null), param);
        } catch (IOException e) {
            throw new ExtException(EmojiSymbol.OSS, e);
        } finally {
            writer.dispose();
        }
        return output.toByteArray();
    }

    /**
     * 将图片写出为输入流
     *
     * @param image      待写出的图片
     * @param formatName 图片格式名称（如 "png", "jpg"）
     * @return 包含图片数据的 ByteArrayInputStream
     * @throws ExtException 如果写出失败或找不到合适的 writer
     */
    public static ByteArrayInputStream writeAsStream(RenderedImage image, String formatName) {
        return new ByteArrayInputStream(ImageIOUtil.writeAsBytes(image, formatName));
    }

    // endregion

}
