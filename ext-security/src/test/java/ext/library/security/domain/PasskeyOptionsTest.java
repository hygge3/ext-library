package ext.library.security.domain;

import ext.library.json.util.JsonUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 这两个 record 是直接下发给浏览器的 WebAuthn 参数，字段名必须和规范一致。
 */
@DisplayName("通行密钥参数")
class PasskeyOptionsTest {

    @Test
    @DisplayName("注册参数按 WebAuthn 规范序列化")
    void registrationOptionsJson() {
        PasskeyRegistrationOptions options = new PasskeyRegistrationOptions(
            "challenge-value",
            new PasskeyRegistrationOptions.Rp("example.com", "Example"),
            new PasskeyRegistrationOptions.User("dXNlcg", "alice", "Alice"),
            List.of(new PasskeyRegistrationOptions.Param("public-key", -7)),
            60000L,
            "none",
            new PasskeyRegistrationOptions.Selection("preferred", "required"));

        String json = JsonUtil.toJson(options);

        assertThat(json)
            .contains("\"challenge\":\"challenge-value\"")
            .contains("\"rp\":{\"id\":\"example.com\",\"name\":\"Example\"}")
            .contains("\"user\":{\"id\":\"dXNlcg\",\"name\":\"alice\",\"displayName\":\"Alice\"}")
            .contains("\"pubKeyCredParams\":[{\"type\":\"public-key\",\"alg\":-7}]")
            .contains("\"authenticatorSelection\":{\"residentKey\":\"preferred\",\"userVerification\":\"required\"}");
    }

    @Test
    @DisplayName("认证参数按 WebAuthn 规范序列化")
    void authenticationOptionsJson() {
        PasskeyAuthenticationOptions options = new PasskeyAuthenticationOptions(
            "challenge-value",
            "example.com",
            List.of(new PasskeyAuthenticationOptions.Descriptor("public-key", "cred-id", List.of("internal"))),
            60000L,
            "preferred");

        String json = JsonUtil.toJson(options);

        assertThat(json)
            .contains("\"rpId\":\"example.com\"")
            .contains("\"allowCredentials\":[{\"type\":\"public-key\",\"id\":\"cred-id\",\"transports\":[\"internal\"]}]");
    }

}
