package ext.library.tool.util;

import ext.library.tool.exception.ToolException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("ImageUtil 工具类测试")
class ImageUtilTest {

    @Test
    @DisplayName("内置格式可读写")
    void builtInFormats() {
        assertTrue(ImageUtil.isReadable("png"));
        assertTrue(ImageUtil.isReadable("PNG"));
        assertTrue(ImageUtil.isWritable("png"));
        assertTrue(ImageUtil.isReadable("jpeg"));
        assertTrue(ImageUtil.isWritable("jpeg"));

        assertTrue(ImageUtil.readableFormats().contains("png"));
        assertTrue(ImageUtil.writableFormats().contains("png"));
    }

    @Test
    @DisplayName("未知格式判定为不可读写")
    void unknownFormat() {
        assertFalse(ImageUtil.isReadable("not-a-format"));
        assertFalse(ImageUtil.isWritable("not-a-format"));
        assertFalse(ImageUtil.isReadable(null));
        assertFalse(ImageUtil.isWritable(null));
    }

    @Test
    @DisplayName("WebP 读取由 TwelveMonkeys 插件提供")
    void webpReadable() {
        // 依赖 testImplementation imageio-webp，插件经 SPI 自动注册
        assertTrue(ImageUtil.isReadable("webp"), "测试类路径缺少 imageio-webp 插件");
        assertTrue(ImageUtil.readableFormats().contains("webp"));
    }

    @Test
    @DisplayName("WebP 无写入支持，写出时抛出明确异常而非静默丢弃")
    void webpNotWritable() {
        assertFalse(ImageUtil.isWritable("webp"));
        assertThrows(ToolException.class,
                () -> ImageUtil.writeAsBytes(new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB), "webp"));
    }

    @Test
    @DisplayName("读取无法识别的数据抛出异常而不是返回 null")
    void readUnsupportedData() {
        ByteArrayInputStream input = new ByteArrayInputStream("not an image".getBytes(StandardCharsets.UTF_8));
        ToolException e = assertThrows(ToolException.class, () -> ImageUtil.read(input));
        assertTrue(e.getMessage().contains("无法识别的图片格式"), e::getMessage);
    }

    @Test
    @DisplayName("PNG 字节数组可往返读写")
    void roundTripPng() {
        BufferedImage source = new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB);
        source.setRGB(0, 0, 0xFF0000);

        byte[] bytes = ImageUtil.writeAsBytes(source, "png");
        BufferedImage readBack = ImageUtil.read(new ByteArrayInputStream(bytes));

        assertNotNull(readBack);
        assertTrue(ImageUtil.isReadable("png"));
    }

}
