package ext.library.tool.util;

import ext.library.tool.constant.EmojiSymbol;
import ext.library.tool.exception.ExtException;
import org.jspecify.annotations.Nullable;

import javax.imageio.ImageIO;
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
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;

/**
 * ImageIO 薄封装：读、写、格式探测
 * <p>
 * 只做「ImageIO 会静默失败的地方转成异常」这一件事，不含缩略图、质量控制等策略——
 * 那些在 [ext-oss] 的 {@code ImageUtil}。放在 ext-tool 是因为 ext-qrcode 与 ext-oss 都要用，
 * 而二者分属不同层，只能落在两者都能依赖的这一层。
 * <p>
 * 可读写哪些格式取决于 classpath 上的 ImageIO 插件，用 {@link #readableFormats()} /
 * {@link #writableFormats()} 查。
 *
 * @since 2026.01.01
 */
public final class ImageIOUtil {

    private ImageIOUtil() {
    }

    /**
     * 从文件读取图片
     *
     * @param input 图片文件
     *
     * @return 图片
     *
     * @throws ExtException 读取失败或格式不受支持
     */
    public static BufferedImage read(File input) {
        try {
            return requireImage(ImageIO.read(input), input.getName());
        } catch (IOException e) {
            throw new ExtException(EmojiSymbol.TOOL, e);
        }
    }

    /**
     * 从输入流读取图片
     * <p>
     * 调用方负责关闭输入流，本方法不会关闭它。
     *
     * @param input 图片输入流
     *
     * @return 图片
     *
     * @throws ExtException 读取失败或格式不受支持
     */
    public static BufferedImage read(InputStream input) {
        try {
            return requireImage(ImageIO.read(input), null);
        } catch (IOException e) {
            throw new ExtException(EmojiSymbol.TOOL, e);
        }
    }

    /**
     * 从字节数组读取图片
     *
     * @param input 图片字节
     *
     * @return 图片
     *
     * @throws ExtException 读取失败或格式不受支持
     */
    public static BufferedImage read(byte[] input) {
        return read(new ByteArrayInputStream(input));
    }

    /**
     * 从 URL 对象读取图片
     *
     * @param url 图片 URL
     *
     * @return 图片
     *
     * @throws ExtException 读取失败或格式不受支持
     */
    public static BufferedImage read(URL url) {
        try {
            return requireImage(ImageIO.read(url), url.getPath());
        } catch (IOException e) {
            throw new ExtException(EmojiSymbol.TOOL, e);
        }
    }

    /**
     * 从 http/https 地址或本地文件路径读取图片
     *
     * @param pathOrUrl 图片地址或本地路径
     *
     * @return 图片
     *
     * @throws ExtException 读取失败或格式不受支持
     */
    public static BufferedImage read(String pathOrUrl) {
        if (!pathOrUrl.startsWith("http://") && !pathOrUrl.startsWith("https://")) {
            return read(new File(pathOrUrl));
        }
        try {
            return read(URI.create(pathOrUrl).toURL());
        } catch (IOException e) {
            throw new ExtException(EmojiSymbol.TOOL, e);
        }
    }

    /**
     * 写出图片
     *
     * @param image      图片
     * @param formatName 格式名，如 {@code png}
     * @param output     输出流，调用方负责关闭
     *
     * @throws ExtException 格式不支持或写出失败
     */
    public static void write(RenderedImage image, String formatName, OutputStream output) {
        try {
            requireWritten(ImageIO.write(image, formatName, output), formatName);
        } catch (IOException e) {
            throw new ExtException(EmojiSymbol.TOOL, e);
        }
    }

    /**
     * 写出图片到文件
     *
     * @param image      图片
     * @param formatName 格式名，如 {@code png}
     * @param output     目标文件
     *
     * @throws ExtException 格式不支持或写出失败
     */
    public static void write(RenderedImage image, String formatName, File output) {
        try {
            requireWritten(ImageIO.write(image, formatName, output), formatName);
        } catch (IOException e) {
            throw new ExtException(EmojiSymbol.TOOL, e);
        }
    }

    /**
     * 写出为字节数组
     *
     * @param image      图片
     * @param formatName 格式名
     *
     * @return 图片字节
     *
     * @throws ExtException 格式不支持或写出失败
     */
    public static byte[] writeAsBytes(RenderedImage image, String formatName) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        write(image, formatName, output);
        return output.toByteArray();
    }

    /**
     * 当前运行时是否可读取该格式
     *
     * @param formatName 格式名，不区分大小写
     *
     * @return 存在对应 reader 时返回 true
     */
    public static boolean isReadable(@Nullable String formatName) {
        return formatName != null && Set.of(ImageIO.getReaderFormatNames()).contains(normalize(formatName));
    }

    /**
     * 当前运行时是否可写出该格式
     *
     * @param formatName 格式名，不区分大小写
     *
     * @return 存在对应 writer 时返回 true
     */
    public static boolean isWritable(@Nullable String formatName) {
        return formatName != null && Set.of(ImageIO.getWriterFormatNames()).contains(normalize(formatName));
    }

    /**
     * 当前运行时支持读取的格式
     */
    public static Set<String> readableFormats() {
        return formats(ImageIO.getReaderFormatNames());
    }

    /**
     * 当前运行时支持写出的格式
     */
    public static Set<String> writableFormats() {
        return formats(ImageIO.getWriterFormatNames());
    }

    /**
     * ImageIO 有多个大小写别名，统一成小写去重
     */
    private static Set<String> formats(String[] names) {
        Set<String> formats = new LinkedHashSet<>();
        for (String name : names) {
            formats.add(normalize(name));
        }
        return formats;
    }

    private static String normalize(String formatName) {
        return formatName.toLowerCase(Locale.ROOT);
    }

    /**
     * ImageIO 认不出格式时返回 null 而不是抛异常，透传会让调用方空指针
     */
    private static BufferedImage requireImage(@Nullable BufferedImage image, @Nullable String source) {
        if (image != null) {
            return image;
        }
        throw new ExtException(EmojiSymbol.TOOL, "无法识别的图片格式（缺少对应的 ImageIO 插件？）：{}，当前可读格式：{}",
                source == null ? "<输入流>" : source, readableFormats());
    }

    /**
     * 找不到 writer 时 ImageIO 只返回 false、什么都不写，静默丢数据比抛异常危险
     */
    private static void requireWritten(boolean written, String formatName) {
        if (!written) {
            throw new ExtException(EmojiSymbol.TOOL, "不支持的图片格式：{}，当前可写格式：{}",
                    formatName, writableFormats());
        }
    }

}
