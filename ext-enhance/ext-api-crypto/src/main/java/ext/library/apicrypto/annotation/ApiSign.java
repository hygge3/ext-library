package ext.library.apicrypto.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 接口签名注解
 * <p>
 * 标注在 Controller 方法或类上，声明该接口需要校验请求签名与防重放。
 * 请求方需携带三个请求头：
 * <ul>
 *   <li>{@code X-Timestamp} — 请求时间戳（秒）</li>
 *   <li>{@code X-Nonce} — 随机数，时间窗内不得重复</li>
 *   <li>{@code X-Sign} — {@code HMAC-SHA256(secret, method + "\n" + uri + "\n" + query + "\n" + timestamp + "\n" + nonce + "\n" + body)} 的十六进制值（大小写不敏感）；
 *       {@code uri} 为含 context-path 的请求路径，{@code query} 为未解码的查询串（无参数时为空串）；
 *       不绑方法、路径与查询串的签名能被截获后改成任意参数</li>
 * </ul>
 * <p>
 * 需要配置 {@code ext.api-crypto.sign.secret}，并开启
 * {@code ext.servlet.repeatable-request.enabled=true}（签名要读请求体）。
 *
 * <pre>{@code
 * @ApiSign
 * public R<?> createOrder(@RequestBody Order order) { ... }
 * }</pre>
 *
 * @since 4.1.0
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ApiSign {

}
