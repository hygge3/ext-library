package ext.library.oss.util;

import ext.library.tool.exception.ExtException;
import ext.library.tool.util.ImageIOUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("ImageUtil 工具类测试")
class ImageUtilTest {

    @Test
    @DisplayName("webp 保留透明通道")
    void webpKeepsAlpha() {
        BufferedImage source = new BufferedImage(8, 8, BufferedImage.TYPE_INT_ARGB);
        source.setRGB(0, 0, 0x00FF0000);
        byte[] webp = ImageUtil.thumbnail(ImageIOUtil.writeAsBytes(source, "png"), 8, "webp", 0.8f);

        BufferedImage decoded = ImageIOUtil.read(webp);

        assertTrue(decoded.getColorModel().hasAlpha(), "webp 解码后丢了 alpha 通道");
        assertThat(decoded.getRGB(0, 0) >>> 24).isZero();
    }

    @Test
    @DisplayName("缩略图不丢透明度")
    void scaleKeepsAlpha() {
        BufferedImage source = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
        source.setRGB(0, 0, 0x00000000);

        BufferedImage shrunk = ImageUtil.scale(source, 32);

        assertTrue(shrunk.getColorModel().hasAlpha());
        assertThat(shrunk.getRGB(0, 0) >>> 24).isZero();
    }

    @Test
    @DisplayName("webp 由 webp-imageio 提供，读写都可用")
    void webpIsSupported() {
        assertTrue(ImageIOUtil.isReadable("webp"), "classpath 缺少 webp-imageio");
        assertTrue(ImageIOUtil.isWritable("webp"));
        assertTrue(ImageUtil.encoder("WEBP").isPresent());
    }

    @Test
    @DisplayName("缩略图只缩不放，输出指定格式")
    void thumbnailKeepsSmallAndEncodes() throws IOException {
        byte[] source = ImageIOUtil.writeAsBytes(png(1280, 640), "png");

        byte[] shrunk = ImageUtil.thumbnail(source, 320, "png", 0.8f);
        byte[] untouched = ImageUtil.thumbnail(ImageIOUtil.writeAsBytes(png(100, 50), "png"), 320, "png", 0.8f);

        assertThat(ImageIOUtil.read(shrunk).getWidth()).isEqualTo(320);
        assertThat(ImageIOUtil.read(untouched).getWidth()).isEqualTo(100);
    }

    @Test
    @DisplayName("未知缩略图格式抛出带可用格式的异常")
    void thumbnailUnknownFormat() {
        byte[] source = ImageIOUtil.writeAsBytes(png(8, 8), "png");

        ExtException e = assertThrows(ExtException.class, () -> ImageUtil.thumbnail(source, 32, "not-a-format", 0.8f));

        assertTrue(e.getMessage().contains("不支持的缩略图格式"));
    }

    private static BufferedImage png(int width, int height) {
        return new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
    }

}
