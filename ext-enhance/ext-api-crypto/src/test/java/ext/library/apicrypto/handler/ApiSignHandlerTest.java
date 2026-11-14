package ext.library.apicrypto.handler;

import ext.library.apicrypto.annotation.ApiSign;
import ext.library.apicrypto.properties.ApiCryptoProperties;
import ext.library.apicrypto.store.NonceStore;
import ext.library.crypto.DigestUtil;
import ext.library.servlet.util.RepeatableRequestWrapper;
import ext.library.tool.exception.ExtException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.method.HandlerMethod;

import java.lang.reflect.Method;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * 接口签名拦截器测试
 */
@DisplayName("接口签名拦截器测试")
class ApiSignHandlerTest {

    private static final String SECRET = "test-secret";

    private static final String BODY = "{\"a\":1}";

    private static final String METHOD = "POST";

    private static final String URI = "/test/signed";

    static class Controller {

        @ApiSign
        public void signed() {
        }

        public void open() {
        }
    }

    private static ApiCryptoProperties properties() {
        ApiCryptoProperties properties = new ApiCryptoProperties();
        properties.getSign().setSecret(SECRET);
        return properties;
    }

    private static HandlerMethod handler(String name) throws NoSuchMethodException {
        Method method = Controller.class.getDeclaredMethod(name);
        return new HandlerMethod(new Controller(), method);
    }

    /**
     * 每个用例用独立的内存随机数存储，避免用例间串味
     */
    private static NonceStore nonceStore() {
        Set<String> used = ConcurrentHashMap.newKeySet();
        return (nonce, ttl) -> used.add(nonce);
    }

    private static RepeatableRequestWrapper request(long timestamp, String nonce, String sign) throws Exception {
        return request(null, timestamp, nonce, sign);
    }

    private static RepeatableRequestWrapper request(String query, long timestamp, String nonce, String sign) throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod(METHOD);
        request.setRequestURI(URI);
        request.setQueryString(query);
        request.setContent(BODY.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        request.addHeader("X-Timestamp", String.valueOf(timestamp));
        request.addHeader("X-Nonce", nonce);
        request.addHeader("X-Sign", sign);
        return new RepeatableRequestWrapper(request);
    }

    private static String sign(long timestamp, String nonce) {
        return sign(null, timestamp, nonce);
    }

    private static String sign(String query, long timestamp, String nonce) {
        String queryString = query == null ? "" : query;
        return DigestUtil.hmac("HmacSHA256", SECRET,
                METHOD + "\n" + URI + "\n" + queryString + "\n" + timestamp + "\n" + nonce + "\n" + BODY);
    }

    @Test
    @DisplayName("正确签名通过")
    void validSignaturePasses() throws Exception {
        ApiSignHandler handler = new ApiSignHandler(nonceStore(), properties());
        long now = System.currentTimeMillis() / 1000;
        String nonce = "nonce-1";

        assertDoesNotThrow(() -> handler.preHandle(
                request(now, nonce, sign(now, nonce)), new MockHttpServletResponse(), handler("signed")));
    }

    @Test
    @DisplayName("无注解的接口直接放行")
    void unsignedMethodPasses() throws Exception {
        ApiSignHandler handler = new ApiSignHandler(nonceStore(), properties());

        assertDoesNotThrow(() -> handler.preHandle(
                new MockHttpServletRequest(), new MockHttpServletResponse(), handler("open")));
    }

    @Test
    @DisplayName("签名不匹配被拒绝")
    void wrongSignatureRejected() throws Exception {
        ApiSignHandler handler = new ApiSignHandler(nonceStore(), properties());
        long now = System.currentTimeMillis() / 1000;

        assertThrows(ExtException.class, () -> handler.preHandle(
                request(now, "n1", "deadbeef"), new MockHttpServletResponse(), handler("signed")));
    }

    @Test
    @DisplayName("验签失败不占用 nonce")
    void wrongSignatureKeepsNonceUsable() throws Exception {
        ApiSignHandler handler = new ApiSignHandler(nonceStore(), properties());
        long now = System.currentTimeMillis() / 1000;
        String nonce = "nonce-retry";

        assertThrows(ExtException.class, () -> handler.preHandle(
                request(now, nonce, "deadbeef"), new MockHttpServletResponse(), handler("signed")));

        // 同一 nonce 的合法签名必须还能用，否则客户端重试会被误报成重放
        assertDoesNotThrow(() -> handler.preHandle(
                request(now, nonce, sign(now, nonce)), new MockHttpServletResponse(), handler("signed")));
    }

    @Test
    @DisplayName("随机数重复使用被拒绝（防重放）")
    void replayedNonceRejected() throws Exception {
        ApiSignHandler handler = new ApiSignHandler(nonceStore(), properties());
        long now = System.currentTimeMillis() / 1000;
        String nonce = "nonce-dup";
        String sign = sign(now, nonce);

        handler.preHandle(request(now, nonce, sign), new MockHttpServletResponse(), handler("signed"));

        assertThrows(ExtException.class, () -> handler.preHandle(
                request(now, nonce, sign), new MockHttpServletResponse(), handler("signed")));
    }

    @Test
    @DisplayName("查询串被篡改时验签不通过")
    void tamperedQueryRejected() throws Exception {
        ApiSignHandler handler = new ApiSignHandler(nonceStore(), properties());
        long now = System.currentTimeMillis() / 1000;
        String nonce = "nonce-query";

        // 客户端签的是 amount=100，中继把参数改成 amount=1
        assertThrows(ExtException.class, () -> handler.preHandle(
                request("amount=1", now, nonce, sign("amount=100", now, nonce)),
                new MockHttpServletResponse(), handler("signed")));
    }

    @Test
    @DisplayName("查询串一致时验签通过")
    void matchedQueryPasses() throws Exception {
        ApiSignHandler handler = new ApiSignHandler(nonceStore(), properties());
        long now = System.currentTimeMillis() / 1000;
        String nonce = "nonce-query-ok";

        assertDoesNotThrow(() -> handler.preHandle(
                request("amount=100&to=alice", now, nonce, sign("amount=100&to=alice", now, nonce)),
                new MockHttpServletResponse(), handler("signed")));
    }

    @Test
    @DisplayName("签名 hex 大写也通过")
    void uppercaseSignPasses() throws Exception {
        ApiSignHandler handler = new ApiSignHandler(nonceStore(), properties());
        long now = System.currentTimeMillis() / 1000;
        String nonce = "nonce-upper";

        assertDoesNotThrow(() -> handler.preHandle(
                request(now, nonce, sign(now, nonce).toUpperCase(java.util.Locale.ROOT)),
                new MockHttpServletResponse(), handler("signed")));
    }

    @Test
    @DisplayName("时间戳超出允许偏差被拒绝")
    void staleTimestampRejected() throws Exception {
        ApiSignHandler handler = new ApiSignHandler(nonceStore(), properties());
        long old = System.currentTimeMillis() / 1000 - 3600;
        String nonce = "nonce-old";

        assertThrows(ExtException.class, () -> handler.preHandle(
                request(old, nonce, sign(old, nonce)), new MockHttpServletResponse(), handler("signed")));
    }

    @Test
    @DisplayName("缺少请求头被拒绝")
    void missingHeadersRejected() throws Exception {
        ApiSignHandler handler = new ApiSignHandler(nonceStore(), properties());

        assertThrows(ExtException.class, () -> handler.preHandle(
                new MockHttpServletRequest(), new MockHttpServletResponse(), handler("signed")));
    }

}
