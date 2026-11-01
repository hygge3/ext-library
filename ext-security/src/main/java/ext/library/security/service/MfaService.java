package ext.library.security.service;

import ext.library.security.domain.MfaCredential;
import ext.library.security.domain.MfaSetup;
import ext.library.security.exception.UnauthorizedException;
import ext.library.security.properties.MfaProperties;
import ext.library.security.repository.MfaCredentialRepository;
import ext.library.security.util.TotpUtil;
import ext.library.tool.util.StringUtil;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;

/**
 * 两步验证服务（TOTP + 恢复码）
 * <p>
 * 只提供验证原语，登录流程由业务编排：密码校验通过后询问 {@link #isEnabled(String)}，
 * 已绑定则要求用户提交验证码并调用 {@link #verify(String, String)} 通过后再 {@code doLogin}。
 *
 * @since 4.1.1
 */
public class MfaService {

    /**
     * 恢复码长度，用于与 6 位 TOTP 验证码区分
     */
    private static final int RECOVERY_CODE_LENGTH = 10;

    /**
     * 去掉了 0/O/1/I 等易混字符
     */
    private static final String RECOVERY_ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";

    private static final Duration enabledExpireTime = Duration.ofDays(365);

    private static final SecureRandom RANDOM = new SecureRandom();

    private final MfaCredentialRepository repository;
    private final MfaProperties properties;

    public MfaService(MfaCredentialRepository repository, MfaProperties properties) {
        this.repository = repository;
        this.properties = properties;
    }

    /**
     * 发起绑定：生成 secret 并返回二维码 URI，此时尚未生效
     *
     * @param loginId 登录 ID
     *
     * @return secret 与 otpauth URI
     */
    public MfaSetup beginSetup(String loginId) {
        String secret = TotpUtil.generateSecret();

        MfaCredential credential = new MfaCredential();
        credential.setLoginId(loginId);
        credential.setSecret(secret);
        credential.setEnabled(false);
        credential.setCreateTime(LocalDateTime.now());
        repository.saveMfaCredential(credential, properties.getSetupTimeout());

        return new MfaSetup(secret, TotpUtil.buildUri(properties.getIssuer(), loginId, secret));
    }

    /**
     * 确认绑定：校验一次验证码后启用，并返回明文恢复码（仅此一次）
     *
     * @param loginId 登录 ID
     * @param code    验证器上的 6 位验证码
     *
     * @return 恢复码明文
     */
    public List<String> confirmSetup(String loginId, String code) {
        MfaCredential credential = repository.getMfaCredential(loginId);
        if (credential == null) {
            throw new UnauthorizedException("未发起两步验证绑定");
        }
        if (credential.isEnabled()) {
            throw new UnauthorizedException("两步验证已绑定");
        }
        if (!TotpUtil.verify(credential.getSecret(), code, properties.getWindow())) {
            throw new UnauthorizedException("验证码错误");
        }

        List<String> recoveryCodes = generateRecoveryCodes();
        credential.setEnabled(true);
        credential.setRecoveryCodes(recoveryCodes.stream().map(MfaService::hash).toList());
        repository.saveMfaCredential(credential, enabledExpireTime);
        return recoveryCodes;
    }

    /**
     * 是否已绑定并启用两步验证
     *
     * @param loginId 登录 ID
     *
     * @return true 已启用
     */
    public boolean isEnabled(String loginId) {
        MfaCredential credential = repository.getMfaCredential(loginId);
        return credential != null && credential.isEnabled();
    }

    /**
     * 校验验证码，TOTP 与恢复码都接受；恢复码用过即失效
     *
     * @param loginId 登录 ID
     * @param code    6 位 TOTP 验证码或 10 位恢复码
     *
     * @return true 校验通过
     */
    public boolean verify(String loginId, String code) {
        if (StringUtil.isBlank(code)) {
            return false;
        }
        MfaCredential credential = repository.getMfaCredential(loginId);
        if (credential == null || !credential.isEnabled()) {
            return false;
        }
        if (code.length() == RECOVERY_CODE_LENGTH) {
            return consumeRecoveryCode(credential, code);
        }
        return TotpUtil.verify(credential.getSecret(), code, properties.getWindow());
    }

    /**
     * 解绑（需先通过一次验证码校验）
     *
     * @param loginId 登录 ID
     * @param code    6 位 TOTP 验证码或 10 位恢复码
     */
    public void disable(String loginId, String code) {
        if (!verify(loginId, code)) {
            throw new UnauthorizedException("验证码错误");
        }
        repository.removeMfaCredential(loginId);
    }

    // region private methods

    /**
     * 消费一次性恢复码
     * <p>
     * shortcut: 读-改-写非原子，两个并发请求可能同时通过同一个恢复码，
     * 需要严格一次性时改为存储层的原子删除。
     */
    private boolean consumeRecoveryCode(MfaCredential credential, String code) {
        List<String> remaining = new ArrayList<>(credential.getRecoveryCodes());
        if (!remaining.remove(hash(code.strip().toUpperCase()))) {
            return false;
        }
        credential.setRecoveryCodes(remaining);
        repository.saveMfaCredential(credential, enabledExpireTime);
        return true;
    }

    private List<String> generateRecoveryCodes() {
        int count = Math.max(1, properties.getRecoveryCodeCount());
        List<String> codes = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            StringBuilder sb = new StringBuilder(RECOVERY_CODE_LENGTH);
            for (int j = 0; j < RECOVERY_CODE_LENGTH; j++) {
                sb.append(RECOVERY_ALPHABET.charAt(RANDOM.nextInt(RECOVERY_ALPHABET.length())));
            }
            codes.add(sb.toString());
        }
        return codes;
    }

    private static String hash(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    // endregion
}
