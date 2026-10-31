package ext.library.tool.util;

import ext.library.tool.constant.EmojiSymbol;
import ext.library.tool.exception.ToolException;
import org.jspecify.annotations.Nullable;

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
import java.util.Collections;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;

/**
 * 图片处理工具类
 * <p>
 * 封装 {@link ImageIO} 常用操作，提供统一的异常处理。
 * <p>
 * 可用的图片格式取决于运行时的 ImageIO 插件：JDK 内置 JPEG、PNG、GIF、BMP、WBMP、TIFF；
 * <b>WebP 不在其中</b>，需要业务方自行引入纯 Java 插件（版本由 ext-bom 统一管理）：
 *
 * <pre>{@code
 * implementation("com.twelvemonkeys.imageio:imageio-webp")
 * }</pre>
 * <p>
 * 插件通过 SPI 自动注册，无需任何代码改动。可用 {@link #readableFormats()} 确认当前运行时支持哪些格式，
 * 或用 {@link #isReadable(String)} 在调用前判断，避免拿到异常后再兜底。
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
     * @return BufferedImage 对象
     * @throws ToolException 如果读取失败或格式不受支持
     */
    public static BufferedImage read(File input) {
        try {
            return requireImage(ImageIO.read(input), input.getName());
        } catch (IOException e) {
            throw new ToolException(EmojiSymbol.TOOL, e);
        }
    }

    /**
     * 从输入流读取图片
     * <p>
     * 调用方负责关闭输入流，本方法不会关闭它。
     *
     * @param input 图片输入流
     * @return BufferedImage 对象
     * @throws ToolException 如果读取失败或格式不受支持
     */
    public static BufferedImage read(InputStream input) {
        try {
            return requireImage(ImageIO.read(input), null);
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
     * @return BufferedImage 对象
     * @throws IllegalArgumentException 如果 url 为空
     * @throws ToolException            如果读取失败或格式不受支持
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
     * @return BufferedImage 对象
     * @throws ToolException 如果读取失败或格式不受支持
     */
    public static BufferedImage read(URL url) {
        try {
            return requireImage(ImageIO.read(url), url.getPath());
        } catch (IOException e) {
            throw new ToolException(EmojiSymbol.TOOL, e);
        }
    }

    private static BufferedImage readUrl(String url) {
        try {
            return read(URI.create(url).toURL());
        } catch (IOException e) {
            throw new ToolException(EmojiSymbol.TOOL, e);
        }
    }

    /**
     * ImageIO 遇到无法识别的格式时返回 null 而不是抛异常，直接透传会让调用方在后续操作上空指针，
     * 因此在这里转成带线索的异常
     */
    private static BufferedImage requireImage(@Nullable BufferedImage image, @Nullable String source) {
        if (image != null) {
            return image;
        }
        throw new ToolException(EmojiSymbol.TOOL,
                "无法识别的图片格式（缺少对应的 ImageIO 插件？）：{}，当前可读格式：{}",
                source == null ? "<输入流>" : source, readableFormats());
    }

    // endregion

    // region 格式探测

    /**
     * 当前运行时是否可读取指定格式
     *
     * @param formatName 格式名称（如 "png"、"webp"），不区分大小写
     * @return 存在对应 reader 时返回 true
     */
    public static boolean isReadable(@Nullable String formatName) {
        return formatName != null && ImageIO.getImageReadersByFormatName(normalize(formatName)).hasNext();
    }

    /**
     * 当前运行时是否可写出指定格式
     *
     * @param formatName 格式名称（如 "png"、"webp"），不区分大小写
     * @return 存在对应 writer 时返回 true
     */
    public static boolean isWritable(@Nullable String formatName) {
        return formatName != null && ImageIO.getImageWritersByFormatName(normalize(formatName)).hasNext();
    }

    /**
     * 当前运行时可读取的全部格式
     * <p>
     * 结果取决于 classpath 上的 ImageIO 插件，可直接用于诊断「为什么读不了某个格式」。
     *
     * @return 格式名称集合（小写、已排序）
     */
    public static Set<String> readableFormats() {
        return formats(ImageIO.getReaderFormatNames());
    }

    /**
     * 当前运行时可写出的全部格式
     *
     * @return 格式名称集合（小写、已排序）
     */
    public static Set<String> writableFormats() {
        return formats(ImageIO.getWriterFormatNames());
    }

    private static Set<String> formats(String[] names) {
        Set<String> formats = new TreeSet<>();
        for (String name : names) {
            formats.add(normalize(name));
        }
        return Collections.unmodifiableSet(formats);
    }

    private static String normalize(String formatName) {
        return formatName.toLowerCase(Locale.ROOT);
    }

    // endregion

    // region 写出图片

    /**
     * 写出图片到 ImageOutputStream
     *
     * @param image      待写出的图片
     * @param formatName 图片格式名称（如 "png", "jpg"）
     * @param output     目标输出流
     * @throws ToolException 如果写出失败或找不到合适的 writer
     */
    public static void write(RenderedImage image, String formatName, ImageOutputStream output) {
        try {
            requireWritten(ImageIO.write(image, formatName, output), formatName);
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
     * @throws ToolException 如果写出失败或找不到合适的 writer
     */
    public static void write(RenderedImage image, String formatName, File output) {
        try {
            requireWritten(ImageIO.write(image, formatName, output), formatName);
        } catch (IOException e) {
            throw new ToolException(EmojiSymbol.TOOL, e);
        }
    }

    /**
     * 写出图片到输出流
     * <p>
     * 调用方负责关闭输出流，本方法不会关闭它。
     *
     * @param image      待写出的图片
     * @param formatName 图片格式名称（如 "png", "jpg"）
     * @param output     目标输出流
     * @throws ToolException 如果写出失败或找不到合适的 writer
     */
    public static void write(RenderedImage image, String formatName, OutputStream output) {
        try {
            requireWritten(ImageIO.write(image, formatName, output), formatName);
        } catch (IOException e) {
            throw new ToolException(EmojiSymbol.TOOL, e);
        }
    }

    /**
     * ImageIO.write 在找不到 writer 时只返回 false、什么都不写，静默丢数据比抛异常危险得多，
     * 因此统一转成异常
     */
    private static void requireWritten(boolean written, String formatName) {
        if (!written) {
            throw new ToolException(EmojiSymbol.TOOL,
                    "不支持的图片格式：{}，当前可写格式：{}", formatName, writableFormats());
        }
    }

    /**
     * 将图片写出为字节数组
     *
     * @param image      待写出的图片
     * @param formatName 图片格式名称（如 "png", "jpg"）
     * @return 图片字节数组
     * @throws ToolException 如果写出失败或找不到合适的 writer
     */
    public static byte[] writeAsBytes(RenderedImage image, String formatName) {
        try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            write(image, formatName, output);
            return output.toByteArray();
        } catch (IOException e) {
            throw new ToolException(EmojiSymbol.TOOL, e);
        }
    }

    /**
     * 将图片写出为输入流
     *
     * @param image      待写出的图片
     * @param formatName 图片格式名称（如 "png", "jpg"）
     * @return 包含图片数据的 ByteArrayInputStream
     * @throws ToolException 如果写出失败或找不到合适的 writer
     */
    public static ByteArrayInputStream writeAsStream(RenderedImage image, String formatName) {
        return new ByteArrayInputStream(writeAsBytes(image, formatName));
    }

    // endregion

}
