package ext.library.security.config;

import ext.library.cache.strategy.CacheStrategy;
import ext.library.security.authority.SecurityAuthority;
import ext.library.security.properties.SecurityProperties;
import ext.library.security.repository.SecurityCacheRepository;
import ext.library.security.repository.SecurityRepository;
import ext.library.security.service.SecurityService;
import ext.library.tool.constant.EmojiSymbol;
import ext.library.tool.runtime.Logs;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * 安全模块自动配置
 * <p>
 * 自动注入安全相关的核心组件，包括权限服务、存储仓库和权限校验接口。
 * 存储使用 ext-cache 模块的 CacheStrategy，支持多种缓存后端。
 *
 * @since 4.0.0
 */
@AutoConfiguration
@EnableConfigurationProperties(SecurityProperties.class)
public class SecurityAutoConfiguration {

    /**
     * 权限服务注入
     *
     * @return {@code SecurityService }
     */
    @Bean
    @ConditionalOnMissingBean(SecurityService.class)
    public SecurityService securityService() {
        return new SecurityService() {
        };
    }

    /**
     * 安全存储仓库注入
     * <p>
     * 使用 ext-cache 的 CacheStrategy 作为底层存储，
     * 支持 Caffeine、Redis、PostgreSQL、L2 等多种后端。
     *
     * @param cacheStrategy 缓存策略
     * @return {@code SecurityRepository }
     */
    @Bean
    @ConditionalOnMissingBean(SecurityRepository.class)
    public SecurityRepository securityRepository(CacheStrategy cacheStrategy) {
        return new SecurityCacheRepository(cacheStrategy);
    }

    /**
     * 权限校验接口注入
     *
     * @return {@code SecurityAuthority }
     */
    @Bean
    @ConditionalOnMissingBean(SecurityAuthority.class)
    public SecurityAuthority securityAuthority() {
        Logs.warn(EmojiSymbol.SECURITY, "将使用默认权限认证接口，所有权限皆无，建议实现 SecurityAuthority 接口中的业务逻辑");
        return new SecurityAuthority() {
        };
    }

}
