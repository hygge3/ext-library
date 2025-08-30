package ext.library.security.enums;

import ext.library.security.repository.SecurityRamRepository;
import ext.library.security.repository.SecurityRedisRepository;
import ext.library.security.repository.SecurityRepository;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 安全存储库
 */
@Getter
@RequiredArgsConstructor
public enum SecurityRepositoryEnum {

    /** redis */
    REDIS(new SecurityRedisRepository()),
    /** 内存 */
    RAM(new SecurityRamRepository()),
    ;
    final SecurityRepository securityRepository;
}