package ext.library.oss;

import ext.library.oss.config.OssProperties;
import ext.library.tool.exception.ExtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 本地对象存储测试
 */
@DisplayName("LocalObjectStorage 测试")
class LocalObjectStorageTest {

    @TempDir
    Path root;

    private OssProperties properties;

    private LocalObjectStorage storage;

    @BeforeEach
    void setUp() throws Exception {
        properties = new OssProperties();
        properties.setPath(root.toString());
        storage = new LocalObjectStorage(properties);
        storage.init();
    }

    @Test
    @DisplayName("写入后能读回内容与元信息")
    void testPutAndGet() throws Exception {
        storage.put("avatar/logo.txt", stream("hello"), 5, "text/plain");

        assertTrue(storage.exists("avatar/logo.txt"));
        ObjectMeta meta = storage.stat("avatar/logo.txt");
        assertEquals(5, meta.size());
        try (OssObject object = storage.get("avatar/logo.txt")) {
            assertEquals("hello", new String(object.content().readAllBytes(), StandardCharsets.UTF_8));
        }
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
    @DisplayName("列举按前缀过滤")
    void testList() {
        storage.put("img/a.png", stream("a"), 1, "image/png");
        storage.put("img/b.png", stream("b"), 1, "image/png");
        storage.put("doc/c.txt", stream("c"), 1, "text/plain");

        List<ObjectItem> items = storage.list("img/");

        assertEquals(2, items.size());
        assertEquals("img/a.png", items.get(0).key());
    }

    @Test
    @DisplayName("删除返回对象是否存在")
    void testDelete() {
        storage.put("x.txt", stream("x"), 1, "text/plain");

        assertTrue(storage.delete("x.txt"));
        assertFalse(storage.delete("x.txt"));
    }

    @Test
    @DisplayName("非法对象键被拒绝")
    void testRejectPathTraversal() {
        assertThrows(ExtException.class, () -> storage.exists("../etc/passwd"));
        assertThrows(ExtException.class, () -> storage.exists("/etc/passwd"));
    }

    @Test
    @DisplayName("未配访问前缀时生成直链报错")
    void testPresignWithoutUrlPrefix() {
        assertThrows(ExtException.class, () -> storage.presign("a.txt", java.time.Duration.ofMinutes(5)));
    }

    @Test
    @DisplayName("配上访问前缀后直链拼接正确")
    void testPresignWithUrlPrefix() {
        properties.setUrlPrefix("https://static.example.com/");

        assertEquals("https://static.example.com/a.txt",
                storage.presign("a.txt", java.time.Duration.ofMinutes(5)));
    }

    private ByteArrayInputStream stream(String content) {
        return new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
    }

}
