package ext.library.oss;

import ext.library.oss.config.OssProperties;
import ext.library.tool.constant.EmojiSymbol;
import ext.library.tool.exception.ExtException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

/**
 * 对象存储门面
 * <p>
 * 业务方只用这一个类，具体落在本地文件系统还是 S3 由 {@link ObjectStorage} 实现决定。
 *
 * @since 4.1.0
 */
public class OssTemplate {

    private static final DateTimeFormatter DATE_DIR = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final ObjectStorage storage;

    private final OssProperties properties;

    /**
     * 构造模板
     *
     * @param storage    存储实现
     * @param properties 配置
     */
    public OssTemplate(ObjectStorage storage, OssProperties properties) {
        this.storage = storage;
        this.properties = properties;
    }

    /**
     * 写入对象
     *
     * @param key         对象键
     * @param content     内容流，由本方法关闭
     * @param size        字节数，小于等于 0 时实现自行决定如何读取
     * @param contentType 内容类型
     *
     * @return 对象键
     */
    public String put(String key, InputStream content, long size, String contentType) {
        try (content) {
            storage.put(key, content, size, contentType);
        } catch (IOException e) {
            throw new ExtException(EmojiSymbol.OSS, e, "写入对象失败：{}", key);
        }
        return key;
    }

    /**
     * 写入字节数组
     *
     * @param key         对象键
     * @param content     内容
     * @param contentType 内容类型
     *
     * @return 对象键
     */
    public String putBytes(String key, byte[] content, String contentType) {
        return put(key, new ByteArrayInputStream(content), content.length, contentType);
    }

    /**
     * 写入本地文件
     *
     * @param key         对象键
     * @param file        本地文件
     * @param contentType 内容类型
     *
     * @return 对象键
     */
    public String putFile(String key, Path file, String contentType) {
        if (!Files.exists(file)) {
            throw new ExtException(EmojiSymbol.OSS, "文件不存在：{}", file);
        }
        try (InputStream stream = Files.newInputStream(file)) {
            return put(key, stream, Files.size(file), contentType);
        } catch (IOException e) {
            throw new ExtException(EmojiSymbol.OSS, e, "写入对象失败：{}", key);
        }
    }

    /**
     * 读取对象
     *
     * @param key 对象键
     *
     * @return 对象，调用方负责关闭
     */
    public OssObject get(String key) {
        return storage.get(key);
    }

    /**
     * 读取对象为字符串
     *
     * @param key 对象键
     *
     * @return UTF-8 文本
     */
    public String getString(String key) {
        try (OssObject object = storage.get(key)) {
            return new String(object.content().readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException | UncheckedIOException e) {
            throw new ExtException(EmojiSymbol.OSS, e, "读取对象失败：{}", key);
        }
    }

    /**
     * 读取对象为字节数组
     *
     * @param key 对象键
     *
     * @return 内容
     */
    public byte[] getBytes(String key) {
        try (OssObject object = storage.get(key)) {
            return object.content().readAllBytes();
        } catch (IOException | UncheckedIOException e) {
            throw new ExtException(EmojiSymbol.OSS, e, "读取对象失败：{}", key);
        }
    }

    /**
     * 删除对象
     *
     * @param key 对象键
     *
     * @return 是否确实删除了一个对象
     */
    public boolean delete(String key) {
        return storage.delete(key);
    }

    /**
     * 判断对象是否存在
     *
     * @param key 对象键
     *
     * @return 存在返回 {@code true}
     */
    public boolean exists(String key) {
        return storage.exists(key);
    }

    /**
     * 按前缀列举对象
     *
     * @param prefix 键前缀
     *
     * @return 按 key 升序排列的不可变列表
     */
    public List<ObjectItem> list(String prefix) {
        return storage.list(prefix);
    }

    /**
     * 用默认有效期生成下载直链
     *
     * @param key 对象键
     *
     * @return 直链
     */
    public String presign(String key) {
        return storage.presign(key, properties.getPresignExpiry());
    }

    /**
     * 生成下载直链
     *
     * @param key    对象键
     * @param expiry 有效期
     *
     * @return 直链
     */
    public String presign(String key, Duration expiry) {
        return storage.presign(key, expiry);
    }

    /**
     * 生成对象键
     * <p>
     * 形如 {@code directory/yyyyMMdd/uuid_文件名}，文件名去掉了路径分隔符等危险字符；
     * 目录为空时省略目录段，文件名无扩展名时补 {@code .bin}。
     *
     * @param directory 目录
     * @param filename  原始文件名
     *
     * @return 对象键
     */
    public String generateKey(String directory, String filename) {
        String safeName = safeName(filename);
        StringBuilder key = new StringBuilder();
        if (directory != null && !directory.isBlank()) {
            key.append(stripSlashes(directory)).append('/');
        }
        key.append(LocalDate.now().format(DATE_DIR))
                .append('/')
                .append(randomId())
                .append('_')
                .append(safeName);
        return key.toString();
    }

    /**
     * 生成安全的文件名
     * <p>
     * 路径分隔符等危险字符换成下划线，扩展名只保留字母数字形式，
     * 不合法或没有时统一补 {@code .bin}，避免把可执行后缀写进对象键。
     *
     * @param filename 原始文件名
     *
     * @return 去掉路径分隔符等危险字符的文件名
     */
    private String safeName(String filename) {
        if (filename == null || filename.isBlank()) {
            return randomId() + ".bin";
        }
        String name = Path.of(filename).getFileName().toString().replaceAll("[\\\\/:*?\"<>|\\s]", "_");
        int dot = name.lastIndexOf('.');
        String ext = dot > 0 && name.substring(dot).matches("\\.[A-Za-z0-9]{1,16}") ? name.substring(dot) : ".bin";
        return randomId() + ext;
    }

    /**
     * 去掉目录首尾的斜杠
     *
     * @param directory 目录
     *
     * @return 规整后的目录
     */
    private String stripSlashes(String directory) {
        return directory.replace('\\', '/').replaceAll("^/+", "").replaceAll("/+$", "");
    }

    /**
     * 无连字符的随机串
     */
    private static String randomId() {
        return UUID.randomUUID().toString().replace("-", "");
    }

}
