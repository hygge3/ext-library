package ext.library.oss;

import ext.library.oss.config.OssProperties;
import ext.library.tool.constant.EmojiSymbol;
import ext.library.tool.exception.ExtException;
import jakarta.annotation.PostConstruct;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * 本地文件系统对象存储
 * <p>
 * 所有键都在 {@link OssProperties#getPath()} 指定的根目录下解析，
 * 含 {@code ..} 或以 {@code /} 开头的键一律拒绝，避免路径穿越。
 *
 * @since 4.1.0
 */
public class LocalObjectStorage implements ObjectStorage {

    private static final String DEFAULT_CONTENT_TYPE = "application/octet-stream";

    private final OssProperties properties;

    private final Path root;

    /**
     * 构造本地存储
     *
     * @param properties 配置
     */
    public LocalObjectStorage(OssProperties properties) {
        this.properties = properties;
        this.root = Path.of(properties.getPath()).toAbsolutePath().normalize();
    }

    /**
     * 初始化根目录
     *
     * @throws IOException 创建目录失败时抛出
     */
    @PostConstruct
    public void init() throws IOException {
        Files.createDirectories(root);
    }

    @Override
    public String name() {
        return "local";
    }

    @Override
    public void put(String key, InputStream content, long size, String contentType) {
        Path path = resolve(key);
        try {
            Path parent = path.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            if (size > 0) {
                Files.copy(content, path, StandardCopyOption.REPLACE_EXISTING);
            } else {
                // 长度未知时无法预估拷贝尺寸，退化为边读边写
                content.transferTo(Files.newOutputStream(path, StandardOpenOption.CREATE,
                        StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE));
            }
        } catch (IOException e) {
            throw new ExtException(EmojiSymbol.OSS, e, "写入对象失败：{}", key);
        }
    }

    @Override
    public OssObject get(String key) {
        Path path = resolve(key);
        if (!Files.exists(path)) {
            throw new ExtException(EmojiSymbol.OSS, "对象不存在：{}", key);
        }
        try {
            InputStream stream = Files.newInputStream(path);
            String contentType = Optional.ofNullable(Files.probeContentType(path)).orElse(DEFAULT_CONTENT_TYPE);
            return new OssObject(properties.getBucket(), key, contentType, Files.size(path), stream);
        } catch (IOException e) {
            throw new ExtException(EmojiSymbol.OSS, e, "读取对象失败：{}", key);
        }
    }

    @Override
    public ObjectMeta stat(String key) {
        Path path = resolve(key);
        if (!Files.exists(path)) {
            throw new ExtException(EmojiSymbol.OSS, "对象不存在：{}", key);
        }
        try {
            return new ObjectMeta(
                    properties.getBucket(),
                    key,
                    Files.size(path),
                    Optional.ofNullable(Files.probeContentType(path)).orElse(DEFAULT_CONTENT_TYPE),
                    Instant.ofEpochMilli(Files.getLastModifiedTime(path).toMillis()).toString()
            );
        } catch (IOException e) {
            throw new ExtException(EmojiSymbol.OSS, e, "读取对象信息失败：{}", key);
        }
    }

    @Override
    public boolean delete(String key) {
        try {
            return Files.deleteIfExists(resolve(key));
        } catch (IOException e) {
            throw new ExtException(EmojiSymbol.OSS, e, "删除对象失败：{}", key);
        }
    }

    @Override
    public boolean exists(String key) {
        return Files.exists(resolve(key));
    }

    @Override
    public List<ObjectItem> list(String prefix) {
        String realPrefix = prefix == null ? "" : prefix;
        try (Stream<Path> stream = Files.walk(root)) {
            return stream.filter(Files::isRegularFile)
                    .map(path -> root.relativize(path).toString().replace('\\', '/'))
                    .filter(key -> key.startsWith(realPrefix))
                    .sorted()
                    .map(key -> {
                        try {
                            Path path = resolve(key);
                            return new ObjectItem(key, Files.size(path), Files.getLastModifiedTime(path).toString());
                        } catch (IOException e) {
                            throw new ExtException(EmojiSymbol.OSS, e, "读取对象信息失败：{}", key);
                        }
                    })
                    .toList();
        } catch (IOException e) {
            throw new ExtException(EmojiSymbol.OSS, e, "列举对象失败：{}", prefix);
        }
    }

    @Override
    public void copy(String sourceKey, String targetKey) {
        Path source = resolve(sourceKey);
        Path target = resolve(targetKey);
        if (!Files.exists(source)) {
            throw new ExtException(EmojiSymbol.OSS, "对象不存在：{}", sourceKey);
        }
        try {
            Path parent = target.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new ExtException(EmojiSymbol.OSS, e, "复制对象失败：{} -> {}", sourceKey, targetKey);
        }
    }

    /**
     * 同桶移动
     * <p>
     * 本地文件系统有真正的 rename，不走「复制 + 删除」，也不会出现目标存在时的半成品。
     *
     * @param sourceKey 源对象键
     * @param targetKey 目标对象键
     */
    @Override
    public void move(String sourceKey, String targetKey) {
        Path source = resolve(sourceKey);
        Path target = resolve(targetKey);
        if (!Files.exists(source)) {
            throw new ExtException(EmojiSymbol.OSS, "对象不存在：{}", sourceKey);
        }
        try {
            Path parent = target.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new ExtException(EmojiSymbol.OSS, e, "移动对象失败：{} -> {}", sourceKey, targetKey);
        }
    }

    @Override
    public String presign(String key, Duration expiry) {
        String prefix = properties.getUrlPrefix();
        if (prefix == null || prefix.isBlank()) {
            throw new ExtException(EmojiSymbol.OSS,
                    "本地对象存储没有配置 ext.oss.url-prefix，无法生成直链：{}", key);
        }
        return stripTrailingSlash(prefix) + "/" + key;
    }

    /**
     * 生成上传直链
     * <p>
     * 本地实现没有签名能力，也没有能接收 PUT 的端点——上传直链是 S3 才有的东西。
     *
     * @param key    对象键
     * @param expiry 有效期
     *
     * @return 不会返回
     */
    @Override
    public String presignPut(String key, Duration expiry) {
        throw new UnsupportedOperationException("本地对象存储不支持预签名上传，请直接用 put/putFile 写入");
    }

    /**
     * 去掉访问前缀末尾的斜杠
     *
     * @param prefix 前缀
     *
     * @return 规整后的前缀
     */
    private String stripTrailingSlash(String prefix) {
        return prefix.endsWith("/") ? prefix.substring(0, prefix.length() - 1) : prefix;
    }

    /**
     * 把键解析成受根目录约束的绝对路径
     *
     * @param key 对象键
     *
     * @return 绝对路径
     */
    private Path resolve(String key) {
        if (key == null || key.isBlank() || key.contains("..") || key.startsWith("/")) {
            throw new ExtException(EmojiSymbol.OSS, "非法的对象键：{}", key);
        }
        Path path = root.resolve(key).normalize();
        if (!path.startsWith(root)) {
            throw new ExtException(EmojiSymbol.OSS, "非法的对象键：{}", key);
        }
        return path;
    }

}
