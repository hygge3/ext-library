package ext.library.captcha.service;

import ext.library.captcha.vo.CaptchaVO;
import java.io.OutputStream;
import java.util.Base64;
import java.util.UUID;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.FastByteArrayOutputStream;

/**
 * 验证码服务
 */
public interface ICaptchaService {

    /**
     * 生成验证码
     *
     * @param uuid         自定义缓存的 uuid
     * @param outputStream OutputStream
     */
    void generate(String uuid, OutputStream outputStream);

    /**
     * 生成验证码
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
     * 生成验证码
     *
     * @param uuid 自定义缓存的 uuid
     * @return ByteArrayResource
     */
    default ByteArrayResource generateByteResource(String uuid) {
        return new ByteArrayResource(this.generateBytes(uuid));
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
     * 生成验证码 base64 CaptchaVo
     *
     * @return CaptchaVo
     */
    default CaptchaVO generateBase64Vo() {
        return generateBase64Vo(UUID.randomUUID().toString());
    }

    /**
     * 生成验证码 base64 CaptchaVo
     *
     * @param uuid 自定义缓存的 uuid
     * @return CaptchaVo
     */
    default CaptchaVO generateBase64Vo(String uuid) {
        return new CaptchaVO(uuid, this.generateBase64(uuid));
    }

    /**
     * 生成验证码
     *
     * @param uuid captcha uuid
     * @return {ResponseEntity}
     */
    default ResponseEntity<Resource> generateResponseEntity(String uuid) {
        return new ResponseEntity<>(this.generateByteResource(uuid), this.getCaptchaHeaders(), HttpStatus.OK);
    }

    /**
     * 图片输出的头，避免验证码被缓存
     *
     * @return {HttpHeaders}
     */
    default HttpHeaders getCaptchaHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setPragma("no-cache");
        headers.setCacheControl("no-cache");
        headers.setExpires(0);
        headers.setContentType(MediaType.IMAGE_JPEG);
        return headers;
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
