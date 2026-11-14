package ext.library.oss.util;

import java.awt.image.BufferedImage;

/**
 * 缩略图编码器
 * <p>
 * 按输出格式注入：默认内置 webp（webp-imageio）与 ImageIO 能写的全部格式，
 * 业务方注册一个同 {@link #format()} 的 Spring Bean 即可覆盖内置实现。
 *
 * @see ImageUtil#registerEncoder(ThumbnailEncoder)
 * @see ImageUtil#thumbnail(byte[], int, String, float)
 */
public interface ThumbnailEncoder {

    /**
     * 输出格式名，如 {@code webp}、{@code jpg}，大小写不敏感
     *
     * @return 格式名
     */
    String format();

    /**
     * 编码为字节数组
     *
     * @param image   已经缩放好的图片
     * @param quality 压缩质量，0.0 到 1.0；编码器不支持质量控制时可忽略
     * @return 图片字节
     */
    byte[] encode(BufferedImage image, float quality);

}
