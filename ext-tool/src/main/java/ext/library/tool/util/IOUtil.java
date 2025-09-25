package ext.library.tool.util;

import com.google.common.io.ByteStreams;
import com.google.common.io.Files;
import ext.library.tool.core.Exceptions;
import org.springframework.util.Assert;

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
import java.util.Collection;

public class IOUtil {
    /**
     * The default buffer size used when copying bytes.
     */
    private static final int BUFFER_SIZE = 1024 * 4;

    /**
     * 关闭 Closeable
     *
     * @param closeable 自动关闭
     */
    public static void closeQuietly(Closeable closeable) {
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

    /**
     * InputStream to String utf-8
     *
     * @param input the <code>InputStream</code> to read from
     *
     * @return the requested String
     *
     * @throws NullPointerException if the input is null
     */
    public static String readToString(InputStream input) {
        try (input) {
            return new String(ByteStreams.toByteArray(input));
        } catch (IOException e) {
            throw Exceptions.unchecked(e);
        }
    }

    /**
     * InputStream to String
     *
     * @param input   the <code>InputStream</code> to read from
     * @param charset the <code>Charset</code>
     *
     * @return the requested String
     *
     * @throws NullPointerException if the input is null
     */
    public static String readToString(InputStream input, Charset charset) {
        try (input) {
            return new String(ByteStreams.toByteArray(input), charset);
        } catch (IOException e) {
            throw Exceptions.unchecked(e);
        }
    }

    /**
     * InputStream to bytes 数组
     *
     * @param input InputStream
     *
     * @return the requested byte array
     */
    public static byte[] readToByteArray(InputStream input) {
        try (input) {
            return ByteStreams.toByteArray(input);
        } catch (IOException e) {
            throw Exceptions.unchecked(e);
        }
    }

    /**
     * 读取文件为字符串
     *
     * @param file the file to read, must not be {@code null}
     *
     * @return the file contents, never {@code null}
     */
    public static String readToString(final File file) {
        try {
            return new String(Files.toByteArray(file), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw Exceptions.unchecked(e);
        }
    }

    /**
     * 读取文件为字符串
     *
     * @param file     the file to read, must not be {@code null}
     * @param encoding the encoding to use, {@code null} means platform default
     *
     * @return the file contents, never {@code null}
     */
    public static String readToString(File file, Charset encoding) {
        try {
            return new String(Files.toByteArray(file), encoding);
        } catch (IOException e) {
            throw Exceptions.unchecked(e);
        }
    }

    /**
     * 读取文件为 byte 数组
     *
     * @param file the file to read, must not be {@code null}
     *
     * @return the file contents, never {@code null}
     */
    public static byte[] readToByteArray(File file) {
        try {
            return Files.toByteArray(file);
        } catch (IOException e) {
            throw Exceptions.unchecked(e);
        }
    }

    /**
     * 拼接临时文件目录。
     *
     * @return 临时文件目录。
     */
    public static String toTempDirPath(String subDirFile) {
        return toTempDir(subDirFile).getAbsolutePath();
    }

    /**
     * Returns a {@link File} representing the system temporary directory.
     *
     * @return the system temporary directory.
     */
    public static File getTempDir() {
        return new File(System.getProperty("java.io.tmpdir"));
    }

    /**
     * 拼接临时文件目录。
     *
     * @return 临时文件目录。
     */
    public static File toTempDir(String subDirFile) {
        String tempDirPath = System.getProperty("java.io.tmpdir");
        if (subDirFile.startsWith("/")) {
            subDirFile = subDirFile.substring(1);
        }
        String fullPath = tempDirPath.concat(subDirFile);
        File fullFilePath = new File(fullPath);
        File dir = fullFilePath.getParentFile();
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return fullFilePath;
    }

    /**
     * Copy from InputStream to OutputStream, Closes both streams when done.
     *
     * @param in  InputStream
     * @param out OutputStream
     *
     * @return the number of bytes copied
     *
     * @throws IOException in case of I/O errors
     */
    public static int copy(InputStream in, OutputStream out) throws IOException {
        Assert.notNull(in, "No InputStream specified");
        Assert.notNull(out, "No OutputStream specified");
        try {
            int byteCount = 0;
            byte[] buffer = new byte[BUFFER_SIZE];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
                byteCount += bytesRead;
            }
            out.flush();
            return byteCount;
        } finally {
            close(in);
            close(out);
        }
    }

    /**
     * Copy from byte array to OutputStream, Closes the stream when done.
     *
     * @param in  the byte array
     * @param out OutputStream
     *
     * @throws IOException in case of I/O errors
     */
    public static void copy(byte[] in, OutputStream out) throws IOException {
        Assert.notNull(in, "No input byte array specified");
        Assert.notNull(out, "No OutputStream specified");
        try {
            out.write(in);
        } finally {
            close(out);
        }
    }

    /**
     * Copy the contents of the given InputStream into a new byte array.
     * Closes the stream when done.
     *
     * @param in the stream to copy from (may be {@code null} or empty)
     *
     * @return the new byte array that has been copied to (possibly empty)
     *
     * @throws IOException in case of I/O errors
     */
    public static byte[] copyToBytes(InputStream in) throws IOException {
        if (in == null) {
            return new byte[0];
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream(BUFFER_SIZE);
        copy(in, out);
        return out.toByteArray();
    }

    /**
     * Copy the contents of the given InputStream into a String.
     *
     * @param in      the stream to copy from (may be {@code null} or empty)
     * @param charset the Charset
     *
     * @return the requested String
     *
     */
    public static String copyToStr(InputStream in, Charset charset) throws IOException {
        if (in == null) {
            return "";
        }

        StringBuilder out = new StringBuilder(BUFFER_SIZE);
        InputStreamReader reader = null;
        try {
            reader = new InputStreamReader(in, charset);
            char[] buffer = new char[BUFFER_SIZE];
            int charsRead;
            while ((charsRead = reader.read(buffer)) != -1) {
                out.append(buffer, 0, charsRead);
            }
        } finally {
            close(in);
            close(reader);
        }
        return out.toString();
    }


    /*
     * Write the given collection to the given output as lines.
     */
    public static void writeLines(final Collection<?> lines, final OutputStream output) throws IOException {
        writeLines(lines, null, output, null);
    }


    /**
     * Write the given collection to the given output as lines.
     *
     * @param lines      the lines to write
     * @param lineEnding the line separator to use (defaults to system default)
     * @param output     the {@link OutputStream} to write to
     * @param charset    the charset to use to convert lines to bytes (defaults to UTF-8)
     *
     * @throws IOException in case of I/O errors
     */
    public static void writeLines(final Collection<?> lines, String lineEnding, final OutputStream output, Charset charset) throws IOException {
        if (lines == null) {
            return;
        }
        if (lineEnding == null) {
            lineEnding = System.lineSeparator();
        }
        if (StandardCharsets.UTF_16.equals(charset)) {
            // don't write a BOM
            charset = StandardCharsets.UTF_16BE;
        }
        if (charset == null) {
            charset = StandardCharsets.UTF_8;
        }

        final byte[] eolBytes = lineEnding.getBytes(charset);
        for (final Object line : lines) {
            if (line != null) {
                writeStr(line.toString(), output, charset);
            }
            output.write(eolBytes);
        }
    }


    /**
     * Write the given string to the given output as a byte array using the UTF-8 charset.
     */
    public static void writeStr(final String data, final OutputStream output) throws IOException {
        writeStr(data, output, StandardCharsets.UTF_8);
    }

    /**
     * Write the given string to the given output as a byte array using the given charset.
     *
     * @param data    the string to write
     * @param output  the {@link OutputStream} to write to
     * @param charset the charset to use to convert lines to bytes (defaults to UTF-8)
     *
     * @throws IOException in case of I/O errors
     */
    public static void writeStr(final String data, final OutputStream output, Charset charset) throws IOException {
        if (data == null) {
            return;
        }
        if (charset == null) {
            charset = StandardCharsets.UTF_8;
        }

        // Use Charset#encode(String), since calling String#getBytes(Charset) might result in
        // NegativeArraySizeException or OutOfMemoryError.
        // The underlying OutputStream should not be closed, so the channel is not closed.
        Channels.newChannel(output).write(charset.encode(data));
    }

    /**
     * Attempt to close the supplied {@link Closeable}, ignore exceptions
     *
     * @param closeable the {@code Closeable} to close
     */
    private static void close(Closeable closeable) {
        try {
            closeable.close();
        } catch (IOException ignore) {
        }
    }

}