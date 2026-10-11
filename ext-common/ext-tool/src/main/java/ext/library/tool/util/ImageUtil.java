package ext.library.tool.util;

import ext.library.tool.constant.EmojiSymbol;
import ext.library.tool.exception.ToolException;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageOutputStream;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URL;

/**
 * 图片处理工具类
 * <p>
 * 封装 {@link ImageIO} 常用操作，提供统一的异常处理。
 * 支持多种图片格式的读取和写出。
 *
 * @since 2025.01.01
 */
public final class ImageUtil {

    private ImageUtil() {
        // 防止实例化
    }

    // region 读取图片

    /**
     * 从文件读取图片
     *
     * @param input 图片文件
     *
     * @return BufferedImage 对象
     *
     * @throws ToolException 如果读取失败
     */
    public static BufferedImage read(File input) {
        try {
            return ImageIO.read(input);
        } catch (IOException e) {
            throw new ToolException(EmojiSymbol.TOOL, e);
        }
    }

    /**
     * 从输入流读取图片
     *
     * @param input 图片输入流
     *
     * @return BufferedImage 对象
     *
     * @throws ToolException 如果读取失败
     */
    public static BufferedImage read(InputStream input) {
        try {
            return ImageIO.read(input);
        } catch (IOException e) {
            throw new ToolException(EmojiSymbol.TOOL, e);
        }
    }

    /**
     * 从 URL 地址读取图片
     * <p>
     * 支持 http/https 网络地址和本地文件路径
     *
     * @param url 图片地址（http/https URL 或本地文件路径）
     *
     * @return BufferedImage 对象
     *
     * @throws IllegalArgumentException 如果 url 为空
     * @throws ToolException            如果读取失败
     */
    public static BufferedImage read(String url) {
        if (url.startsWith("http://") || url.startsWith("https://")) {
            return readUrl(url);
        }
        return read(new File(url));
    }

    /**
     * 从 URL 对象读取图片
     *
     * @param url 图片 URL
     *
     * @return BufferedImage 对象
     *
     * @throws ToolException 如果读取失败
     */
    public static BufferedImage read(URL url) {
        try {
            return ImageIO.read(url);
        } catch (IOException e) {
            throw new ToolException(EmojiSymbol.TOOL, e);
        }
    }

    private static BufferedImage readUrl(String url) {
        try {
            return ImageIO.read(URI.create(url).toURL());
        } catch (IOException e) {
            throw new ToolException(EmojiSymbol.TOOL, e);
        }
    }

    // endregion

    // region 写出图片

    /**
     * 写出图片到 ImageOutputStream
     *
     * @param image      待写出的图片
     * @param formatName 图片格式名称（如 "png", "jpg"）
     * @param output     目标输出流
     *
     * @return 如果找到合适的 writer 返回 true，否则返回 false
     *
     * @throws ToolException 如果写出失败
     */
    public static boolean write(RenderedImage image, String formatName, ImageOutputStream output) {
        try {
            return ImageIO.write(image, formatName, output);
        } catch (IOException e) {
            throw new ToolException(EmojiSymbol.TOOL, e);
        }
    }

    /**
     * 写出图片到文件
     *
     * @param image      待写出的图片
     * @param formatName 图片格式名称（如 "png", "jpg"）
     * @param output     目标文件
     *
     * @return 如果找到合适的 writer 返回 true，否则返回 false
     *
     * @throws ToolException 如果写出失败
     */
    public static boolean write(RenderedImage image, String formatName, File output) {
        try {
            return ImageIO.write(image, formatName, output);
        } catch (IOException e) {
            throw new ToolException(EmojiSymbol.TOOL, e);
        }
    }

    /**
     * 写出图片到输出流
     *
     * @param image      待写出的图片
     * @param formatName 图片格式名称（如 "png", "jpg"）
     * @param output     目标输出流
     *
     * @return 如果找到合适的 writer 返回 true，否则返回 false
     *
     * @throws ToolException 如果写出失败
     */
    public static boolean write(RenderedImage image, String formatName, OutputStream output) {
        try {
            return ImageIO.write(image, formatName, output);
        } catch (IOException e) {
            throw new ToolException(EmojiSymbol.TOOL, e);
        }
    }

    /**
     * 将图片写出为字节数组
     *
     * @param image      待写出的图片
     * @param formatName 图片格式名称（如 "png", "jpg"）
     *
     * @return 图片字节数组
     *
     * @throws ToolException 如果写出失败或找不到合适的 writer
     */
    public static byte[] writeAsBytes(RenderedImage image, String formatName) {
        try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            if (ImageIO.write(image, formatName, output)) {
                return output.toByteArray();
            }
            throw new ToolException(EmojiSymbol.TOOL, "不支持的图片格式：{}", formatName);
        } catch (IOException e) {
            throw new ToolException(EmojiSymbol.TOOL, e);
        }
    }

    /**
     * 将图片写出为输入流
     *
     * @param image      待写出的图片
     * @param formatName 图片格式名称（如 "png", "jpg"）
     *
     * @return 包含图片数据的 ByteArrayInputStream
     *
     * @throws ToolException 如果写出失败或找不到合适的 writer
     */
    public static ByteArrayInputStream writeAsStream(RenderedImage image, String formatName) {
        return new ByteArrayInputStream(writeAsBytes(image, formatName));
    }

    // endregion

}
