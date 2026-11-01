package ext.library.security.domain;

import java.util.List;

/**
 * 通行密钥注册参数，直接作为 {@code navigator.credentials.create()} 的 publicKey 下发
 * <p>
 * 字段名与 WebAuthn 规范一致（camelCase），不额外包一层。
 *
 * @param challenge             挑战（Base64URL）
 * @param rp                    依赖方信息
 * @param user                  用户信息
 * @param pubKeyCredParams      允许的算法，默认 ES256(-7) 与 RS256(-257)
 * @param timeout               超时（毫秒）
 * @param attestation           attestation 传递方式
 * @param authenticatorSelection 认证器选择条件
 *
 * @since 4.1.1
 */
public record PasskeyRegistrationOptions(
    String challenge,
    Rp rp,
    User user,
    List<Param> pubKeyCredParams,
    long timeout,
    String attestation,
    Selection authenticatorSelection) {

    /**
     * 依赖方
     *
     * @param id   rpId，通常是域名
     * @param name 展示名称
     */
    public record Rp(String id, String name) {
    }

    /**
     * 用户实体
     *
     * @param id          用户句柄（Base64URL）
     * @param name        账号
     * @param displayName 展示名
     */
    public record User(String id, String name, String displayName) {
    }

    /**
     * 算法参数
     *
     * @param type 固定 public-key
     * @param alg  COSE 算法标识（-7 = ES256，-257 = RS256）
     */
    public record Param(String type, int alg) {
    }

    /**
     * 认证器选择条件
     *
     * @param residentKey       是否要求可发现凭证
     * @param userVerification  用户验证要求
     */
    public record Selection(String residentKey, String userVerification) {
    }

}
