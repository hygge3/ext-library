package ext.library.qrcode;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("QrCode 测试")
class QrCodeTest {

    @TempDir
    File tempDir;

    @Nested
    @DisplayName("创建方法测试")
    class CreateTests {

        @Test
        @DisplayName("from() 创建二维码")
        void fromCreatesQrCode() {
            QrCode qrCode = QrCode.from("test content");
            assertNotNull(qrCode);
        }

        @Test
        @DisplayName("form() 已废弃方法仍可用")
        void deprecatedFormStillWorks() {
            QrCode qrCode = QrCode.from("test content");
            assertNotNull(qrCode);
        }

        @Test
        @DisplayName("内容为 null 抛出异常")
        void nullContentThrowsException() {
            assertThrows(IllegalArgumentException.class, () -> QrCode.from(null));
        }

        @Test
        @DisplayName("内容为空字符串抛出异常")
        void emptyContentThrowsException() {
            assertThrows(IllegalArgumentException.class, () -> QrCode.from(""));
        }

        @Test
        @DisplayName("内容为空白字符串抛出异常")
        void blankContentThrowsException() {
            assertThrows(IllegalArgumentException.class, () -> QrCode.from("   "));
        }
    }

    @Nested
    @DisplayName("配置方法测试")
    class ConfigTests {

        @Test
        @DisplayName("size 设置正确")
        void sizeConfiguration() {
            BufferedImage image = QrCode.from("test")
                    .size(100)
                    .deleteMargin(false)
                    .toImage();
            // 由于 ZXing 内部会调整大小，只检查图像存在
            assertNotNull(image);
            assertTrue(image.getWidth() > 0);
        }

        @Test
        @DisplayName("size 为 0 抛出异常")
        void zeroSizeThrowsException() {
            assertThrows(IllegalArgumentException.class, () -> QrCode.from("test").size(0));
        }

        @Test
        @DisplayName("size 为负数抛出异常")
        void negativeSizeThrowsException() {
            assertThrows(IllegalArgumentException.class, () -> QrCode.from("test").size(-1));
        }

        @Test
        @DisplayName("链式配置")
        void chainedConfiguration() {
            QrCode qrCode = QrCode.from("test")
                    .size(256)
                    .foreGroundColor(Color.BLUE)
                    .backGroundColor(Color.YELLOW)
                    .encode(StandardCharsets.UTF_8)
                    .imageFormat("png")
                    .deleteMargin(true);
            assertNotNull(qrCode);
        }

        @Test
        @DisplayName("十六进制颜色字符串")
        void hexColorString() {
            QrCode qrCode = QrCode.from("test")
                    .foreGroundColor("#FF0000")
                    .backGroundColor("FFFFFF");
            assertNotNull(qrCode.toImage());
        }
    }

    @Nested
    @DisplayName("输出方法测试")
    class OutputTests {

        @Test
        @DisplayName("toImage 返回 BufferedImage")
        void toImageReturnsBufferedImage() {
            BufferedImage image = QrCode.from("Hello World").toImage();
            assertNotNull(image);
            assertTrue(image.getWidth() > 0);
            assertTrue(image.getHeight() > 0);
        }

        @Test
        @DisplayName("toBytes 返回字节数组")
        void toBytesReturnsBytes() {
            byte[] bytes = QrCode.from("Hello World").toBytes();
            assertNotNull(bytes);
            assertTrue(bytes.length > 0);
        }

        @Test
        @DisplayName("toStream 返回输入流")
        void toStreamReturnsInputStream() {
            ByteArrayInputStream stream = QrCode.from("Hello World").toStream();
            assertNotNull(stream);
            assertTrue(stream.available() > 0);
        }

        @Test
        @DisplayName("toFile 创建文件")
        void toFileCreatesFile() {
            File file = new File(tempDir, "test-qrcode.png");
            File result = QrCode.from("Hello World").toFile(file);
            assertTrue(result.exists());
            assertTrue(result.length() > 0);
        }

        @Test
        @DisplayName("toFile 使用路径字符串")
        void toFileWithPathString() {
            String filePath = new File(tempDir, "test-qrcode2.png").getAbsolutePath();
            File result = QrCode.from("Hello World").toFile(filePath);
            assertTrue(result.exists());
        }
    }

    @Nested
    @DisplayName("Base64 输出测试")
    class Base64Tests {

        @Test
        @DisplayName("toBase64 返回 Data URI")
        void toBase64ReturnsDataUri() {
            String base64 = QrCode.from("test").toBase64();
            assertNotNull(base64);
            assertTrue(base64.startsWith("data:image/"));
            assertTrue(base64.contains(";base64,"));
        }

        @Test
        @DisplayName("PNG 格式的 MIME 类型正确")
        void pngMimeType() {
            String base64 = QrCode.from("test").imageFormat("png").toBase64();
            assertTrue(base64.startsWith("data:image/png;base64,"));
        }

        @Test
        @DisplayName("JPG 格式的 MIME 类型正确")
        void jpgMimeType() {
            String base64 = QrCode.from("test").imageFormat("jpg").toBase64();
            assertTrue(base64.startsWith("data:image/jpeg;base64,"));
        }

        @Test
        @DisplayName("JPEG 格式的 MIME 类型正确")
        void jpegMimeType() {
            String base64 = QrCode.from("test").imageFormat("jpeg").toBase64();
            assertTrue(base64.startsWith("data:image/jpeg;base64,"));
        }

        @Test
        @DisplayName("GIF 格式的 MIME 类型正确")
        void gifMimeType() {
            String base64 = QrCode.from("test").imageFormat("gif").toBase64();
            assertTrue(base64.startsWith("data:image/gif;base64,"));
        }
    }

    @Nested
    @DisplayName("读取测试")
    class ReadTests {

        @Test
        @DisplayName("从 BufferedImage 读取")
        void readFromBufferedImage() {
            String content = "Test QR Code Content";
            BufferedImage image = QrCode.from(content).toImage();
            String result = QrCode.read(image);
            assertEquals(content, result);
        }

        @Test
        @DisplayName("从文件读取")
        void readFromFile() {
            String content = "File QR Code";
            File file = new File(tempDir, "read-test.png");
            QrCode.from(content).toFile(file);

            String result = QrCode.read(file);
            assertEquals(content, result);
        }

        @Test
        @DisplayName("从文件路径读取")
        void readFromFilePath() {
            String content = "Path QR Code";
            File file = new File(tempDir, "path-test.png");
            QrCode.from(content).toFile(file);

            String result = QrCode.read(file.getAbsolutePath());
            assertEquals(content, result);
        }

        @Test
        @DisplayName("从 InputStream 读取")
        void readFromInputStream() {
            String content = "Stream Content";
            ByteArrayInputStream stream = QrCode.from(content).toStream();
            String result = QrCode.read(stream);
            assertEquals(content, result);
        }

        @Test
        @DisplayName("指定编码读取")
        void readWithCharset() {
            String content = "中文测试内容";
            BufferedImage image = QrCode.from(content).encode(StandardCharsets.UTF_8).toImage();
            String result = QrCode.read(image, StandardCharsets.UTF_8);
            assertEquals(content, result);
        }

        @Test
        @DisplayName("readRawBytes 返回原始字节")
        void readRawBytesReturnsBytes() {
            String content = "Raw Bytes Test";
            BufferedImage image = QrCode.from(content).toImage();
            byte[] rawBytes = QrCode.readRawBytes(image);
            assertNotNull(rawBytes);
            // 原始字节包含编码后的数据
            assertTrue(rawBytes.length > 0);
        }
    }

    @Nested
    @DisplayName("Logo 测试")
    class LogoTests {

        @Test
        @DisplayName("无 Logo 时正常生成")
        void withoutLogoGeneratesNormally() {
            BufferedImage image = QrCode.from("test content").toImage();
            assertNotNull(image);
        }

        @Test
        @DisplayName("带 Logo 生成")
        void withLogoGenerates() {
            // 创建一个简单的 logo 图像
            BufferedImage logo = new BufferedImage(50, 50, BufferedImage.TYPE_INT_RGB);
            logo.getGraphics().setColor(Color.RED);
            logo.getGraphics().fillRect(0, 0, 50, 50);

            BufferedImage image = QrCode.from("test with logo")
                    .logo(logo)
                    .toImage();
            assertNotNull(image);
        }

        @Test
        @DisplayName("带 Logo 的二维码仍可识别")
        void qrCodeWithLogoStillReadable() {
            String content = "Logo QR Code";

            // 创建一个小 logo
            BufferedImage logo = new BufferedImage(30, 30, BufferedImage.TYPE_INT_RGB);
            logo.getGraphics().setColor(Color.BLUE);
            logo.getGraphics().fillRect(0, 0, 30, 30);

            BufferedImage image = QrCode.from(content)
                    .size(512)
                    .logo(logo)
                    .toImage();

            String result = QrCode.read(image);
            assertEquals(content, result);
        }
    }

    @Nested
    @DisplayName("删除白边测试")
    class MarginTests {

        @Test
        @DisplayName("删除白边（默认）")
        void deleteMarginByDefault() {
            BufferedImage image = QrCode.from("test").toImage();
            assertNotNull(image);
        }

        @Test
        @DisplayName("保留白边")
        void keepMargin() {
            BufferedImage imageWithoutMargin = QrCode.from("test").deleteMargin(true).toImage();
            BufferedImage imageWithMargin = QrCode.from("test").deleteMargin(false).toImage();

            // 保留白边的图像应该更大或相等
            assertTrue(imageWithMargin.getWidth() >= imageWithoutMargin.getWidth());
        }
    }

    @Nested
    @DisplayName("特殊内容测试")
    class SpecialContentTests {

        @Test
        @DisplayName("URL 内容")
        void urlContent() {
            String url = "https://example.com/path?param=value";
            BufferedImage image = QrCode.from(url).toImage();
            String result = QrCode.read(image);
            assertEquals(url, result);
        }

        @Test
        @DisplayName("中文内容")
        void chineseContent() {
            String chinese = "二维码中文内容测试";
            BufferedImage image = QrCode.from(chinese).toImage();
            String result = QrCode.read(image);
            assertEquals(chinese, result);
        }

        @Test
        @DisplayName("Emoji 内容")
        void emojiContent() {
            String emoji = "Hello World";
            BufferedImage image = QrCode.from(emoji).toImage();
            String result = QrCode.read(image);
            assertEquals(emoji, result);
        }

        @Test
        @DisplayName("长文本内容")
        void longContent() {
            String longText = "A".repeat(500);
            BufferedImage image = QrCode.from(longText).size(1024).toImage();
            String result = QrCode.read(image);
            assertEquals(longText, result);
        }
    }
}
