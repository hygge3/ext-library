package ext.library.captcha.service;

import ext.library.captcha.model.CaptchaResponse;
import org.springframework.util.FastByteArrayOutputStream;

import java.io.OutputStream;
import java.util.Base64;
import java.util.UUID;

/**
 * 验证码服务 - 核心接口
 */
public interface CaptchaService {

    /**
     * 生成验证码
     *
     * @param uuid         自定义缓存的 uuid
     * @param outputStream OutputStream
     */
    void generate(String uuid, OutputStream outputStream);

    /**
     * 生成验证码字节数组
     *
     * @param uuid 自定义缓存的 uuid
     * @return bytes
     */
    default byte[] generateBytes(String uuid) {
        FastByteArrayOutputStream outputStream = new FastByteArrayOutputStream();
        this.generate(uuid, outputStream);
        return outputStream.toByteArray();
    }

    /**
     * 生成验证码 base64 字符串
     *
     * @param uuid 自定义缓存的 uuid
     * @return base64 图片
     */
    default String generateBase64(String uuid) {
        FastByteArrayOutputStream outputStream = new FastByteArrayOutputStream();
        this.generate(uuid, outputStream);
        return "data:image/jpeg;base64," + Base64.getEncoder().encodeToString(outputStream.toByteArray());
    }

    /**
     * 生成验证码 CaptchaResponse
     *
     * @return CaptchaResponse
     */
    default CaptchaResponse generateCaptchaResponse() {
        return generateCaptchaResponse(UUID.randomUUID().toString());
    }

    /**
     * 生成验证码 CaptchaResponse
     *
     * @param uuid 自定义缓存的 uuid
     * @return CaptchaResponse
     */
    default CaptchaResponse generateCaptchaResponse(String uuid) {
        return new CaptchaResponse(uuid, this.generateBase64(uuid));
    }

    /**
     * 校验验证码
     *
     * @param uuid             自定义缓存的 uuid
     * @param userInputCaptcha 用户输入的图形验证码
     * @return 是否校验成功
     */
    boolean validate(String uuid, String userInputCaptcha);
}
