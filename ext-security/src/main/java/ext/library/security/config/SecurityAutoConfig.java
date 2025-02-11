package ext.library.security.config;

import ext.library.security.authority.SecurityAuthority;
import ext.library.security.config.properties.SecurityProperties;
import ext.library.security.enums.SecurityRepositoryEnum;
import ext.library.security.repository.SecurityRamRepository;
import ext.library.security.repository.SecurityRedisRepository;
import ext.library.security.repository.SecurityRepository;
import ext.library.security.service.SecurityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * 自动配置
 */
@RequiredArgsConstructor
@AutoConfiguration
@EnableConfigurationProperties(SecurityProperties.class)
@Slf4j
public class SecurityAutoConfig {

    private final SecurityProperties securityProperties;

    /**
     * 权限服务注入
     *
     * @return {@code SecurityService }
     */
    @Bean
    public SecurityService securityService() {
        return new SecurityService() {
        };
    }

    /**
     * 权限存储注入
     *
     * @return {@code SecurityRepository }
     */
    @Bean
    public SecurityRepository securityRepository() {
        if (SecurityRepositoryEnum.REDIS.equals(securityProperties.getRepository())) {
            return new SecurityRedisRepository();
        } else {
            return new SecurityRamRepository();
        }
    }

    /**
     * 权限对象注入
     *
     * @return {@code SecurityAuthority }
     */
    @Bean
    @ConditionalOnMissingBean(SecurityAuthority.class)
    public SecurityAuthority securityAuthority() {
        log.warn("[🛡️] 将使用默认权限认证接口，所有权限皆无，建议实现 SecurityAuthority 接口中的业务逻辑！");
        return new SecurityAuthority() {};
    }

}
