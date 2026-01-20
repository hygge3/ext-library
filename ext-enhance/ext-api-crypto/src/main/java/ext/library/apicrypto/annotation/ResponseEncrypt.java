package ext.library.apicrypto.annotation;

import ext.library.apicrypto.enums.Algorithm;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 响应加密注解
 * <p>
 * 标记在 Controller 方法或类上，自动对响应体进行加密处理。
 * <p>
 * 使用示例：
 * <pre>{@code
 * @GetMapping("/user/info")
 * @ResponseEncrypt
 * public R<UserVO> getUserInfo() {
 *     // 返回的数据会自动加密
 *     return R.ok(userService.getCurrentUser());
 * }
 *
 * // 指定算法
 * @GetMapping("/sensitive")
 * @ResponseEncrypt(algorithm = Algorithm.SM2)
 * public R<SensitiveVO> getSensitiveData() {
 *     return R.ok(sensitiveService.getData());
 * }
 * }</pre>
 *
 * @since 4.0.0
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface ResponseEncrypt {

    /**
     * 指定加密算法
     * <p>
     * 如果不指定，则使用配置文件中 {@code ext.api-crypto.algorithm} 的默认值。
     *
     * @return 加密算法，默认为 {@link Algorithm#RSA}
     */
    Algorithm algorithm() default Algorithm.RSA;

    /**
     * 是否使用配置文件中的默认算法
     * <p>
     * 当设置为 {@code true} 时，忽略 {@link #algorithm()} 属性，使用配置文件中的算法。
     *
     * @return 是否使用默认算法
     */
    boolean useDefault() default true;
}
