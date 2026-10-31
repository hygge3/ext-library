package ext.library.oss;

import ext.library.oss.config.OssProperties;
import ext.library.tool.constant.EmojiSymbol;
import ext.library.tool.exception.ExtException;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.apache.sshd.client.SshClient;
import org.apache.sshd.client.session.ClientSession;
import org.apache.sshd.common.NamedResource;
import org.apache.sshd.common.config.keys.FilePasswordProvider;
import org.apache.sshd.common.util.security.SecurityUtils;
import org.apache.sshd.sftp.client.SftpClient;
import org.apache.sshd.sftp.client.SftpClientFactory;
import org.apache.sshd.sftp.client.extensions.CopyFileExtension;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.time.Duration;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Deque;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Semaphore;

/**
 * SSH / SFTP 对象存储
 * <p>
 * 远端目录当桶用：{@code ext.oss.sftp.base-path} 是根，对象键是相对路径。
 * 上传先写临时文件再 {@code rename}，避免别人读到写了一半的对象；连接按需建、用完还池，
 * 池大小由 {@code ext.oss.sftp.pool-size} 控制。
 *
 * @since 4.1.0
 */
public class SftpObjectStorage implements ObjectStorage {

    private static final String TEMP_SUFFIX = ".uploading";

    private final OssProperties.Sftp sftp;

    private final OssProperties properties;

    private final SshClient sshClient;

    /**
     * 空闲连接，取不到时新建（数量受 {@link #permits} 限制）
     */
    private final Deque<Holder> idle = new ArrayDeque<>();

    private final Semaphore permits;

    /**
     * 构造 SFTP 存储
     *
     * @param sshClient  {@code org.apache.sshd.client.SshClient}，由自动配置类造好传进来
     * @param properties 配置
     */
    public SftpObjectStorage(Object sshClient, OssProperties properties) {
        this.sshClient = (SshClient) sshClient;
        this.properties = properties;
        this.sftp = properties.getSftp();
        this.permits = new Semaphore(Math.max(1, sftp.getPoolSize()));
    }

    /**
     * 启动 SSH 客户端
     */
    @PostConstruct
    public void init() {
        sshClient.start();
    }

    /**
     * 关闭所有连接与 SSH 客户端
     */
    @PreDestroy
    public void destroy() {
        List<Holder> toClose;
        synchronized (idle) {
            toClose = new ArrayList<>(idle);
            idle.clear();
        }
        toClose.forEach(this::closeQuietly);
        sshClient.stop();
    }

    @Override
    public String name() {
        return "sftp";
    }

    @Override
    public void put(String key, InputStream content, long size, String contentType) {
        String path = resolve(key);
        String tempPath = path + TEMP_SUFFIX;
        execute(client -> {
            // 远端目录不像 S3 那样隐式存在，父目录得自己建
            ensureDirectory(client, parent(path));
            try (OutputStream out = client.write(tempPath, OpenModes.WRITE)) {
                content.transferTo(out);
            }
            // 先落临时文件再改名，读方不会看到半写对象
            client.rename(tempPath, path, SftpClient.CopyMode.Overwrite);
            return null;
        }, "写入对象失败：" + key);
    }

    @Override
    public OssObject get(String key) {
        String path = resolve(key);
        Holder holder = borrow();
        try {
            SftpClient.Attributes attributes = statAttributes(holder.client, path, key);
            InputStream content = holder.client.read(path);
            // 连接要等流读完才能还池：同一个 SftpClient 不能并发使用
            return new OssObject(properties.getBucket(), key, contentType(path), attributes.getSize(),
                    new FilterInputStream(content) {
                        @Override
                        public void close() throws IOException {
                            try {
                                super.close();
                            } finally {
                                release(holder);
                            }
                        }
                    });
        } catch (IOException e) {
            holder.broken = true;
            release(holder);
            throw new ExtException(EmojiSymbol.OSS, e, "读取对象失败：" + key);
        } catch (RuntimeException e) {
            // 对象不存在这类业务异常，连接本身是好的，照常还池
            release(holder);
            throw e;
        }
    }

    @Override
    public ObjectMeta stat(String key) {
        String path = resolve(key);
        return execute(client -> {
            SftpClient.Attributes attributes = statAttributes(client, path, key);
            String modified = attributes.getModifyTime() == null ? "" : attributes.getModifyTime().toString();
            return new ObjectMeta(properties.getBucket(), key, attributes.getSize(), contentType(path), modified);
        }, "读取对象信息失败：" + key);
    }

    @Override
    public boolean delete(String key) {
        String path = resolve(key);
        return execute(client -> {
            if (!isFile(client, path)) {
                return false;
            }
            client.remove(path);
            return true;
        }, "删除对象失败：" + key);
    }

    @Override
    public boolean exists(String key) {
        String path = resolve(key);
        return execute(client -> isFile(client, path), "查询对象失败：" + key);
    }

    @Override
    public List<ObjectItem> list(String prefix) {
        String realPrefix = prefix == null ? "" : prefix.replace('\\', '/');
        return execute(client -> {
            List<ObjectItem> items = new ArrayList<>();
            collect(client, basePath(), realPrefix, items);
            return items.stream().sorted(Comparator.comparing(ObjectItem::key)).toList();
        }, "列举对象失败：" + prefix);
    }

    @Override
    public void copy(String sourceKey, String targetKey) {
        String source = resolve(sourceKey);
        String target = resolve(targetKey);
        execute(client -> {
            ensureDirectory(client, parent(target));
            CopyFileExtension extension = client.getExtension(CopyFileExtension.class);
            if (extension != null && extension.isSupported()) {
                // 服务端 copy-data：数据不经过应用进程
                extension.copyFile(source, target, true);
            } else {
                // 老服务端不支持扩展，退化为流式复制（大文件会占用带宽，但至少能用）
                try (InputStream in = client.read(source);
                     OutputStream out = client.write(target, OpenModes.WRITE)) {
                    in.transferTo(out);
                }
            }
            return null;
        }, "复制对象失败：" + sourceKey + " -> " + targetKey);
    }

    @Override
    public void move(String sourceKey, String targetKey) {
        String source = resolve(sourceKey);
        String target = resolve(targetKey);
        execute(client -> {
            ensureDirectory(client, parent(target));
            client.rename(source, target, SftpClient.CopyMode.Overwrite);
            return null;
        }, "移动对象失败：" + sourceKey + " -> " + targetKey);
    }

    @Override
    public String presign(String key, Duration expiry) {
        String accessPrefix = sftp.getAccessPrefix();
        if (accessPrefix == null || accessPrefix.isBlank()) {
            throw new UnsupportedOperationException("SFTP 没有预签名能力，请配置 ext.oss.sftp.access-prefix 以拼接对外直链");
        }
        return stripTrailingSlash(accessPrefix) + "/" + key;
    }

    @Override
    public String presignPut(String key, Duration expiry) {
        throw new UnsupportedOperationException("SFTP 没有预签名上传，远端目录只能由服务端自己写");
    }

    // ──────────── 内部实现 ────────────

    /**
     * 借一条连接执行操作
     */
    private <T> T execute(SftpTask<T> task, String errorMessage) {
        Holder holder = borrow();
        try {
            return task.run(holder.client);
        } catch (IOException | GeneralSecurityException e) {
            // 连接可能已经烂了，别还回池子
            holder.broken = true;
            throw new ExtException(EmojiSymbol.OSS, e, errorMessage);
        } finally {
            release(holder);
        }
    }

    /**
     * 借连接：优先用空闲的，没有就新建（受信号量限流）
     */
    private Holder borrow() {
        synchronized (idle) {
            Holder pooled = idle.pollFirst();
            if (pooled != null) {
                return pooled;
            }
        }
        try {
            permits.acquire();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ExtException(EmojiSymbol.OSS, e, "等待 SFTP 连接时被中断");
        }
        try {
            return openConnection();
        } catch (ExtException e) {
            permits.release();
            throw e;
        } catch (IOException | GeneralSecurityException | RuntimeException e) {
            permits.release();
            throw new ExtException(EmojiSymbol.OSS, e, "连接 SFTP 失败：{}:{}", sftp.getHost(), sftp.getPort());
        }
    }

    /**
     * 归还连接
     */
    private void release(Holder holder) {
        if (holder.broken) {
            closeQuietly(holder);
            return;
        }
        synchronized (idle) {
            idle.addLast(holder);
        }
    }

    /**
     * 建立一条新连接
     */
    private Holder openConnection() throws IOException, GeneralSecurityException {
        ClientSession session = sshClient.connect(sftp.getUsername(), sftp.getHost(), sftp.getPort())
                .verify(Duration.ofSeconds(Math.max(1, sftp.getConnectTimeoutSeconds())))
                .getSession();

        if (sftp.getPassword() != null && !sftp.getPassword().isBlank()) {
            session.addPasswordIdentity(sftp.getPassword());
        }
        if (sftp.getPrivateKey() != null && !sftp.getPrivateKey().isBlank()) {
            Path keyPath = Path.of(sftp.getPrivateKey());
            FilePasswordProvider passwordProvider = sftp.getPrivateKeyPassword() == null
                    || sftp.getPrivateKeyPassword().isEmpty()
                    ? FilePasswordProvider.EMPTY
                    : FilePasswordProvider.of(sftp.getPrivateKeyPassword());
            try (InputStream in = Files.newInputStream(keyPath)) {
                Iterable<KeyPair> identities = SecurityUtils.loadKeyPairIdentities(
                        null, NamedResource.ofName(keyPath.toString()), in, passwordProvider);
                for (KeyPair identity : identities) {
                    session.addPublicKeyIdentity(identity);
                }
            }
        }
        session.auth().verify(Duration.ofSeconds(Math.max(1, sftp.getConnectTimeoutSeconds())));

        SftpClient client = SftpClientFactory.instance().createSftpClient(session);
        ensureDirectory(client, basePath());
        return new Holder(session, client);
    }

    /**
     * 关闭连接
     */
    private void closeQuietly(Holder holder) {
        try {
            holder.client.close();
        } catch (IOException ignored) {
            // 关不掉也没别的办法
        }
        try {
            holder.session.close();
        } catch (IOException ignored) {
            // 同上
        }
        permits.release();
    }

    /**
     * 递归收集目录下的文件
     */
    private void collect(SftpClient client, String directory, String prefix, List<ObjectItem> items) {
        Collection<SftpClient.DirEntry> entries;
        try {
            entries = client.readEntries(directory);
        } catch (IOException e) {
            // 目录不存在时按空处理：列举一个还没写过的前缀不该报错
            return;
        }
        for (SftpClient.DirEntry entry : entries) {
            String name = entry.getFilename();
            if (".".equals(name) || "..".equals(name)) {
                continue;
            }
            String path = join(directory, name);
            String key = relativize(path);
            if (entry.getAttributes().isDirectory()) {
                collect(client, path, prefix, items);
            } else if (key.startsWith(prefix)) {
                SftpClient.Attributes attributes = entry.getAttributes();
                String modified = attributes.getModifyTime() == null ? "" : attributes.getModifyTime().toString();
                items.add(new ObjectItem(key, attributes.getSize(), modified));
            }
        }
    }

    /**
     * 逐级创建目录，已存在则跳过
     */
    private void ensureDirectory(SftpClient client, String path) throws IOException {
        StringBuilder current = new StringBuilder();
        for (String segment : path.split("/")) {
            if (segment.isEmpty()) {
                continue;
            }
            current.append('/').append(segment);
            String dir = current.toString();
            try {
                client.stat(dir);
            } catch (IOException notExists) {
                try {
                    client.mkdir(dir);
                } catch (IOException e) {
                    // 并发下可能被别人先建了，再查一次确认
                    client.stat(dir);
                }
            }
        }
    }

    /**
     * 取远端文件属性，不存在时抛业务异常
     */
    private SftpClient.Attributes statAttributes(SftpClient client, String path, String key) throws IOException {
        if (!isFile(client, path)) {
            throw new ExtException(EmojiSymbol.OSS, "对象不存在：{}", key);
        }
        return client.stat(path);
    }

    /**
     * 判断远端路径是否为可读文件
     */
    private boolean isFile(SftpClient client, String path) {
        try {
            return client.stat(path).isRegularFile();
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * 桶根目录
     */
    private String basePath() {
        String base = sftp.getBasePath();
        if (base == null || base.isBlank()) {
            return "/";
        }
        return base.startsWith("/") ? base : "/" + base;
    }

    /**
     * 把对象键解析成远端绝对路径
     */
    private String resolve(String key) {
        if (key == null || key.isBlank() || key.contains("..") || key.startsWith("/")) {
            throw new ExtException(EmojiSymbol.OSS, "非法的对象键：{}", key);
        }
        return join(basePath(), key);
    }

    /**
     * 取路径的父目录
     */
    private String parent(String path) {
        int slash = path.lastIndexOf('/');
        return slash <= 0 ? "/" : path.substring(0, slash);
    }

    /**
     * 拼远端路径
     */
    private String join(String directory, String name) {
        return directory.endsWith("/") ? directory + name : directory + "/" + name;
    }

    /**
     * 远端绝对路径转回对象键
     */
    private String relativize(String path) {
        String base = basePath();
        String relative = path.startsWith(base) ? path.substring(base.length()) : path;
        return relative.replaceAll("^/+", "");
    }

    /**
     * 按扩展名猜内容类型
     */
    private String contentType(String path) {
        String guess = URLConnection.guessContentTypeFromName(path);
        return guess == null ? "application/octet-stream" : guess;
    }

    /**
     * 去掉访问前缀末尾的斜杠
     */
    private String stripTrailingSlash(String prefix) {
        return prefix.endsWith("/") ? prefix.substring(0, prefix.length() - 1) : prefix;
    }

    /**
     * 连接持有者
     */
    private static final class Holder {

        private final ClientSession session;

        private final SftpClient client;

        private boolean broken;

        private Holder(ClientSession session, SftpClient client) {
            this.session = session;
            this.client = client;
        }

    }

    /**
     * 打开模式常量：写 + 覆盖
     */
    private static final class OpenModes {

        private static final Collection<SftpClient.OpenMode> WRITE =
                List.of(SftpClient.OpenMode.Write, SftpClient.OpenMode.Create, SftpClient.OpenMode.Truncate);

    }

    /**
     * 需要连接的 SFTP 操作
     */
    @FunctionalInterface
    private interface SftpTask<T> {

        T run(SftpClient client) throws IOException, GeneralSecurityException;

    }

}
