package ext.library.security.domain;

/**
 * 两步验证绑定信息
 *
 * @param secret     Base32 secret
 * @param otpauthUri 绑定 URI，前端渲染成二维码交给验证器扫描
 *
 * @since 4.1.1
 */
public record MfaSetup(String secret, String otpauthUri) {

}
