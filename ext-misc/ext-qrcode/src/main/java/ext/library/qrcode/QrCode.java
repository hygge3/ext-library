package ext.library.qrcode;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.DecodeHintType;
import com.google.zxing.EncodeHintType;
import com.google.zxing.FormatException;
import com.google.zxing.LuminanceSource;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import ext.library.tool.constant.EmojiSymbol;
import ext.library.tool.exception.ExtException;
import ext.library.tool.util.ImageUtil;
import org.jspecify.annotations.Nullable;

import java.awt.*;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.EnumMap;
import java.util.Map;

/**
 * 二维码生成与识别工具
 * <p>
 * 基于 Google ZXing 库，提供简洁的链式 API 进行二维码生成和识别。
 * </p>
 *
 * <h3>生成示例：</h3>
 * <pre>{@code
 * // 基本用法
 * QrCode.from("https://example.com").toFile("qrcode.png");
 *
 * // 完整配置
 * QrCode.from("内容")
 *     .size(512)
 *     .foreGroundColor(Color.BLACK)
 *     .backGroundColor(Color.WHITE)
 *     .logo("/path/to/logo.png")
 *     .toFile("output.png");
 * }</pre>
 *
 * <h3>识别示例：</h3>
 * <pre>{@code
 * String content = QrCode.read(new File("qrcode.png"));
 * }</pre>
 */
public class QrCode {

    /**
     * 二维码内容
     */
    private final String content;

    /**
     * 提供给编码器的额外参数
     */
    private final Map<EncodeHintType, Object> hints;

    /**
     * 图片大小（像素）
     */
    private int size;

    /**
     * 内容编码格式
     */
    private Charset encode;

    /**
     * 错误修正等级
     */
    private ErrorCorrectionLevel errorCorrectionLevel;

    /**
     * 错误修正等级的具体值（用于计算 Logo 最大尺寸）
     */
    private double errorCorrectionLevelValue;

    /**
     * 前景色
     */
    private Color foreGroundColor;

    /**
     * 背景色
     */
    private Color backGroundColor;

    /**
     * 图片的文件格式
     */
    private String imageFormat;

    /**
     * 是否删除图片的外白边
     */
    private boolean deleteMargin;

    /**
     * Logo 图片
     */
    private @Nullable BufferedImage logo;

    /**
     * 创建一个带有默认值的 QRCode 生成器。
     * <p>默认值：</p>
     * <ul>
     * <li>图片大小：512px</li>
     * <li>内容编码格式：UTF-8</li>
     * <li>错误修正等级：Level M（15% 内容可修正）</li>
     * <li>前景色：黑色</li>
     * <li>背景色：白色</li>
     * <li>输出图片格式：png</li>
     * <li>删除白边：true</li>
     * </ul>
     *
     * @param content 二维码内容
     */
    private QrCode(String content) {
        if (content.isBlank()) {
            throw new IllegalArgumentException("二维码内容不能为空");
        }
        this.content = content;
        this.size = 512;
        this.encode = StandardCharsets.UTF_8;
        this.errorCorrectionLevel = ErrorCorrectionLevel.M;
        this.errorCorrectionLevelValue = 0.15;
        this.foreGroundColor = Color.BLACK;
        this.backGroundColor = Color.WHITE;
        this.imageFormat = "png";
        this.deleteMargin = true;
        this.hints = new EnumMap<>(EncodeHintType.class);
    }

    // ==================== 静态工厂方法 ====================

    /**
     * 创建二维码生成器
     *
     * @param content 二维码内容
     *
     * @return QrCode 实例
     */
    public static QrCode from(String content) {
        return new QrCode(content);
    }

    // ==================== 读取二维码 ====================

    /**
     * 从文件路径读取二维码内容
     *
     * @param qrCodeFile 二维码图片文件路径
     *
     * @return 二维码内容
     */
    public static String read(String qrCodeFile) {
        return read(ImageUtil.read(qrCodeFile));
    }

    /**
     * 从文件读取二维码内容
     *
     * @param qrCodeFile 二维码图片文件
     *
     * @return 二维码内容
     */
    public static String read(File qrCodeFile) {
        return read(ImageUtil.read(qrCodeFile));
    }

    /**
     * 从 URL 读取二维码内容
     *
     * @param qrCodeUrl 二维码图片 URL
     *
     * @return 二维码内容
     */
    public static String read(URL qrCodeUrl) {
        return read(ImageUtil.read(qrCodeUrl));
    }

    /**
     * 从输入流读取二维码内容
     *
     * @param inputStream 二维码图片输入流
     *
     * @return 二维码内容
     */
    public static String read(InputStream inputStream) {
        return read(ImageUtil.read(inputStream));
    }

    /**
     * 从图像对象读取二维码内容
     *
     * @param qrCodeImage 二维码图像对象
     *
     * @return 二维码内容
     */
    public static String read(BufferedImage qrCodeImage) {
        return read(qrCodeImage, (Map<DecodeHintType, ?>) null);
    }

    /**
     * 从文件路径读取二维码内容（指定编码）
     *
     * @param qrCodeFile 二维码图片文件路径
     * @param charset    字符编码
     *
     * @return 二维码内容
     */
    public static String read(String qrCodeFile, Charset charset) {
        return read(ImageUtil.read(qrCodeFile), charset);
    }

    /**
     * 从文件读取二维码内容（指定编码）
     *
     * @param qrCodeFile 二维码图片文件
     * @param charset    字符编码
     *
     * @return 二维码内容
     */
    public static String read(File qrCodeFile, Charset charset) {
        return read(ImageUtil.read(qrCodeFile), charset);
    }

    /**
     * 从 URL 读取二维码内容（指定编码）
     *
     * @param qrCodeUrl 二维码图片 URL
     * @param charset   字符编码
     *
     * @return 二维码内容
     */
    public static String read(URL qrCodeUrl, Charset charset) {
        return read(ImageUtil.read(qrCodeUrl), charset);
    }

    /**
     * 从输入流读取二维码内容（指定编码）
     *
     * @param inputStream 二维码图片输入流
     * @param charset     字符编码
     *
     * @return 二维码内容
     */
    public static String read(InputStream inputStream, Charset charset) {
        return read(ImageUtil.read(inputStream), charset);
    }

    /**
     * 从图像对象读取二维码内容（指定编码）
     *
     * @param qrCodeImage 二维码图像对象
     * @param charset     字符编码
     *
     * @return 二维码内容
     */
    public static String read(BufferedImage qrCodeImage, Charset charset) {
        Map<DecodeHintType, Object> hints = new EnumMap<>(DecodeHintType.class);
        hints.put(DecodeHintType.CHARACTER_SET, charset);
        return read(qrCodeImage, hints);
    }

    /**
     * 从图像对象读取二维码内容（自定义 hints）
     *
     * @param qrCodeImage 二维码图像对象
     * @param hints       解码提示参数
     *
     * @return 二维码内容
     */
    public static String read(BufferedImage qrCodeImage, @Nullable Map<DecodeHintType, ?> hints) {
        LuminanceSource source = new BufferedImageLuminanceSource(qrCodeImage);
        BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
        try {
            Result result = new QRCodeReader().decode(bitmap, hints);
            return result.getText();
        } catch (NotFoundException | ChecksumException | FormatException e) {
            throw new ExtException(EmojiSymbol.QRCODE, e);
        } finally {
            qrCodeImage.getGraphics().dispose();
        }
    }

    /**
     * 从文件路径读取二维码原始字节
     *
     * @param qrCodeFile 二维码图片文件路径
     *
     * @return 原始字节数组
     */
    public static byte[] readRawBytes(String qrCodeFile) {
        return readRawBytes(ImageUtil.read(qrCodeFile));
    }

    /**
     * 从文件读取二维码原始字节
     *
     * @param qrCodeFile 二维码图片文件
     *
     * @return 原始字节数组
     */
    public static byte[] readRawBytes(File qrCodeFile) {
        return readRawBytes(ImageUtil.read(qrCodeFile));
    }

    /**
     * 从 URL 读取二维码原始字节
     *
     * @param qrCodeUrl 二维码图片 URL
     *
     * @return 原始字节数组
     */
    public static byte[] readRawBytes(URL qrCodeUrl) {
        return readRawBytes(ImageUtil.read(qrCodeUrl));
    }

    /**
     * 从输入流读取二维码原始字节
     *
     * @param inputStream 二维码图片输入流
     *
     * @return 原始字节数组
     */
    public static byte[] readRawBytes(InputStream inputStream) {
        return readRawBytes(ImageUtil.read(inputStream));
    }

    /**
     * 从图像对象读取二维码原始字节
     *
     * @param qrCodeImage 二维码图像对象
     *
     * @return 原始字节数组
     */
    public static byte[] readRawBytes(BufferedImage qrCodeImage) {
        LuminanceSource source = new BufferedImageLuminanceSource(qrCodeImage);
        BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
        try {
            Result result = new QRCodeReader().decode(bitmap);
            return result.getRawBytes();
        } catch (NotFoundException | ChecksumException | FormatException e) {
            throw new ExtException(EmojiSymbol.QRCODE, e);
        } finally {
            qrCodeImage.getGraphics().dispose();
        }
    }

    // ==================== 链式配置方法 ====================

    /**
     * 解析十六进制颜色字符串
     */
    private static Color parseColor(String hexString, Color defaultColor) {
        try {
            String normalized = hexString.startsWith("#") ? hexString : "#" + hexString;
            return new Color(Long.decode(normalized).intValue());
        } catch (NumberFormatException e) {
            return defaultColor;
        }
    }

    /**
     * 删除二维码白边
     */
    private static BitMatrix deleteWhiteMargin(BitMatrix matrix) {
        int[] rec = matrix.getEnclosingRectangle();
        int resWidth = rec[2] + 1;
        int resHeight = rec[3] + 1;
        BitMatrix resMatrix = new BitMatrix(resWidth, resHeight);
        resMatrix.clear();
        for (int i = 0; i < resWidth; i++) {
            for (int j = 0; j < resHeight; j++) {
                if (matrix.get(i + rec[0], j + rec[1])) {
                    resMatrix.set(i, j);
                }
            }
        }
        return resMatrix;
    }

    /**
     * 设置图片大小（像素）
     *
     * @param size 图片大小（必须大于 0）
     *
     * @return this
     */
    public QrCode size(int size) {
        if (size <= 0) {
            throw new IllegalArgumentException("图片大小必须大于 0");
        }
        this.size = size;
        return this;
    }

    /**
     * 设置内容编码格式
     *
     * @param charset 编码格式
     *
     * @return this
     */
    public QrCode encode(Charset charset) {
        this.encode = charset;
        return this;
    }

    /**
     * 设置错误修正等级
     * <ul>
     * <li>L: 7% 内容可修正</li>
     * <li>M: 15% 内容可修正（默认）</li>
     * <li>Q: 25% 内容可修正</li>
     * <li>H: 30% 内容可修正</li>
     * </ul>
     *
     * @param level 错误修正等级
     *
     * @return this
     */
    public QrCode errorCorrectionLevel(ErrorCorrectionLevel level) {
        this.errorCorrectionLevel = level;
        this.errorCorrectionLevelValue = switch (level) {
            case L -> 0.07;
            case M -> 0.15;
            case Q -> 0.25;
            case H -> 0.30;
        };
        return this;
    }

    /**
     * 设置前景色（十六进制字符串）
     *
     * @param hexColor 十六进制颜色值（如 "#000000" 或 "000000"）
     *
     * @return this
     */
    public QrCode foreGroundColor(String hexColor) {
        this.foreGroundColor = parseColor(hexColor, Color.BLACK);
        return this;
    }

    /**
     * 设置前景色
     *
     * @param color 颜色
     *
     * @return this
     */
    public QrCode foreGroundColor(Color color) {
        this.foreGroundColor = color;
        return this;
    }

    /**
     * 设置背景色（十六进制字符串）
     *
     * @param hexColor 十六进制颜色值（如 "#FFFFFF" 或 "FFFFFF"）
     *
     * @return this
     */
    public QrCode backGroundColor(String hexColor) {
        this.backGroundColor = parseColor(hexColor, Color.WHITE);
        return this;
    }

    /**
     * 设置背景色
     *
     * @param color 颜色
     *
     * @return this
     */
    public QrCode backGroundColor(Color color) {
        this.backGroundColor = color;
        return this;
    }

    /**
     * 设置输出图片格式
     *
     * @param format 图片格式（如 "png", "jpg"）
     *
     * @return this
     */
    public QrCode imageFormat(String format) {
        this.imageFormat = format.toLowerCase();
        return this;
    }

    /**
     * 设置是否删除白边
     *
     * @param deleteMargin true 删除，false 保留
     *
     * @return this
     */
    public QrCode deleteMargin(boolean deleteMargin) {
        this.deleteMargin = deleteMargin;
        return this;
    }

    /**
     * 设置 Logo 图片
     *
     * @param logo Logo 图像
     *
     * @return this
     */
    public QrCode logo(BufferedImage logo) {
        this.logo = logo;
        return this;
    }

    /**
     * 设置 Logo 图片（从文件）
     *
     * @param logoFile Logo 文件
     *
     * @return this
     */
    public QrCode logo(File logoFile) {
        return logo(ImageUtil.read(logoFile));
    }

    /**
     * 设置 Logo 图片（从 URL）
     *
     * @param logoUrl Logo URL
     *
     * @return this
     */
    public QrCode logo(URL logoUrl) {
        return logo(ImageUtil.read(logoUrl));
    }

    // ==================== 输出方法 ====================

    /**
     * 设置 Logo 图片（从文件路径）
     *
     * @param logoPath Logo 文件路径
     *
     * @return this
     */
    public QrCode logo(String logoPath) {
        return logo(ImageUtil.read(logoPath));
    }

    /**
     * 设置 Logo 图片（从输入流）
     *
     * @param logoStream Logo 输入流
     *
     * @return this
     */
    public QrCode logo(InputStream logoStream) {
        return logo(ImageUtil.read(logoStream));
    }

    /**
     * 输出到流
     *
     * @param output 输出流
     *
     * @return 是否成功
     */
    public boolean write(OutputStream output) {
        BufferedImage image = this.toImage();
        return ImageUtil.write(image, this.imageFormat, output);
    }

    /**
     * 输出到文件
     *
     * @param filePath 文件路径
     *
     * @return 文件对象
     */
    public File toFile(String filePath) {
        return toFile(new File(filePath));
    }

    /**
     * 输出到文件
     *
     * @param file 文件对象
     *
     * @return 文件对象
     */
    public File toFile(File file) {
        File parentDir = file.getParentFile();
        if (parentDir != null) {
            parentDir.mkdirs();
        }
        BufferedImage image = this.toImage();
        ImageUtil.write(image, this.imageFormat, file);
        return file;
    }

    /**
     * 输出为 Base64 Data URI
     *
     * @return Base64 Data URI 字符串
     */
    public String toBase64() {
        String mimeType = getMimeType();
        return "data:" + mimeType + ";base64," + Base64.getEncoder().encodeToString(toBytes());
    }

    /**
     * 输出为字节数组
     *
     * @return 字节数组
     */
    public byte[] toBytes() {
        BufferedImage image = this.toImage();
        return ImageUtil.writeAsBytes(image, this.imageFormat);
    }

    // ==================== 内部方法 ====================

    /**
     * 输出为输入流
     *
     * @return ByteArrayInputStream
     */
    public ByteArrayInputStream toStream() {
        BufferedImage image = this.toImage();
        return ImageUtil.writeAsStream(image, this.imageFormat);
    }

    /**
     * 输出为图像对象
     *
     * @return BufferedImage
     */
    public BufferedImage toImage() {
        BitMatrix matrix;
        try {
            matrix = new QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, this.size, this.size, this.getHints());
        } catch (WriterException e) {
            throw new ExtException(EmojiSymbol.QRCODE, e);
        }

        if (this.deleteMargin) {
            matrix = deleteWhiteMargin(matrix);
        }

        int width = matrix.getWidth();
        int height = matrix.getHeight();
        int fgColor = this.foreGroundColor.getRGB();
        int bgColor = this.backGroundColor.getRGB();

        BufferedImage image = new BufferedImage(width, height, ColorSpace.TYPE_RGB);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                image.setRGB(x, y, matrix.get(x, y) ? fgColor : bgColor);
            }
        }

        // 添加 Logo（如果有）
        if (this.logo != null) {
            addLogo(image, this.logo);
        }

        return image;
    }

    /**
     * 获取编码器参数
     */
    private Map<EncodeHintType, ?> getHints() {
        hints.clear();
        hints.put(EncodeHintType.ERROR_CORRECTION, this.errorCorrectionLevel);
        hints.put(EncodeHintType.CHARACTER_SET, this.encode);
        hints.put(EncodeHintType.MARGIN, 0);
        return hints;
    }

    /**
     * 获取 MIME 类型
     */
    private String getMimeType() {
        return switch (this.imageFormat) {
            case "jpg", "jpeg" -> "image/jpeg";
            case "gif" -> "image/gif";
            case "bmp" -> "image/bmp";
            case "webp" -> "image/webp";
            default -> "image/png";
        };
    }

    /**
     * 在二维码上添加 Logo
     */
    private void addLogo(BufferedImage qrCodeImage, BufferedImage logoImage) {
        int baseWidth = qrCodeImage.getWidth();
        int baseHeight = qrCodeImage.getHeight();

        // 计算 Logo 的最大边长（基于错误修正等级）
        int maxWidth = (int) Math.sqrt(baseWidth * baseHeight * this.errorCorrectionLevelValue * 0.4);
        int logoRectWidth = Math.min(maxWidth, logoImage.getWidth());
        int logoRectHeight = Math.min(maxWidth, logoImage.getHeight());

        // 创建带边框的 Logo 区域
        BufferedImage logoRect = new BufferedImage(logoRectWidth, logoRectHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = logoRect.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // 绘制白色背景
        g2.setColor(Color.WHITE);
        g2.fillRect(0, 0, logoRectWidth, logoRectHeight);
        g2.setComposite(AlphaComposite.SrcAtop);

        // 绘制灰色边框（2px）
        g2.setColor(Color.LIGHT_GRAY);
        g2.fillRect(2, 2, logoRectWidth - 4, logoRectHeight - 4);
        g2.setComposite(AlphaComposite.SrcAtop);

        // 绘制 Logo 图片
        g2.drawImage(logoImage, 4, 4, logoRectWidth - 8, logoRectHeight - 8, null);
        logoImage.getGraphics().dispose();
        g2.dispose();

        // 将 Logo 添加到二维码中央
        Graphics2D gc = (Graphics2D) qrCodeImage.getGraphics();
        gc.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        gc.setColor(this.backGroundColor);
        gc.drawImage(logoRect, (baseWidth - logoRectWidth) / 2, (baseHeight - logoRectHeight) / 2, null);
        gc.dispose();
    }
}
