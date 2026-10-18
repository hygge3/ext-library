package ext.library.oss;

import java.io.InputStream;
import java.time.Duration;
import java.util.List;

/**
 * 对象存储 SPI
 * <p>
 * 本地文件系统与 S3 协议各实现一份，业务方只依赖 {@link OssTemplate}，
 * 切换实现不需要改动调用代码。
 *
 * @since 4.1.0
 */
public interface ObjectStorage {

    /**
     * 实现名称，用于日志
     *
     * @return 实现名称
     */
    String name();

    /**
     * 写入对象
     *
     * @param key         对象键
     * @param content     内容流，由调用方关闭
     * @param size        字节数，小于等于 0 时实现自行决定如何读取
     * @param contentType 内容类型
     */
    void put(String key, InputStream content, long size, String contentType);

    /**
     * 读取对象
     *
     * @param key 对象键
     *
     * @return 对象，调用方负责关闭
     */
    OssObject get(String key);

    /**
     * 删除对象
     *
     * @param key 对象键
     *
     * @return 是否确实删除了一个对象
     */
    boolean delete(String key);

    /**
     * 判断对象是否存在
     *
     * @param key 对象键
     *
     * @return 存在返回 {@code true}
     */
    boolean exists(String key);

    /**
     * 按前缀列举对象
     *
     * @param prefix 键前缀
     *
     * @return 按 key 升序排列的不可变列表
     */
    List<ObjectItem> list(String prefix);

    /**
     * 生成下载直链
     * <p>
     * 本地实现下返回 {@code urlPrefix + "/" + key}。
     *
     * @param key     对象键
     * @param expiry  有效期
     *
     * @return 可直接访问的 URL
     */
    String presign(String key, Duration expiry);

}
