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
     * S3 协议（AWS SDK for Java v2），对接 AWS S3、MinIO、Ceph、R2、阿里云 OSS 等
     */
    S3,

    /**
     * SSH / SFTP 远端目录
     */
    SFTP

}
