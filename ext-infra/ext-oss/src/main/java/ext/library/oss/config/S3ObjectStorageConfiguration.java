package ext.library.oss.config;

import ext.library.oss.ObjectStorage;
import ext.library.oss.S3ObjectStorage;
import ext.library.oss.StorageType;
import ext.library.tool.constant.EmojiSymbol;
import ext.library.tool.exception.ExtException;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
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
 * S3 存储自动配置
 * <p>
 * 整个类被 {@code @ConditionalOnClass(S3Client)} 挡住：没引 AWS SDK 时本类不加载，
 * 其余类型（LOCAL）照常工作。
 *
 * @since 4.1.0
 */
@AutoConfiguration
@EnableConfigurationProperties(OssProperties.class)
@ConditionalOnClass(S3Client.class)
@ConditionalOnProperty(prefix = OssProperties.PREFIX, name = "type", havingValue = "S3")
public class S3ObjectStorageConfiguration {

    /**
     * S3 客户端
     * <p>
     * 手写凭证而不是走默认的凭证链：对象存储的密钥来自配置，不该在容器里四处寻找。
     *
     * @param properties 配置
     *
     * @return S3 客户端
     */
    @Bean
    @ConditionalOnMissingBean
    public S3Client s3Client(OssProperties properties) {
        return S3Client.builder()
                .credentialsProvider(credentials(properties))
                .region(Region.of(properties.getRegion()))
                .endpointOverride(endpoint(properties))
                .serviceConfiguration(serviceConfiguration(properties))
                .build();
    }

    /**
     * 预签名器，{@code presign*} 系列方法用它生成临时链接
     *
     * @param properties 配置
     *
     * @return 预签名器
     */
    @Bean
    @ConditionalOnMissingBean
    public S3Presigner s3Presigner(OssProperties properties) {
        return S3Presigner.builder()
                .credentialsProvider(credentials(properties))
                .region(Region.of(properties.getRegion()))
                .endpointOverride(endpoint(properties))
                .serviceConfiguration(serviceConfiguration(properties))
                .build();
    }

    /**
     * S3 存储实现
     *
     * @param s3Client   S3 客户端
     * @param s3Presigner 预签名器
     * @param properties 配置
     *
     * @return S3 存储实现
     */
    @Bean
    @ConditionalOnMissingBean
    public ObjectStorage s3ObjectStorage(S3Client s3Client, S3Presigner s3Presigner, OssProperties properties) {
        if (properties.getType() != StorageType.S3) {
            throw new ExtException(EmojiSymbol.OSS, "本配置只处理 ext.oss.type=S3");
        }
        return new S3ObjectStorage(s3Client, s3Presigner, properties);
    }

    private static StaticCredentialsProvider credentials(OssProperties properties) {
        if (isBlank(properties.getAccessKey()) || isBlank(properties.getSecretKey())) {
            throw new ExtException(EmojiSymbol.OSS, "ext.oss.type=S3 时必须配置 access-key 与 secret-key");
        }
        return StaticCredentialsProvider.create(
                AwsBasicCredentials.create(properties.getAccessKey(), properties.getSecretKey()));
    }

    private static URI endpoint(OssProperties properties) {
        return isBlank(properties.getEndpoint()) ? null : URI.create(properties.getEndpoint());
    }

    private static S3Configuration serviceConfiguration(OssProperties properties) {
        return S3Configuration.builder()
                .pathStyleAccessEnabled(properties.isPathStyleAccess())
                .chunkedEncodingEnabled(properties.isChunkedEncoding())
                .build();
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
