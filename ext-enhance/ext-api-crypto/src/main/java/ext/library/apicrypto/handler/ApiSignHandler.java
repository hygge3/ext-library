package ext.library.apicrypto.handler;

import ext.library.apicrypto.annotation.ApiSign;
import ext.library.apicrypto.properties.ApiCryptoProperties;
import ext.library.apicrypto.store.NonceStore;
import ext.library.crypto.DigestUtil;
import ext.library.servlet.util.RepeatableRequestWrapper;
import ext.library.tool.constant.EmojiSymbol;
import ext.library.tool.exception.ExtException;
import ext.library.tool.util.ClassUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.Locale;

/**
 * 接口签名拦截器
 * <p>
 * 校验请求头 {@code X-Timestamp} / {@code X-Nonce} / {@code X-Sign}：
 * 时间戳必须在允许偏差内，签名必须匹配，随机数在时间窗内不得重复。
 * 签名覆盖 {@code method + "\n" + uri + "\n" + query + "\n" + timestamp + "\n" + nonce + "\n" + body}：
 * 不绑方法和路径的话，一个被截获的签名能直接打到任何 body 相同的接口上；不带查询串的话，
 * 一个 GET 接口的 {@code ?amount=100} 能被改成任意值而验签照样通过。
 * 因此需要 {@code ext.servlet.repeatable-request.enabled=true} 才能读到请求体。
 *
 * @since 4.1.0
 */
public class ApiSignHandler implements HandlerInterceptor {

    /**
     * 时间戳请求头
     */
    private static final String TIMESTAMP_HEADER = "X-Timestamp";

    /**
     * 随机数请求头
     */
    private static final String NONCE_HEADER = "X-Nonce";

    /**
     * 签名请求头
     */
    private static final String SIGN_HEADER = "X-Sign";

    /**
     * 签名算法
     */
    private static final String HMAC_ALGORITHM = "HmacSHA256";

    private final NonceStore nonceStore;

    private final ApiCryptoProperties properties;

    public ApiSignHandler(NonceStore nonceStore, ApiCryptoProperties properties) {
        this.nonceStore = nonceStore;
        this.properties = properties;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }
        ApiSign apiSign = ClassUtil.getAnnotation(handlerMethod.getMethod(), ApiSign.class);
        if (apiSign == null) {
            return true;
        }

        ApiCryptoProperties.Sign sign = properties.getSign();
        if (isBlank(sign.getSecret())) {
            throw new ExtException(EmojiSymbol.API_CRYPTO, "接口签名已启用但未配置 ext.api-crypto.sign.secret");
        }

        String timestamp = request.getHeader(TIMESTAMP_HEADER);
        String nonce = request.getHeader(NONCE_HEADER);
        String signValue = request.getHeader(SIGN_HEADER);
        if (isBlank(timestamp) || isBlank(nonce) || isBlank(signValue)) {
            throw new ExtException(EmojiSymbol.API_CRYPTO, "缺少签名参数（X-Timestamp / X-Nonce / X-Sign）");
        }

        long maxSkew = sign.getMaxSkewSeconds();
        long seconds = parseTimestamp(timestamp);
        if (Math.abs(System.currentTimeMillis() / 1000 - seconds) > maxSkew) {
            throw new ExtException(EmojiSymbol.API_CRYPTO, "请求时间戳超出允许范围");
        }

        String expected = DigestUtil.hmac(HMAC_ALGORITHM, sign.getSecret(),
                request.getMethod() + "\n" + request.getRequestURI() + "\n" + queryString(request) + "\n"
                        + timestamp + "\n" + nonce + "\n" + readBody(request));
        // hex 大小写不敏感：客户端普遍能输出大写，逐字节比较会把人捻在"签名不匹配"上
        String actual = signValue.trim().toLowerCase(Locale.ROOT);
        if (!MessageDigest.isEqual(expected.getBytes(StandardCharsets.UTF_8),
                actual.getBytes(StandardCharsets.UTF_8))) {
            throw new ExtException(EmojiSymbol.API_CRYPTO, "签名不匹配");
        }

        // 验签之后才占 nonce：先占的话未认证请求就能灌满缓存，客户端重试还会被误报成重放
        if (!nonceStore.tryAcquire(nonce, Duration.ofSeconds(maxSkew))) {
            throw new ExtException(EmojiSymbol.API_CRYPTO, "随机数已使用（疑似重放）");
        }
        return true;
    }

    /**
     * 解析时间戳
     *
     * @param timestamp 时间戳字符串
     *
     * @return 秒级时间戳
     */
    private static long parseTimestamp(String timestamp) {
        try {
            return Long.parseLong(timestamp);
        } catch (NumberFormatException _) {
            throw new ExtException(EmojiSymbol.API_CRYPTO, "时间戳格式非法");
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    /**
     * 读取查询串
     * <p>
     * 按发送顺序原样参与签名，不做排序与解码：客户端签自己发出的串，服务端拿到的也是同一串。
     *
     * @param request 请求
     *
     * @return 查询串，无参数时为 ""
     */
    private static String queryString(HttpServletRequest request) {
        String query = request.getQueryString();
        return query == null ? "" : query;
    }

    /**
     * 读取请求体
     *
     * @param request 请求
     *
     * @return 请求体字符串
     */
    private static String readBody(HttpServletRequest request) {
        if (request instanceof RepeatableRequestWrapper wrapper) {
            return wrapper.getBodyAsString();
        }
        throw new ExtException(EmojiSymbol.API_CRYPTO,
                "接口签名需要读取请求体，请开启 ext.servlet.repeatable-request.enabled=true");
    }

}
