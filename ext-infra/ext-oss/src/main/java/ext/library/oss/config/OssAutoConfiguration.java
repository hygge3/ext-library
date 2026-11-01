package ext.library.oss.config;

import ext.library.oss.LocalObjectStorage;
import ext.library.oss.ObjectStorage;
import ext.library.oss.OssTemplate;
import ext.library.oss.S3ObjectStorage;
import ext.library.tool.constant.EmojiSymbol;
import ext.library.tool.exception.ExtException;
import ext.library.tool.runtime.Logs;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.net.URI;

/**
 * 对象存储自动配置
 * <p>
 * 按 {@code ext.oss.type} 选择本地文件系统或 S3 实现。厂商类型只出现在本类的私有方法体内，
 * 因此没引对应 SDK 时选其他模式照样能启动。
 *
 * @since 4.1.0
 */
@AutoConfiguration
@EnableConfigurationProperties(OssProperties.class)
@ConditionalOnProperty(prefix = OssProperties.PREFIX, name = "enabled", havingValue = "true",
        matchIfMissing = true)
public class OssAutoConfiguration {

    /**
     * 对象存储门面
     *
     * @param storage    存储实现
     * @param properties 配置
     *
     * @return 门面实例
     */
    @Bean
    @ConditionalOnMissingBean
    public OssTemplate ossTemplate(ObjectStorage storage, OssProperties properties) {
        Logs.info(EmojiSymbol.OSS, "载入模块：对象存储（类型: {}）", storage.name());
        return new OssTemplate(storage, properties);
    }

    /**
     * 存储实现
     *
     * @param properties 配置
     *
     * @return 按 {@code ext.oss.type} 选出的实现
     */
    @Bean
    @ConditionalOnMissingBean
    public ObjectStorage objectStorage(OssProperties properties) {
        return switch (properties.getType()) {
            case LOCAL -> new LocalObjectStorage(properties);
            case S3 -> new S3ObjectStorage(s3Client(properties), s3Presigner(properties), properties);
        };
    }

    /**
     * 创建 S3 客户端
     * <p>
     * 手写凭证而不是走默认的凭证链：对象存储的密钥来自配置，不该在容器里四处寻找。
     *
     * @param properties 配置
     *
     * @return S3 客户端
     */
    private Object s3Client(OssProperties properties) {
        if (isBlank(properties.getAccessKey()) || isBlank(properties.getSecretKey())) {
            throw new ExtException(EmojiSymbol.OSS, "ext.oss.type=s3 时必须配置 access-key 与 secret-key");
        }
        return S3Client.builder()
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(properties.getAccessKey(), properties.getSecretKey())))
                .region(Region.of(properties.getRegion()))
                .endpointOverride(isBlank(properties.getEndpoint()) ? null : URI.create(properties.getEndpoint()))
                .serviceConfiguration(S3Configuration.builder()
                        .pathStyleAccessEnabled(properties.isPathStyleAccess())
                        .chunkedEncodingEnabled(properties.isChunkedEncoding())
                        .build())
                .build();
    }

    /**
     * 创建 S3 预签名器
     *
     * @param properties 配置
     *
     * @return 预签名器
     */
    private Object s3Presigner(OssProperties properties) {
        return S3Presigner.builder()
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(properties.getAccessKey(), properties.getSecretKey())))
                .region(Region.of(properties.getRegion()))
                .endpointOverride(isBlank(properties.getEndpoint()) ? null : URI.create(properties.getEndpoint()))
                .serviceConfiguration(S3Configuration.builder()
                        .pathStyleAccessEnabled(properties.isPathStyleAccess())
                        .chunkedEncodingEnabled(properties.isChunkedEncoding())
                        .build())
                .build();
    }

    /**
     * 判断字符串是否为空
     */
    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

}
