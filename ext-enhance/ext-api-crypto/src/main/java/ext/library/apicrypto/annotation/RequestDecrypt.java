package ext.library.apicrypto.annotation;

import ext.library.apicrypto.enums.Algorithm;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 请求解密注解
 * <p>
 * 标记在 Controller 方法或类上，自动对请求体进行解密处理。
 * <p>
 * 使用示例：
 * <pre>{@code
 * @PostMapping("/login")
 * @RequestDecrypt
 * public R<LoginVO> login(@RequestBody LoginDTO dto) {
 *     // dto 已自动解密
 *     return R.ok(authService.login(dto));
 * }
 *
 * // 指定算法
 * @PostMapping("/data")
 * @RequestDecrypt(algorithm = Algorithm.AES)
 * public R<DataVO> handleData(@RequestBody DataDTO dto) {
 *     return R.ok(dataService.handle(dto));
 * }
 * }</pre>
 *
 * @since 4.0.0
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface RequestDecrypt {

    /**
     * 指定解密算法
     * <p>
     * 如果不指定，则使用配置文件中 {@code ext.api-crypto.algorithm} 的默认值。
     *
     * @return 解密算法，默认为 {@link Algorithm#RSA}
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
