package ext.library.tool.util;

import ext.library.tool.constant.EmojiSymbol;
import ext.library.tool.exception.ToolException;
import org.jspecify.annotations.Nullable;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.Flushable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.channels.Channels;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Collection;

/**
 * IO 操作工具类
 * <p>
 * 提供流读写、文件操作、资源关闭等常用 IO 功能的封装。
 *
 * @since 2025.01.01
 */
public final class IOUtil {

    /**
     * 默认缓冲区大小：4KB
     */
    private static final int BUFFER_SIZE = 4096;

    private IOUtil() {
        // 防止实例化
    }

    // region 资源关闭

    /**
     * 静默关闭资源（忽略异常）
     * <p>
     * 如果资源实现了 {@link Flushable}，会先尝试刷新缓冲区
     *
     * @param closeable 待关闭的资源，可以为 null
     */
    public static void closeQuietly(@Nullable Closeable closeable) {
        if (closeable == null) {
            return;
        }
        if (closeable instanceof Flushable flushable) {
            try {
                flushable.flush();
            } catch (IOException ignored) {
                // ignore
            }
        }
        try {
            closeable.close();
        } catch (IOException ignored) {
            // ignore
        }
    }

    // endregion

    // region 读取为字符串

    /**
     * 从输入流读取字符串（UTF-8 编码）
     *
     * @param input 输入流
     * @return 读取的字符串
     * @throws ToolException 如果读取失败
     */
    public static String readToString(InputStream input) {
        return readToString(input, StandardCharsets.UTF_8);
    }

    /**
     * 从输入流读取字符串
     *
     * @param input   输入流
     * @param charset 字符编码
     * @return 读取的字符串
     * @throws ToolException 如果读取失败
     */
    public static String readToString(InputStream input, Charset charset) {
        try {
            return new String(input.readAllBytes(), charset);
        } catch (IOException e) {
            throw new ToolException(EmojiSymbol.TOOL, e);
        }
    }

    /**
     * 读取文件内容为字符串（UTF-8 编码）
     *
     * @param file 文件
     * @return 文件内容
     * @throws ToolException 如果读取失败
     */
    public static String readToString(File file) {
        try {
            return Files.readString(file.toPath());
        } catch (IOException e) {
            throw new ToolException(EmojiSymbol.TOOL, e);
        }
    }

    /**
     * 读取文件内容为字符串
     *
     * @param file    文件
     * @param charset 字符编码
     * @return 文件内容
     * @throws ToolException 如果读取失败
     */
    public static String readToString(File file, Charset charset) {
        try {
            return Files.readString(file.toPath(), charset);
        } catch (IOException e) {
            throw new ToolException(EmojiSymbol.TOOL, e);
        }
    }

    // endregion

    // region 读取为字节数组

    /**
     * 从输入流读取字节数组
     *
     * @param input 输入流
     * @return 字节数组
     * @throws ToolException 如果读取失败
     */
    public static byte[] readToByteArray(InputStream input) {
        try {
            return input.readAllBytes();
        } catch (IOException e) {
            throw new ToolException(EmojiSymbol.TOOL, e);
        }
    }

    /**
     * 读取文件内容为字节数组
     *
     * @param file 文件
     * @return 字节数组
     * @throws ToolException 如果读取失败
     */
    public static byte[] readToByteArray(File file) {
        try {
            return Files.readAllBytes(file.toPath());
        } catch (IOException e) {
            throw new ToolException(EmojiSymbol.TOOL, e);
        }
    }

    // endregion

    // region 临时目录

    /**
     * 获取系统临时目录
     *
     * @return 临时目录 File 对象
     */
    public static File getTempDir() {
        return new File(System.getProperty("java.io.tmpdir"));
    }

    /**
     * 在临时目录下创建子路径
     * <p>
     * 如果父目录不存在，会自动创建
     *
     * @param subPath 子路径（相对于临时目录）
     * @return 完整路径的 File 对象
     */
    public static File toTempDir(String subPath) {
        File tempDir = getTempDir();
        File fullPath = new File(tempDir, subPath);
        File parentDir = fullPath.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }
        return fullPath;
    }

    /**
     * 获取临时目录下子路径的绝对路径字符串
     *
     * @param subPath 子路径（相对于临时目录）
     * @return 绝对路径字符串
     */
    public static String toTempDirPath(String subPath) {
        return toTempDir(subPath).getAbsolutePath();
    }

    // endregion

    // region 流复制

    /**
     * 从输入流复制到输出流
     * <p>
     * 完成后会关闭两个流
     *
     * @param in  输入流
     * @param out 输出流
     * @return 复制的字节数
     * @throws IOException 如果发生 IO 错误
     */
    public static long copy(InputStream in, OutputStream out) throws IOException {
        try (in; out) {
            long byteCount = 0;
            byte[] buffer = new byte[BUFFER_SIZE];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
                byteCount += bytesRead;
            }
            out.flush();
            return byteCount;
        }
    }

    /**
     * 将字节数组写入输出流
     * <p>
     * 完成后会关闭输出流
     *
     * @param in  字节数组
     * @param out 输出流
     * @throws IOException 如果发生 IO 错误
     */
    public static void copy(byte[] in, OutputStream out) throws IOException {
        try (out) {
            out.write(in);
        }
    }

    /**
     * 从输入流复制到字节数组
     * <p>
     * 完成后会关闭输入流
     *
     * @param in 输入流
     * @return 字节数组
     * @throws IOException 如果发生 IO 错误
     */
    public static byte[] copyToBytes(InputStream in) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream(BUFFER_SIZE);
        copy(in, out);
        return out.toByteArray();
    }

    /**
     * 从输入流复制到字符串
     * <p>
     * 完成后会关闭输入流
     *
     * @param in      输入流
     * @param charset 字符编码
     * @return 字符串
     * @throws IOException 如果发生 IO 错误
     */
    public static String copyToString(InputStream in, Charset charset) throws IOException {
        StringBuilder out = new StringBuilder(BUFFER_SIZE);
        try (in; InputStreamReader reader = new InputStreamReader(in, charset)) {
            char[] buffer = new char[BUFFER_SIZE];
            int charsRead;
            while ((charsRead = reader.read(buffer)) != -1) {
                out.append(buffer, 0, charsRead);
            }
        }
        return out.toString();
    }

    // endregion

    // region 写入

    /**
     * 将字符串写入输出流（UTF-8 编码）
     *
     * @param data   字符串数据
     * @param output 输出流
     * @throws IOException 如果发生 IO 错误
     */
    public static void writeString(String data, OutputStream output) throws IOException {
        writeString(data, output, StandardCharsets.UTF_8);
    }

    /**
     * 将字符串写入输出流
     * <p>
     * 使用 {@link Charset#encode(String)} 避免大字符串转换时的内存问题
     *
     * @param data    字符串数据
     * @param output  输出流
     * @param charset 字符编码
     * @throws IOException 如果发生 IO 错误
     */
    public static void writeString(String data, OutputStream output, Charset charset) throws IOException {
        Channels.newChannel(output).write(charset.encode(data));
    }

    /**
     * 将集合按行写入输出流（UTF-8 编码，系统默认换行符）
     *
     * @param lines  行集合
     * @param output 输出流
     * @throws IOException 如果发生 IO 错误
     */
    public static void writeLines(Collection<?> lines, OutputStream output) throws IOException {
        writeLines(lines, null, output, null);
    }

    /**
     * 将集合按行写入输出流
     *
     * @param lines      行集合
     * @param lineEnding 行结束符（null 使用系统默认）
     * @param output     输出流
     * @param charset    字符编码（null 使用 UTF-8）
     * @throws IOException 如果发生 IO 错误
     */
    public static void writeLines(Collection<?> lines, @Nullable String lineEnding,
                                  OutputStream output, @Nullable Charset charset) throws IOException {
        if (lineEnding == null) {
            lineEnding = System.lineSeparator();
        }
        if (StandardCharsets.UTF_16.equals(charset)) {
            // 避免写入 BOM
            charset = StandardCharsets.UTF_16BE;
        }
        if (charset == null) {
            charset = StandardCharsets.UTF_8;
        }

        byte[] eolBytes = lineEnding.getBytes(charset);
        for (Object line : lines) {
            writeString(line.toString(), output, charset);
            output.write(eolBytes);
        }
    }

    // endregion

}
