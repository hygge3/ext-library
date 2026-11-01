package ext.library.security.service;

import ext.library.security.domain.MfaCredential;
import ext.library.security.domain.MfaSetup;
import ext.library.security.exception.UnauthorizedException;
import ext.library.security.properties.MfaProperties;
import ext.library.security.repository.MfaCredentialRepository;
import ext.library.security.util.TotpUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("两步验证服务")
class MfaServiceTest {

    private static final String LOGIN_ID = "alice";

    private final InMemoryRepository repository = new InMemoryRepository();
    private final MfaService service = new MfaService(repository, new MfaProperties());

    @Test
    @DisplayName("绑定流程：发起 → 确认启用 → TOTP 与恢复码可校验，恢复码一次性")
    void setupAndVerify() {
        MfaSetup setup = service.beginSetup(LOGIN_ID);
        assertThat(setup.otpauthUri()).contains("totp").contains(setup.secret());
        // 还没确认，不算启用
        assertThat(service.isEnabled(LOGIN_ID)).isFalse();

        List<String> recoveryCodes = service.confirmSetup(LOGIN_ID, code(setup.secret()));
        assertThat(recoveryCodes).hasSize(new MfaProperties().getRecoveryCodeCount());
        assertThat(service.isEnabled(LOGIN_ID)).isTrue();

        assertThat(service.verify(LOGIN_ID, code(setup.secret()))).isTrue();
        assertThat(service.verify(LOGIN_ID, "000000")).isFalse();

        String recoveryCode = recoveryCodes.getFirst();
        assertThat(service.verify(LOGIN_ID, recoveryCode)).isTrue();
        assertThat(service.verify(LOGIN_ID, recoveryCode)).as("恢复码只能用一次").isFalse();
        assertThat(service.verify(LOGIN_ID, "ZZZZZZZZZZ")).isFalse();
    }

    @Test
    @DisplayName("未绑定的账号一律校验失败")
    void verifyWithoutCredential() {
        assertThat(service.isEnabled(LOGIN_ID)).isFalse();
        assertThat(service.verify(LOGIN_ID, "123456")).isFalse();
    }

    @Test
    @DisplayName("确认绑定时验证码错误则拒绝，且不启用")
    void confirmWithWrongCode() {
        service.beginSetup(LOGIN_ID);
        assertThatThrownBy(() -> service.confirmSetup(LOGIN_ID, "000000"))
            .isInstanceOf(UnauthorizedException.class);
        assertThat(service.isEnabled(LOGIN_ID)).isFalse();
    }

    @Test
    @DisplayName("未发起绑定就直接确认会报错")
    void confirmWithoutSetup() {
        assertThatThrownBy(() -> service.confirmSetup(LOGIN_ID, "000000"))
            .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    @DisplayName("解绑需要正确验证码")
    void disableRequiresCode() {
        MfaSetup setup = service.beginSetup(LOGIN_ID);
        service.confirmSetup(LOGIN_ID, code(setup.secret()));

        assertThatThrownBy(() -> service.disable(LOGIN_ID, "000000"))
            .isInstanceOf(UnauthorizedException.class);
        assertThat(service.isEnabled(LOGIN_ID)).isTrue();

        service.disable(LOGIN_ID, code(setup.secret()));
        assertThat(service.isEnabled(LOGIN_ID)).isFalse();
    }

    @Test
    @DisplayName("恢复码明文不落存储，只存摘要")
    void recoveryCodesAreHashed() {
        MfaSetup setup = service.beginSetup(LOGIN_ID);
        List<String> recoveryCodes = service.confirmSetup(LOGIN_ID, code(setup.secret()));

        MfaCredential stored = repository.getMfaCredential(LOGIN_ID);
        assertThat(stored.getRecoveryCodes()).noneMatch(recoveryCodes::contains);
        assertThat(stored.getRecoveryCodes()).allMatch(item -> item.length() == 64);
    }

    private static String code(String secret) {
        return TotpUtil.generateCode(secret, System.currentTimeMillis() / 1000);
    }

    /**
     * 内存仓库，语义与缓存实现一致（存副本）
     */
    private static class InMemoryRepository implements MfaCredentialRepository {

        private final Map<String, MfaCredential> store = new HashMap<>();

        @Override
        public MfaCredential getMfaCredential(String loginId) {
            MfaCredential credential = store.get(loginId);
            return credential == null ? null : credential.copy();
        }

        @Override
        public boolean saveMfaCredential(MfaCredential credential, Duration expireTime) {
            store.put(credential.getLoginId(), credential.copy());
            return true;
        }

        @Override
        public boolean removeMfaCredential(String loginId) {
            store.remove(loginId);
            return true;
        }
    }

}
