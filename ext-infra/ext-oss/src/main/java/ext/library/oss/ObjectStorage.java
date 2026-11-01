package ext.library.oss;

import java.io.InputStream;
import java.time.Duration;
import java.util.List;
import java.util.Objects;

/**
 * 对象存储 SPI
 * <p>
 * 本地文件系统与 S3 协议各实现一份，业务方只依赖 {@link OssTemplate}，
 * 切换实现不需要改动调用代码。
 * <p>
 * 实现里的厂商类型（AWS SDK 的 {@code S3Client} / {@code S3Presigner}）不得出现在
 * SPI 签名上——否则没有该 SDK 的环境连接口都加载不了，其他模式也跟着起不来。
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
     * 读取对象元信息（不下载内容）
     *
     * @param key 对象键
     *
     * @return 元信息
     */
    ObjectMeta stat(String key);

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
     * 同桶复制对象
     * <p>
     * S3 实现走服务端 {@code CopyObject}，数据不经过应用进程。
     *
     * @param sourceKey 源对象键
     * @param targetKey 目标对象键
     */
    void copy(String sourceKey, String targetKey);

    /**
     * 跨桶复制对象
     * <p>
     * 只有 S3 实现有桶的概念，其他实现默认在桶名不同时直接拒绝——跨实现、跨桶的数据搬运是迁移工具的事，
     * 门面不为此在运行时装配第二个客户端。
     *
     * @param sourceBucket 源桶
     * @param sourceKey    源对象键
     * @param targetBucket 目标桶
     * @param targetKey    目标对象键
     */
    default void copy(String sourceBucket, String sourceKey, String targetBucket, String targetKey) {
        if (!Objects.equals(sourceBucket, targetBucket)) {
            throw new UnsupportedOperationException(name() + " 实现没有桶的概念，不支持跨桶复制");
        }
        copy(sourceKey, targetKey);
    }

    /**
     * 同桶移动对象
     * <p>
     * S3 协议没有原子 rename，所以是「复制 + 删除」两步，中间失败会留下源对象（不会丢数据）。
     *
     * @param sourceKey 源对象键
     * @param targetKey 目标对象键
     */
    default void move(String sourceKey, String targetKey) {
        copy(sourceKey, targetKey);
        delete(sourceKey);
    }

    /**
     * 跨桶移动对象
     *
     * @param sourceBucket 源桶
     * @param sourceKey    源对象键
     * @param targetBucket 目标桶
     * @param targetKey    目标对象键
     */
    default void move(String sourceBucket, String sourceKey, String targetBucket, String targetKey) {
        copy(sourceBucket, sourceKey, targetBucket, targetKey);
        if (!Objects.equals(sourceBucket, targetBucket)) {
            throw new UnsupportedOperationException(name() + " 实现没有桶的概念，不支持跨桶移动");
        }
        delete(sourceKey);
    }

    /**
     * 生成下载直链
     * <p>
     * 本地实现返回 {@code 对外访问前缀 + "/" + key}（没配前缀时拒绝）。
     *
     * @param key    对象键
     * @param expiry 有效期
     *
     * @return 可直接访问的 URL
     */
    String presign(String key, Duration expiry);

    /**
     * 生成上传直链（预签名 PUT）
     *
     * @param key    对象键
     * @param expiry 有效期
     *
     * @return 可直接 PUT 的 URL
     */
    String presignPut(String key, Duration expiry);

}
