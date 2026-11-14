package ext.library.oss.util;

import java.awt.image.BufferedImage;
import java.util.Locale;
import java.util.Objects;

/**
 * 基于 {@link javax.imageio.ImageIO} 的缩略图编码器
 * <p>
 * 可写格式取决于 classpath 上的 ImageIO 插件：JDK 自带 jpeg/png/gif/bmp/tiff，
 * webp 由 ext-oss 依赖的 webp-imageio 提供。
 */
public class ImageIoThumbnailEncoder implements ThumbnailEncoder {

    private final String format;

    /**
     * @param format 输出格式，如 {@code webp}
     */
    public ImageIoThumbnailEncoder(String format) {
        this.format = Objects.requireNonNull(format, "format is null").toLowerCase(Locale.ROOT);
    }

    @Override
    public String format() {
        return format;
    }

    @Override
    public byte[] encode(BufferedImage image, float quality) {
        return ImageUtil.writeAsBytes(image, format, quality);
    }

}
