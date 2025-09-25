package ext.library.security.enums;

import ext.library.security.repository.SecurityRamRepository;
import ext.library.security.repository.SecurityRedisRepository;
import ext.library.security.repository.SecurityRepository;

/**
 * 安全存储库
 */
public enum SecurityRepositoryEnum {

    /** redis */
    REDIS(new SecurityRedisRepository()),
    /** 内存 */
    RAM(new SecurityRamRepository()),
    ;
    final SecurityRepository securityRepository;

    SecurityRepositoryEnum(SecurityRepository securityRepository) {
        this.securityRepository = securityRepository;
    }

    public SecurityRepository getSecurityRepository() {
        return securityRepository;
    }
}