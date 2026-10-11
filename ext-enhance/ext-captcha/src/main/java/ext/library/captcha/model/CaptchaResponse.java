package ext.library.captcha.model;

/**
 * 验证码响应模型
 *
 * @param uuid   验证码唯一标识
 * @param base64 验证码图片 base64 编码
 */
public record CaptchaResponse(String uuid, String base64) {
}
