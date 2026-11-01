package ext.library.security.domain;

import java.util.List;

/**
 * 通行密钥认证参数，直接作为 {@code navigator.credentials.get()} 的 publicKey 下发
 *
 * @param challenge        挑战（Base64URL）
 * @param rpId             依赖方 ID
 * @param allowCredentials 允许的凭证列表，空列表表示由认证器自行选择可发现凭证
 * @param timeout          超时（毫秒）
 * @param userVerification 用户验证要求
 *
 * @since 4.1.1
 */
public record PasskeyAuthenticationOptions(
    String challenge,
    String rpId,
    List<Descriptor> allowCredentials,
    long timeout,
    String userVerification) {

    /**
     * 凭证描述符
     *
     * @param type       固定 public-key
     * @param id         凭证 ID（Base64URL）
     * @param transports 传输方式
     */
    public record Descriptor(String type, String id, List<String> transports) {
    }

}
