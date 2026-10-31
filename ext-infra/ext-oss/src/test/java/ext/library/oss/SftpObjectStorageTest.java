package ext.library.oss;

import ext.library.oss.config.OssProperties;
import ext.library.tool.exception.ExtException;
import org.apache.sshd.client.SshClient;
import org.apache.sshd.client.keyverifier.AcceptAllServerKeyVerifier;
import org.apache.sshd.common.file.virtualfs.VirtualFileSystemFactory;
import org.apache.sshd.server.SshServer;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.apache.sshd.sftp.server.SftpSubsystemFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * SFTP 对象存储测试
 * <p>
 * 起一个进程内的 MINA SSHD 服务端（虚拟文件系统指向临时目录），跑真实的 SFTP 往返，
 * 不需要 Docker，也不碰真实主机。
 */
@DisplayName("SftpObjectStorage 测试")
class SftpObjectStorageTest {

    private static final String USER = "ext";

    /** 进程内临时服务端的登录密码，随实例生成，不留固定口令 */
    private static final String PASSWORD = UUID.randomUUID().toString();

    /**
     * 服务端虚拟文件系统的根
     */
    @TempDir
    Path root;

    /**
     * 主机密钥目录，刻意放在虚拟根之外，免得被当成对象列出来
     */
    @TempDir
    Path keys;

    private SshServer server;

    private OssProperties properties;

    private SftpObjectStorage storage;

    @BeforeEach
    void setUp() throws Exception {
        server = SshServer.setUpDefaultServer();
        server.setHost("127.0.0.1");
        server.setPort(0);
        server.setKeyPairProvider(new SimpleGeneratorHostKeyProvider(keys.resolve("host-key.ser")));
        server.setPasswordAuthenticator(
                (username, password, session) -> USER.equals(username) && PASSWORD.equals(password));
        server.setSubsystemFactories(List.of(new SftpSubsystemFactory()));
        server.setFileSystemFactory(new VirtualFileSystemFactory(root));
        server.start();

        properties = new OssProperties();
        properties.setBucket("ext-library");
        OssProperties.Sftp sftp = properties.getSftp();
        sftp.setHost("127.0.0.1");
        sftp.setPort(server.getPort());
        sftp.setUsername(USER);
        sftp.setPassword(PASSWORD);
        sftp.setBasePath("/data/oss");

        SshClient client = SshClient.setUpDefaultClient();
        // 服务端是本地临时进程，主机密钥每次都换
        client.setServerKeyVerifier(AcceptAllServerKeyVerifier.INSTANCE);
        storage = new SftpObjectStorage(client, properties);
        storage.init();
    }

    @AfterEach
    void tearDown() throws Exception {
        storage.destroy();
        server.stop(true);
    }

    @Test
    @DisplayName("写入后能读回内容与元信息")
    void testPutAndGet() throws Exception {
        storage.put("avatar/logo.txt", stream("hello"), 5, "text/plain");

        assertTrue(storage.exists("avatar/logo.txt"));
        ObjectMeta meta = storage.stat("avatar/logo.txt");
        assertEquals(5, meta.size());
        assertEquals("text/plain", meta.contentType());
        try (OssObject object = storage.get("avatar/logo.txt")) {
            assertEquals("hello", new String(object.content().readAllBytes(), StandardCharsets.UTF_8));
        }
    }

    @Test
    @DisplayName("重复写入覆盖旧内容，且不留下临时文件")
    void testPutOverwritesAndHidesTempFile() throws Exception {
        storage.put("a/report.txt", stream("v1"), 2, "text/plain");
        storage.put("a/report.txt", stream("v2-longer"), 9, "text/plain");

        try (OssObject object = storage.get("a/report.txt")) {
            assertEquals("v2-longer", new String(object.content().readAllBytes(), StandardCharsets.UTF_8));
        }
        // 先写 .uploading 再 rename，列举里只该看到最终对象
        List<ObjectItem> items = storage.list("a/");
        assertEquals(1, items.size());
        assertEquals("a/report.txt", items.get(0).key());
    }

    @Test
    @DisplayName("复制与移动后源对象状态正确")
    void testCopyAndMove() {
        storage.put("a.txt", stream("data"), 4, "text/plain");

        storage.copy("a.txt", "dir/a.txt");
        assertTrue(storage.exists("a.txt"));
        assertTrue(storage.exists("dir/a.txt"));

        storage.move("a.txt", "b.txt");
        assertFalse(storage.exists("a.txt"));
        assertTrue(storage.exists("b.txt"));
    }

    @Test
    @DisplayName("列举按前缀过滤并按 key 排序")
    void testList() {
        storage.put("img/b.png", stream("b"), 1, "image/png");
        storage.put("img/a.png", stream("a"), 1, "image/png");
        storage.put("doc/c.txt", stream("c"), 1, "text/plain");

        List<ObjectItem> items = storage.list("img/");

        assertEquals(2, items.size());
        assertEquals("img/a.png", items.get(0).key());
        assertEquals("img/b.png", items.get(1).key());
        assertEquals(1, items.get(0).size());
    }

    @Test
    @DisplayName("列举未写入过的前缀返回空而不报错")
    void testListMissingPrefix() {
        assertTrue(storage.list("nothing/here/").isEmpty());
    }

    @Test
    @DisplayName("删除返回对象是否存在")
    void testDelete() {
        storage.put("x.txt", stream("x"), 1, "text/plain");

        assertTrue(storage.delete("x.txt"));
        assertFalse(storage.delete("x.txt"));
    }

    @Test
    @DisplayName("读取不存在的对象报业务异常")
    void testGetMissing() {
        assertThrows(ExtException.class, () -> storage.get("missing.txt"));
        assertThrows(ExtException.class, () -> storage.stat("missing.txt"));
        assertFalse(storage.exists("missing.txt"));
    }

    @Test
    @DisplayName("非法对象键被拒绝")
    void testRejectPathTraversal() {
        assertThrows(ExtException.class, () -> storage.exists("../etc/passwd"));
        assertThrows(ExtException.class, () -> storage.exists("/etc/passwd"));
    }

    @Test
    @DisplayName("未配访问前缀时生成直链报错")
    void testPresignWithoutAccessPrefix() {
        assertThrows(UnsupportedOperationException.class, () -> storage.presign("a.txt", Duration.ofMinutes(5)));
    }

    @Test
    @DisplayName("配上访问前缀后直链拼接正确")
    void testPresignWithAccessPrefix() {
        properties.getSftp().setAccessPrefix("https://static.example.com/");

        assertEquals("https://static.example.com/a.txt", storage.presign("a.txt", Duration.ofMinutes(5)));
    }

    @Test
    @DisplayName("预签名上传始终不支持")
    void testPresignPutUnsupported() {
        properties.getSftp().setAccessPrefix("https://static.example.com");

        assertThrows(UnsupportedOperationException.class, () -> storage.presignPut("a.txt", Duration.ofMinutes(5)));
    }

    private InputStream stream(String content) {
        return new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
    }

}
