package ext.library.oss;

/**
 * 存储类型
 *
 * @since 4.1.0
 */
public enum StorageType {

    /**
     * 本地文件系统
     */
    LOCAL,

    /**
     * S3 协议，对接 MinIO、Ceph、阿里云 OSS 等
     */
    S3

}
