package ext.library.captcha.service;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

/**
 * 验证码服务 - Web 扩展接口
 * <p>
 * 提供与 Spring Web 集成的便捷方法
 */
public interface CaptchaWebService extends CaptchaService {

    /**
     * 生成验证码 ByteArrayResource
     *
     * @param uuid 自定义缓存的 uuid
     *
     * @return ByteArrayResource
     */
    default ByteArrayResource generateByteResource(String uuid) {
        return new ByteArrayResource(this.generateBytes(uuid));
    }

    /**
     * 生成验证码 ResponseEntity
     *
     * @param uuid captcha uuid
     *
     * @return ResponseEntity
     */
    default ResponseEntity<Resource> generateResponseEntity(String uuid) {
        return new ResponseEntity<>(this.generateByteResource(uuid), getCaptchaHeaders(), HttpStatus.OK);
    }

    /**
     * 获取验证码响应头，避免验证码被缓存
     *
     * @return HttpHeaders
     */
    default HttpHeaders getCaptchaHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setPragma("no-cache");
        headers.setCacheControl("no-cache");
        headers.setExpires(0);
        headers.setContentType(MediaType.IMAGE_JPEG);
        return headers;
    }
}
