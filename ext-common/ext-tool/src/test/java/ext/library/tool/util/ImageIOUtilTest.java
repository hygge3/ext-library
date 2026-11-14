package ext.library.tool.util;

import ext.library.tool.exception.ExtException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("ImageIOUtil 测试")
class ImageIOUtilTest {

    @Test
    @DisplayName("内置格式可读写")
    void builtinFormats() {
        assertTrue(ImageIOUtil.isReadable("png"));
        assertTrue(ImageIOUtil.isReadable("PNG"));
        assertTrue(ImageIOUtil.isWritable("png"));
        assertTrue(ImageIOUtil.isReadable("jpeg"));
        assertTrue(ImageIOUtil.isWritable("jpeg"));
        assertTrue(ImageIOUtil.readableFormats().contains("png"));
        assertTrue(ImageIOUtil.writableFormats().contains("png"));
    }

    @Test
    @DisplayName("未知格式与 null 返回 false")
    void unknownFormat() {
        assertFalse(ImageIOUtil.isReadable("not-a-format"));
        assertFalse(ImageIOUtil.isWritable("not-a-format"));
        assertFalse(ImageIOUtil.isReadable(null));
        assertFalse(ImageIOUtil.isWritable(null));
    }

    @Test
    @DisplayName("读取非法字节时给出当前可读格式")
    void readInvalidBytes() {
        byte[] input = new byte[0];

        ExtException e = assertThrows(ExtException.class, () -> ImageIOUtil.read(new ByteArrayInputStream(input)));

        assertTrue(e.getMessage().contains("当前可读格式"));
    }

    @Test
    @DisplayName("写读往返保持尺寸")
    void writeThenRead() {
        BufferedImage source = new BufferedImage(2, 3, BufferedImage.TYPE_INT_RGB);

        byte[] bytes = ImageIOUtil.writeAsBytes(source, "png");
        BufferedImage readBack = ImageIOUtil.read(new ByteArrayInputStream(bytes));

        assertThat(readBack.getWidth()).isEqualTo(2);
        assertThat(readBack.getHeight()).isEqualTo(3);
    }

    @Test
    @DisplayName("写到文件、写到输出流、从文件读")
    void fileAndStream(@TempDir Path tempDir) {
        BufferedImage source = new BufferedImage(4, 5, BufferedImage.TYPE_INT_RGB);
        File file = tempDir.resolve("a.png").toFile();

        ImageIOUtil.write(source, "png", file);

        assertThat(ImageIOUtil.read(file).getHeight()).isEqualTo(5);
        assertThat(ImageIOUtil.read(file.getPath()).getWidth()).isEqualTo(4);

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ImageIOUtil.write(source, "png", java.io.OutputStream.class.cast(output));
        assertThat(output.size()).isPositive();
    }

    @Test
    @DisplayName("写不支持的格式抛异常而不是静默丢数据")
    void writeUnknownFormat() {
        BufferedImage source = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);

        ExtException e = assertThrows(ExtException.class,
                () -> ImageIOUtil.writeAsBytes(source, "not-a-format"));

        assertTrue(e.getMessage().contains("当前可写格式"));
    }

}
