package ext.library.oss;

import ext.library.oss.config.OssProperties;
import ext.library.oss.util.ImageIoThumbnailEncoder;
import ext.library.oss.util.ImageUtil;
import ext.library.oss.util.ThumbnailEncoder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("缩略图输出")
class OssTemplateThumbnailTest {

    @TempDir
    Path tempDir;

    private OssTemplate template() {
        OssProperties properties = new OssProperties();
        properties.setPath(tempDir.toString());
        return new OssTemplate(new LocalObjectStorage(properties), properties);
    }

    private static byte[] png(int width, int height) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        return ext.library.tool.util.ImageIOUtil.writeAsBytes(image, "png");
    }

    @Test
    @DisplayName("默认输出 webp，长边压到配置的 320")
    void defaultFormatIsWebp() throws Exception {
        OssTemplate oss = template();
        oss.putBytes("photo.png", png(1280, 640), "image/png");

        byte[] thumbnail = oss.thumbnail("photo.png");

        assertThat(new String(thumbnail, 0, 4, StandardCharsets.US_ASCII)).isEqualTo("RIFF");
        // RIFF 容器第 8 字节起是格式标识
        assertThat(new String(thumbnail, 8, 4, StandardCharsets.US_ASCII)).isEqualTo("WEBP");
        BufferedImage decoded = ImageIO.read(new ByteArrayInputStream(thumbnail));
        assertThat(decoded.getWidth()).isEqualTo(320);
        assertThat(decoded.getHeight()).isEqualTo(160);
    }

    @Test
    @DisplayName("指定格式输出，只缩不放")
    void keepsSmallImage() throws Exception {
        OssTemplate oss = template();
        oss.putBytes("small.png", png(100, 50), "image/png");

        BufferedImage decoded = ImageIO.read(new ByteArrayInputStream(oss.thumbnail("small.png", 320, "png")));

        assertThat(decoded.getWidth()).isEqualTo(100);
        assertThat(decoded.getHeight()).isEqualTo(50);
    }

    @Test
    @DisplayName("不支持的格式列出可用格式")
    void unsupportedFormat() {
        OssTemplate oss = template();
        oss.putBytes("photo.png", png(64, 64), "image/png");

        assertThat(ImageUtil.encoder("avif")).isEmpty();
        assertThat(ImageUtil.encoder("WEBP")).isPresent();
    }

    @AfterEach
    void restoreWebp() {
        // 静态注册表跨测试共享，覆盖后得还回去
        ImageUtil.registerEncoder(new ImageIoThumbnailEncoder("webp"));
    }

    @Test
    @DisplayName("注入同格式编码器后覆盖内置实现")
    void customEncoderWins() {
        ImageUtil.registerEncoder(new ThumbnailEncoder() {
            @Override
            public String format() {
                return "webp";
            }

            @Override
            public byte[] encode(BufferedImage image, float quality) {
                return "custom".getBytes();
            }
        });

        assertThat(template().thumbnail(stored(), 64)).containsExactly("custom".getBytes());
    }

    private String stored() {
        OssTemplate oss = template();
        return oss.putBytes("photo.png", png(640, 640), "image/png");
    }

}
