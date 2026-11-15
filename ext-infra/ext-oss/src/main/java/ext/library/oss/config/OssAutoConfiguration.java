package ext.library.oss.config;

import ext.library.oss.LocalObjectStorage;
import ext.library.oss.ObjectStorage;
import ext.library.oss.OssTemplate;
import ext.library.oss.util.ImageUtil;
import ext.library.oss.util.ThumbnailEncoder;
import ext.library.tool.constant.EmojiSymbol;
import ext.library.tool.runtime.Logs;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * 对象存储自动配置
 * <p>
 * 本类只处理本地存储。S3 的 Bean 在 {@link S3ObjectStorageConfiguration}，那里用
 * {@code @ConditionalOnClass} 挡住未引 AWS SDK 的场景——本类的方法签名里不能出现厂商类型，
 * 否则 Spring 反射方法时会因类型缺失直接报 NoClassDefFoundError。
 *
 * @since 4.1.0
 */
@AutoConfiguration
@EnableConfigurationProperties(OssProperties.class)
@ConditionalOnProperty(prefix = OssProperties.PREFIX, name = "enabled", havingValue = "true",
        matchIfMissing = true)
public class OssAutoConfiguration {

    /**
     * 注册业务方注入的缩略图编码器，同格式覆盖内置实现
     *
     * @param encoders 容器里的编码器
     *
     * @return 初始化回调
     */
    @Bean
    public SmartInitializingSingleton thumbnailEncoderRegistrar(ObjectProvider<ThumbnailEncoder> encoders) {
        return () -> encoders.orderedStream().forEach(ImageUtil::registerEncoder);
    }

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
     * 本地存储实现
     * <p>
     * S3 实现由 {@link S3ObjectStorageConfiguration} 提供，两处都标 {@code @ConditionalOnMissingBean}，
     * 业务方自定义 {@link ObjectStorage} 时同时失效。
     *
     * @param properties 配置
     *
     * @return 本地存储实现
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = OssProperties.PREFIX, name = "type", havingValue = "LOCAL", matchIfMissing = true)
    public ObjectStorage localObjectStorage(OssProperties properties) {
        return new LocalObjectStorage(properties);
    }

}
