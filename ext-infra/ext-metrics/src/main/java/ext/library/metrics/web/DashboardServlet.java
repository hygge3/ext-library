package ext.library.metrics.web;

import ext.library.json.util.JsonUtil;
import ext.library.metrics.core.MetricsRegistry;
import ext.library.metrics.properties.MetricsProperties;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Set;

/**
 * 指标大屏 servlet
 * <p>
 * 一个页面 + 一个数据接口，都挂在同一路径下：{@code /metrics} 出页面，{@code /metrics/snapshot} 出 JSON。
 * 没有前端构建，HTML 就是这一份静态文件，改样式不用重新打包前端。
 * <p>
 * 数据里有慢 SQL 与端点清单：配了令牌就必须带令牌，没配只在本机且没有反向代理时才允许访问。
 */
public class DashboardServlet extends HttpServlet {

    private static final String TEMPLATE = "ext-metrics/dashboard.html";

    private static final String SNAPSHOT_SUFFIX = "/snapshot";

    /**
     * 允许匿名访问的地址：没配令牌时只放行本机
     */
    private static final Set<String> LOOPBACK = Set.of("127.0.0.1", "0:0:0:0:0:0:0:1", "::1");

    private static final String TOKEN_HEADER = "X-Metrics-Token";

    private final MetricsRegistry registry;

    private final String token;

    private final int refreshSeconds;

    private final double errorRateThreshold;

    private final long p99ThresholdMillis;

    private volatile byte[] page;

    public DashboardServlet(MetricsRegistry registry, MetricsProperties properties) {
        this.registry = registry;
        this.token = properties.getDashboard().getToken();
        this.refreshSeconds = properties.getDashboard().getRefreshSeconds();
        this.errorRateThreshold = properties.getAlert().getErrorRateThreshold();
        this.p99ThresholdMillis = properties.getAlert().getP99Threshold().toMillis();
    }

    @Override
    protected void doGet(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response) throws IOException {
        if (!allowed(request)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "指标接口需要 access token");
            return;
        }
        String path = request.getPathInfo();
        if (path != null && path.endsWith(SNAPSHOT_SUFFIX)) {
            response.setContentType("application/json;charset=UTF-8");
            response.setHeader("Cache-Control", "no-store");
            response.getWriter().write(JsonUtil.toJson(registry.snapshot()));
            return;
        }
        response.setContentType("text/html;charset=UTF-8");
        response.setHeader("Cache-Control", "no-store");
        response.getOutputStream().write(page());
    }

    /**
     * 配了令牌就校验令牌；没配则只允许「本机且没有反向代理」。
     * 代理后面每个请求的 {@code remoteAddr} 都是 127.0.0.1，本机不再是安全边界，所以看到转发头就拒绝。
     */
    private boolean allowed(HttpServletRequest request) {
        if (!token.isBlank()) {
            return matches(request.getHeader(TOKEN_HEADER)) || matches(request.getParameter("token"));
        }
        return LOOPBACK.contains(request.getRemoteAddr())
                && request.getHeader("X-Forwarded-For") == null
                && request.getHeader("Forwarded") == null;
    }

    /**
     * 常量时间比对，别让令牌能按响应时间一位一位猜出来
     */
    private boolean matches(String candidate) {
        return candidate != null && MessageDigest.isEqual(token.getBytes(StandardCharsets.UTF_8),
                candidate.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 模板只读一次，占位符在首次请求时替换
     */
    private byte[] page() throws IOException {
        byte[] cached = page;
        if (cached != null) {
            return cached;
        }
        synchronized (this) {
            if (page == null) {
                page = render();
            }
            return page;
        }
    }

    private byte[] render() throws IOException {
        try (InputStream stream = getClass().getClassLoader().getResourceAsStream(TEMPLATE)) {
            if (stream == null) {
                throw new IllegalStateException("指标大屏模板缺失：" + TEMPLATE);
            }
            String html = new String(stream.readAllBytes(), StandardCharsets.UTF_8)
                    .replace("{{refreshSeconds}}", String.valueOf(refreshSeconds))
                    .replace("{{snapshotUrl}}", SNAPSHOT_SUFFIX)
                    .replace("{{errorRateThreshold}}", String.valueOf(errorRateThreshold))
                    .replace("{{p99ThresholdMillis}}", String.valueOf(p99ThresholdMillis));
            return html.getBytes(StandardCharsets.UTF_8);
        }
    }
}
