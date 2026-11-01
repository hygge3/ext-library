package ext.library.security.service;

import com.webauthn4j.WebAuthnAuthenticationManager;
import com.webauthn4j.WebAuthnRegistrationManager;
import com.webauthn4j.converter.AttestedCredentialDataConverter;
import com.webauthn4j.converter.util.ObjectConverter;
import com.webauthn4j.credential.CredentialRecord;
import com.webauthn4j.credential.CredentialRecordImpl;
import com.webauthn4j.data.AuthenticationData;
import com.webauthn4j.data.AuthenticationParameters;
import com.webauthn4j.data.AuthenticatorTransport;
import com.webauthn4j.data.RegistrationData;
import com.webauthn4j.data.RegistrationParameters;
import com.webauthn4j.data.attestation.authenticator.AttestedCredentialData;
import com.webauthn4j.data.attestation.authenticator.AuthenticatorData;
import com.webauthn4j.data.client.Origin;
import com.webauthn4j.data.client.challenge.DefaultChallenge;
import com.webauthn4j.server.ServerProperty;
import ext.library.cache.strategy.CacheStrategy;
import ext.library.security.domain.PasskeyAuthenticationOptions;
import ext.library.security.domain.PasskeyCredential;
import ext.library.security.domain.PasskeyRegistrationOptions;
import ext.library.security.exception.UnauthorizedException;
import ext.library.security.properties.PasskeyProperties;
import ext.library.security.repository.PasskeyCredentialRepository;
import ext.library.tool.util.Base64Util;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * 通行密钥服务（WebAuthn）
 * <p>
 * 只提供挑战与校验原语，登录流程由业务编排：认证成功返回 loginId，业务再 {@code doLogin}。
 *
 * @since 4.1.1
 */
public class PasskeyService {

    private static final String challengeCacheName = "security:passkey:challenge";
    private static final String registrationKeyPrefix = "reg:";
    private static final String authenticationKeyPrefix = "auth:";
    private static final int CHALLENGE_BYTES = 32;

    private static final SecureRandom RANDOM = new SecureRandom();

    private final PasskeyCredentialRepository repository;
    private final PasskeyProperties properties;
    private final CacheStrategy cacheStrategy;
    private final ObjectConverter objectConverter = new ObjectConverter();
    private final AttestedCredentialDataConverter attestedCredentialDataConverter =
        new AttestedCredentialDataConverter(objectConverter);
    private final WebAuthnRegistrationManager registrationManager =
        WebAuthnRegistrationManager.createNonStrictWebAuthnRegistrationManager(objectConverter);
    private final WebAuthnAuthenticationManager authenticationManager = new WebAuthnAuthenticationManager();

    public PasskeyService(PasskeyCredentialRepository repository, PasskeyProperties properties,
                          CacheStrategy cacheStrategy) {
        this.repository = repository;
        this.properties = properties;
        this.cacheStrategy = cacheStrategy.distributed();
    }

    /**
     * 发起注册，返回可直接下发给前端的注册参数
     *
     * @param loginId     登录 ID
     * @param userName    账号（显示在认证器里）
     * @param displayName 展示名
     *
     * @return 注册参数
     */
    public PasskeyRegistrationOptions beginRegistration(String loginId, String userName, String displayName) {
        byte[] challenge = newChallenge(registrationKeyPrefix + loginId);
        return new PasskeyRegistrationOptions(
            Base64Util.encodeUrlSafeToStr(challenge),
            new PasskeyRegistrationOptions.Rp(properties.getRpId(), properties.getRpName()),
            new PasskeyRegistrationOptions.User(
                Base64Util.encodeUrlSafeToStr(loginId.getBytes(StandardCharsets.UTF_8)), userName, displayName),
            List.of(new PasskeyRegistrationOptions.Param("public-key", -7),
                    new PasskeyRegistrationOptions.Param("public-key", -257)),
            properties.getChallengeTimeout().toMillis(),
            "none",
            new PasskeyRegistrationOptions.Selection(
                "preferred", properties.isUserVerificationRequired() ? "required" : "preferred"));
    }

    /**
     * 完成注册：校验认证器返回的注册响应并保存凭证
     *
     * @param loginId      登录 ID
     * @param responseJson {@code PublicKeyCredential} 的 JSON 序列化
     */
    public void finishRegistration(String loginId, String responseJson) {
        String challenge = consumeChallenge(registrationKeyPrefix + loginId);

        RegistrationData registrationData;
        try {
            registrationData = registrationManager.verify(responseJson,
                new RegistrationParameters(serverProperty(challenge), null, properties.isUserVerificationRequired()));
        } catch (RuntimeException e) {
            throw new UnauthorizedException("通行密钥注册失败：" + e.getMessage());
        }

        AuthenticatorData<?> authenticatorData = Objects.requireNonNull(
            Objects.requireNonNull(registrationData.getAttestationObject()).getAuthenticatorData());
        AttestedCredentialData attestedCredentialData = authenticatorData.getAttestedCredentialData();
        if (attestedCredentialData == null) {
            throw new UnauthorizedException("通行密钥注册失败：缺少凭证数据");
        }

        PasskeyCredential credential = new PasskeyCredential();
        credential.setLoginId(loginId);
        credential.setCredentialId(Base64Util.encodeUrlSafeToStr(attestedCredentialData.getCredentialId()));
        credential.setAttestedCredentialData(
            Base64Util.encodeUrlSafeToStr(attestedCredentialDataConverter.convert(attestedCredentialData)));
        credential.setCounter(authenticatorData.getSignCount());
        credential.setUvInitialized(authenticatorData.isFlagUV());
        credential.setBackupEligible(authenticatorData.isFlagBE());
        credential.setBackedUp(authenticatorData.isFlagBS());
        credential.setTransports(registrationData.getTransports() == null
                                     ? List.of()
                                     : registrationData.getTransports().stream().map(AuthenticatorTransport::getValue).toList());
        credential.setCreateTime(LocalDateTime.now());
        repository.savePasskeyCredential(credential);
    }

    /**
     * 发起认证，返回可直接下发给前端的认证参数
     *
     * @param loginId 登录 ID
     *
     * @return 认证参数；该账号未注册通行密钥时 allowCredentials 为空，由认证器自行选择可发现凭证
     */
    public PasskeyAuthenticationOptions beginAuthentication(String loginId) {
        byte[] challenge = newChallenge(authenticationKeyPrefix + loginId);
        List<PasskeyAuthenticationOptions.Descriptor> allowCredentials = repository.listPasskeyCredentials(loginId)
            .stream()
            .map(item -> new PasskeyAuthenticationOptions.Descriptor("public-key", item.getCredentialId(),
                item.getTransports()))
            .toList();
        return new PasskeyAuthenticationOptions(
            Base64Util.encodeUrlSafeToStr(challenge),
            properties.getRpId(),
            allowCredentials,
            properties.getChallengeTimeout().toMillis(),
            properties.isUserVerificationRequired() ? "required" : "preferred");
    }

    /**
     * 完成认证：校验断言，成功则返回凭证所属的登录 ID
     *
     * @param loginId      登录 ID；传 null 表示不限定账号（可发现凭证 / 免用户名登录）
     * @param responseJson {@code PublicKeyCredential} 的 JSON 序列化
     *
     * @return 登录 ID
     */
    public String finishAuthentication(String loginId, String responseJson) {
        AuthenticationData parsed;
        try {
            parsed = authenticationManager.parse(responseJson);
        } catch (RuntimeException e) {
            throw new UnauthorizedException("通行密钥认证失败：" + e.getMessage());
        }

        byte[] credentialId = parsed.getCredentialId();
        if (credentialId == null) {
            throw new UnauthorizedException("通行密钥认证失败：缺少凭证 ID");
        }
        PasskeyCredential credential = repository.getPasskeyCredential(Base64Util.encodeUrlSafeToStr(credentialId));
        if (credential == null) {
            throw new UnauthorizedException("通行密钥认证失败：凭证未注册");
        }
        if (loginId != null && !loginId.equals(credential.getLoginId())) {
            throw new UnauthorizedException("通行密钥认证失败：凭证不属于该账号");
        }

        AuthenticatorData<?> authenticatorData;
        try {
            AuthenticationData authenticationData = authenticationManager.verify(responseJson,
                new AuthenticationParameters(serverProperty(consumeChallenge(authenticationKeyPrefix + credential.getLoginId())),
                    toCredentialRecord(credential), List.of(credentialId), properties.isUserVerificationRequired()));
            authenticatorData = Objects.requireNonNull(authenticationData.getAuthenticatorData());
        } catch (RuntimeException e) {
            throw new UnauthorizedException("通行密钥认证失败：" + e.getMessage());
        }

        credential.setCounter(authenticatorData.getSignCount());
        credential.setLastUsedTime(LocalDateTime.now());
        repository.savePasskeyCredential(credential);
        return credential.getLoginId();
    }

    /**
     * 查询某账号已注册的通行密钥
     *
     * @param loginId 登录 ID
     *
     * @return 凭证列表
     */
    public List<PasskeyCredential> listPasskeyCredentials(String loginId) {
        return repository.listPasskeyCredentials(loginId);
    }

    /**
     * 删除通行密钥
     *
     * @param loginId      登录 ID
     * @param credentialId 凭证 ID（Base64URL）
     */
    public void removePasskeyCredential(String loginId, String credentialId) {
        repository.removePasskeyCredential(loginId, credentialId);
    }

    // region private methods

    private byte[] newChallenge(String key) {
        byte[] challenge = new byte[CHALLENGE_BYTES];
        RANDOM.nextBytes(challenge);
        cacheStrategy.put(challengeCacheName, key, Base64Util.encodeUrlSafeToStr(challenge),
            properties.getChallengeTimeout());
        return challenge;
    }

    /**
     * 取出并作废挑战，一个挑战只能用一次
     */
    private String consumeChallenge(String key) {
        String challenge = cacheStrategy.get(challengeCacheName, key, String.class);
        if (challenge == null) {
            throw new UnauthorizedException("通行密钥挑战已失效，请重新发起");
        }
        cacheStrategy.evict(challengeCacheName, key);
        return challenge;
    }

    private ServerProperty serverProperty(String challenge) {
        Set<Origin> origins = new LinkedHashSet<>();
        if (properties.getOrigins().isEmpty()) {
            origins.add(Origin.create("https://" + properties.getRpId()));
        } else {
            properties.getOrigins().forEach(origin -> origins.add(Origin.create(origin)));
        }
        return ServerProperty.builder()
            .origins(origins)
            .rpId(properties.getRpId())
            .challenge(new DefaultChallenge(challenge))
            .build();
    }

    private CredentialRecord toCredentialRecord(PasskeyCredential credential) {
        AttestedCredentialData attestedCredentialData = attestedCredentialDataConverter.convert(
            Base64Util.decodeUrlSafe(credential.getAttestedCredentialData()));
        Set<AuthenticatorTransport> transports = new LinkedHashSet<>();
        credential.getTransports().forEach(item -> transports.add(AuthenticatorTransport.create(item)));
        return new CredentialRecordImpl(null, credential.getUvInitialized(), credential.getBackupEligible(),
            credential.getBackedUp(), credential.getCounter(), attestedCredentialData, null, null, null, transports);
    }

    // endregion
}
