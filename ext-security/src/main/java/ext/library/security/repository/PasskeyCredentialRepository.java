package ext.library.security.repository;

import ext.library.security.domain.PasskeyCredential;

import java.util.List;

/**
 * 通行密钥凭证存储接口
 * <p>
 * 需要落业务库时实现本接口并注册成 Bean，默认走 ext-cache。
 *
 * @since 4.1.1
 */
public interface PasskeyCredentialRepository {

    /**
     * 查询某个账号下所有通行密钥
     *
     * @param loginId 登录 ID
     *
     * @return 凭证列表，无则空列表
     */
    List<PasskeyCredential> listPasskeyCredentials(String loginId);

    /**
     * 按凭证 ID 查询（断言校验时只知道凭证 ID）
     *
     * @param credentialId 凭证 ID（Base64URL）
     *
     * @return 凭证，不存在返回 null
     */
    PasskeyCredential getPasskeyCredential(String credentialId);

    /**
     * 保存通行密钥凭证
     *
     * @param credential 凭证
     *
     * @return true 成功
     */
    boolean savePasskeyCredential(PasskeyCredential credential);

    /**
     * 删除通行密钥凭证
     *
     * @param loginId      登录 ID
     * @param credentialId 凭证 ID（Base64URL）
     *
     * @return true 成功
     */
    boolean removePasskeyCredential(String loginId, String credentialId);

}
