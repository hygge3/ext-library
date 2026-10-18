package ext.library.oss.config;

import ext.library.oss.LocalObjectStorage;
import ext.library.oss.ObjectStorage;
import ext.library.oss.OssTemplate;
import ext.library.oss.S3ObjectStorage;
import ext.library.oss.StorageType;
import ext.library.tool.constant.EmojiSymbol;
import ext.library.tool.runtime.Logs;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * 对象存储自动配置
 * <p>
 * 按 {@code ext.oss.type} 选择本地文件系统或 S3 实现。
 *
 * @since 4.1.0
 */
@AutoConfiguration
@EnableConfigurationProperties(OssProperties.class)
@ConditionalOnProperty(prefix = OssProperties.PREFIX, name = "enabled", havingValue = "true",
        matchIfMissing = true)
public class OssAutoConfiguration {

    /**
     * 存储实现
     * <p>
     * MinIO 类型只出现在方法体内部，不出现在签名与返回值上，
     * 这样即使容器里没有 MinIO 依赖，本地模式也能正常启动。
     *
     * @param properties 配置
     *
     * @return 存储实现
     */
    @Bean
    @ConditionalOnMissingBean
    public ObjectStorage objectStorage(OssProperties properties) {
        Logs.info(EmojiSymbol.OSS, "载入模块：对象存储，type={}", properties.getType());
        return switch (properties.getType()) {
            case S3 -> new S3ObjectStorage((io.minio.MinioClient) minioClient(properties), properties);
            case LOCAL -> new LocalObjectStorage(properties);
        };
    }

    /**
     * 构建 MinIO 客户端
     * <p>
     * 返回类型写成 {@link Object}，避免在自动配置类的签名上留下 MinIO 类型引用。
     *
     * @param properties 配置
     *
     * @return 客户端
     */
    private Object minioClient(OssProperties properties) {
        var builder = io.minio.MinioClient.builder()
                .endpoint(properties.getEndpoint())
                .credentials(properties.getAccessKey(), properties.getSecretKey());
        if (properties.getRegion() != null && !properties.getRegion().isBlank()) {
            builder.region(properties.getRegion());
        }
        return builder.build();
    }

    /**
     * 对象存储门面
     *
     * @param storage    存储实现
     * @param properties 配置
     *
     * @return 模板
     */
    @Bean
    @ConditionalOnMissingBean
    public OssTemplate ossTemplate(ObjectStorage storage, OssProperties properties) {
        return new OssTemplate(storage, properties);
    }

}
