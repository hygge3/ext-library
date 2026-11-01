package ext.library.security.repository;

import ext.library.security.domain.MfaCredential;

import java.time.Duration;

/**
 * 两步验证凭证存储接口
 * <p>
 * 需要落业务库时实现本接口并注册成 Bean，默认走 ext-cache。
 *
 * @since 4.1.1
 */
public interface MfaCredentialRepository {

    /**
     * 查询两步验证凭证
     *
     * @param loginId 登录 ID
     *
     * @return 凭证，不存在返回 null
     */
    MfaCredential getMfaCredential(String loginId);

    /**
     * 保存两步验证凭证
     *
     * @param credential 凭证
     * @param expireTime 过期时长
     *
     * @return true 成功
     */
    boolean saveMfaCredential(MfaCredential credential, Duration expireTime);

    /**
     * 删除两步验证凭证
     *
     * @param loginId 登录 ID
     *
     * @return true 成功
     */
    boolean removeMfaCredential(String loginId);

}
