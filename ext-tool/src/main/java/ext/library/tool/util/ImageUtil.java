package ext.library.tool.util;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageOutputStream;

import ext.library.tool.core.Exceptions;
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
import lombok.experimental.UtilityClass;

/**
 * image 工具
 */
@UtilityClass
public class ImageUtil {

    /**
     * 读取图片
     *
     * @param input 图片文件
     * @return BufferedImage
     */
    public BufferedImage read(File input) {
        try {
            return ImageIO.read(input);
        } catch (IOException e) {
            throw Exceptions.unchecked(e);
        }
    }

    /**
     * 读取图片
     *
     * @param input 图片文件流
     * @return BufferedImage
     */
    public BufferedImage read(InputStream input) {
        try {
            return ImageIO.read(input);
        } catch (IOException e) {
            throw Exceptions.unchecked(e);
        }
    }

    /**
     * 读取图片，http 或者 file 地址
     *
     * @param url 图片链接地址
     * @return BufferedImage
     */
    public BufferedImage read(String url) {
        return url.startsWith("http://") || url.startsWith("https://") ? readUrl(url) : read(new File(url));
    }

    /**
     * 读取图片
     *
     * @param url 图片链接地址
     * @return BufferedImage
     */
    private BufferedImage readUrl(String url) {
        try {
            return ImageIO.read(URI.create(url).toURL());
        } catch (IOException e) {
            throw Exceptions.unchecked(e);
        }
    }

    /**
     * 读取图片
     *
     * @param url 图片链接地址
     * @return BufferedImage
     */
    public BufferedImage read(URL url) {
        try {
            return ImageIO.read(url);
        } catch (IOException e) {
            throw Exceptions.unchecked(e);
        }
    }

    /**
     * 写出图片
     *
     * @param im         RenderedImage to be written.
     * @param formatName a String containing the informal name of the format.
     * @param output     an ImageOutputStream to be written to.
     * @return false if no appropriate writer is found.
     */
    public boolean write(RenderedImage im, String formatName, ImageOutputStream output) {
        try {
            return ImageIO.write(im, formatName, output);
        } catch (IOException e) {
            throw Exceptions.unchecked(e);
        }
    }

    /**
     * 写出图片
     *
     * @param im         RenderedImage to be written.
     * @param formatName a String containing the informal name of the format.
     * @param output     an ImageOutputStream to be written to.
     * @return false if no appropriate writer is found.
     */
    public boolean write(RenderedImage im, String formatName, File output) {
        try {
            return ImageIO.write(im, formatName, output);
        } catch (IOException e) {
            throw Exceptions.unchecked(e);
        }
    }

    /**
     * 写出图片
     *
     * @param im         RenderedImage to be written.
     * @param formatName a String containing the informal name of the format.
     * @param output     an ImageOutputStream to be written to.
     * @return false if no appropriate writer is found.
     */
    public boolean write(RenderedImage im, String formatName, OutputStream output) {
        try {
            return ImageIO.write(im, formatName, output);
        } catch (IOException e) {
            throw Exceptions.unchecked(e);
        }
    }

    /**
     * 写出图片为 byte 数组
     *
     * @param im         RenderedImage to be written.
     * @param formatName a String containing the informal name of the format.
     * @return byte array.
     */
    public byte[] writeAsBytes(RenderedImage im, String formatName) {
        try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            if (ImageIO.write(im, formatName, output)) {
                return output.toByteArray();
            }
            throw new IllegalArgumentException("ImageWriter formatName " + formatName + " writer is null.");
        } catch (IOException e) {
            throw Exceptions.unchecked(e);
        }
    }

    /**
     * 写出图片为 InputStream
     *
     * @param im         RenderedImage to be written.
     * @param formatName a String containing the informal name of the format.
     * @return byte array input stream.
     */
    public ByteArrayInputStream writeAsStream(RenderedImage im, String formatName) {
        return new ByteArrayInputStream(writeAsBytes(im, formatName));
    }

}
