package ext.library.http;

import ext.library.tool.constant.EmojiSymbol;
import ext.library.tool.exception.ExtException;
import ext.library.tool.runtime.Logs;
import org.jspecify.annotations.Nullable;
import org.springframework.util.MimeTypeUtils;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * JDK HttpClient 工具类
 * <p>
 * 提供流畅的链式调用 API，支持 HTTP 请求的构建和执行。
 * </p>
 *
 * <h2>使用示例</h2>
 * <pre>{@code
 * // 同步 GET 请求
 * String result = HttpUtil.get("https://api.example.com/data")
 *     .header("Authorization", "Bearer token")
 *     .query("page", "1")
 *     .execute()
 *     .asString();
 *
 * // 同步 POST 请求
 * String result = HttpUtil.post("https://api.example.com/users")
 *     .contentTypeJson()
 *     .body("{\"name\":\"test\"}")
 *     .execute()
 *     .asString();
 *
 * // 异步请求
 * HttpUtil.post("https://api.example.com/users")
 *     .contentTypeJson()
 *     .body("{\"name\":\"test\"}")
 *     .executeAsync()
 *     .thenAccept(response -> System.out.println(response.asString()));
 *
 * // 使用表单数据
 * String result = HttpUtil.post("https://api.example.com/login")
 *     .form("username", "admin")
 *     .form("password", "123456")
 *     .execute()
 *     .asString();
 * }</pre>
 *
 * @since jdk11
 */
public final class HttpUtil {

    /**
     * 默认连接超时（毫秒）
     */
    private static final int defaultConnectTimeout = 10_000;

    /**
     * 默认读取超时（毫秒）
     */
    private static final long defaultReadTimeout = 120_000;

    private static volatile HttpClient client;
    private static volatile HttpClient insecureClient;
    private static long defaultTimeout = defaultReadTimeout;
    private static String defaultContentType = MimeTypeUtils.APPLICATION_JSON_VALUE;

    static {
        initDefaultClient();
    }

    private HttpUtil() {
    }

    private static void initDefaultClient() {
        client = HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1).connectTimeout(Duration.ofMillis(defaultConnectTimeout)).followRedirects(HttpClient.Redirect.NORMAL).executor(Executors.newVirtualThreadPerTaskExecutor()).build();
    }

    /**
     * 设置默认超时时间
     *
     * @param timeout 超时时间（毫秒）
     */
    public static void setDefaultTimeout(long timeout) {
        HttpUtil.defaultTimeout = timeout;
    }

    /**
     * 设置默认 Content-Type
     *
     * @param contentType Content-Type
     */
    public static void setDefaultContentType(String contentType) {
        HttpUtil.defaultContentType = contentType;
    }

    /**
     * 获取默认 HttpClient（启用 SSL 验证）
     *
     * @return HttpClient 实例
     */
    public static HttpClient getClient() {
        return client;
    }

    /**
     * 设置自定义 HttpClient
     *
     * @param client HttpClient 实例
     */
    public static void setClient(HttpClient client) {
        HttpUtil.client = client;
    }

    /**
     * 获取不安全的 HttpClient（禁用 SSL 验证）
     * <p>
     * <b>警告：仅用于开发和测试环境！生产环境使用会带来安全风险。</b>
     * </p>
     *
     * @return 不验证 SSL 证书的 HttpClient
     */
    public static HttpClient getInsecureClient() {
        if (insecureClient == null) {
            synchronized (HttpUtil.class) {
                if (insecureClient == null) {
                    insecureClient = createInsecureClient();
                }
            }
        }
        return insecureClient;
    }

    /**
     * 创建不安全的 HttpClient（禁用 SSL 验证）
     */
    private static HttpClient createInsecureClient() {
        Logs.warn(EmojiSymbol.HTTP, "创建不安全的 HttpClient，SSL 证书验证已禁用。仅用于开发/测试环境！");
        try {
            TrustManager[] trustAllCertificates = new TrustManager[]{new X509TrustManager() {
                @Override
                public void checkClientTrusted(X509Certificate[] chain, String authType) {
                    // Trust all
                }

                @Override
                public void checkServerTrusted(X509Certificate[] chain, String authType) {
                    // Trust all
                }

                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }
            }};

            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustAllCertificates, new SecureRandom());

            return HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1).connectTimeout(Duration.ofMillis(defaultConnectTimeout)).followRedirects(HttpClient.Redirect.NORMAL).sslContext(sslContext).executor(Executors.newVirtualThreadPerTaskExecutor()).build();
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            throw new ExtException(EmojiSymbol.HTTP, "创建不安全 HttpClient 失败", e);
        }
    }

    /**
     * 创建通用请求构建器
     *
     * @param url 请求 URL
     *
     * @return 请求构建器
     */
    public static Request request(String url) {
        return new Request(url);
    }

    /**
     * 创建 GET 请求
     *
     * @param url 请求 URL
     *
     * @return 请求构建器
     */
    public static Request get(String url) {
        return new Request(url).method(HttpMethod.GET);
    }

    /**
     * 创建 POST 请求
     *
     * @param url 请求 URL
     *
     * @return 请求构建器
     */
    public static Request post(String url) {
        return new Request(url).method(HttpMethod.POST);
    }

    /**
     * 创建 PUT 请求
     *
     * @param url 请求 URL
     *
     * @return 请求构建器
     */
    public static Request put(String url) {
        return new Request(url).method(HttpMethod.PUT);
    }

    /**
     * 创建 DELETE 请求
     *
     * @param url 请求 URL
     *
     * @return 请求构建器
     */
    public static Request delete(String url) {
        return new Request(url).method(HttpMethod.DELETE);
    }

    /**
     * 创建 PATCH 请求
     *
     * @param url 请求 URL
     *
     * @return 请求构建器
     */
    public static Request patch(String url) {
        return new Request(url).method(HttpMethod.PATCH);
    }

    /**
     * HTTP 方法枚举
     */
    public enum HttpMethod {
        GET, POST, PUT, DELETE, PATCH, HEAD, OPTIONS
    }

    /**
     * HTTP 请求构建器
     * <p>
     * 提供流畅的链式调用 API 构建和执行 HTTP 请求。
     * </p>
     */
    public static class Request {

        private final String url;
        private final Map<String, String> headers;
        private final Map<String, String> queryParams;
        private HttpMethod method;
        @Nullable
        private Object body;
        private long timeout;
        private Class<?> responseType;
        private boolean formUrlEncoded;
        private boolean multipart;
        private boolean insecure;
        private Consumer<Exception> errorHandler;
        private BiConsumer<HttpRequest, HttpResponse<?>> responseHandler;

        /**
         * 构造请求构建器
         *
         * @param url 请求 URL
         */
        public Request(String url) {
            this.url = url;
            this.method = HttpMethod.GET;
            this.headers = new HashMap<>();
            this.queryParams = new HashMap<>();
            this.timeout = defaultTimeout;
            this.responseType = String.class;
        }

        /**
         * 设置 HTTP 方法
         *
         * @param method HTTP 方法
         *
         * @return this
         */
        public Request method(HttpMethod method) {
            this.method = method;
            return this;
        }

        /**
         * 设置为 GET 方法
         *
         * @return this
         */
        public Request get() {
            return method(HttpMethod.GET);
        }

        /**
         * 设置为 POST 方法
         *
         * @return this
         */
        public Request post() {
            return method(HttpMethod.POST);
        }

        /**
         * 设置为 PUT 方法
         *
         * @return this
         */
        public Request put() {
            return method(HttpMethod.PUT);
        }

        /**
         * 设置为 DELETE 方法
         *
         * @return this
         */
        public Request delete() {
            return method(HttpMethod.DELETE);
        }

        /**
         * 设置为 PATCH 方法
         *
         * @return this
         */
        public Request patch() {
            return method(HttpMethod.PATCH);
        }

        /**
         * 添加请求头
         *
         * @param name  头名称
         * @param value 头值
         *
         * @return this
         */
        public Request header(String name, String value) {
            this.headers.put(name, value);
            return this;
        }

        /**
         * 批量添加请求头
         *
         * @param headers 请求头 Map
         *
         * @return this
         */
        public Request headers(Map<String, String> headers) {
            this.headers.putAll(headers);
            return this;
        }

        /**
         * 设置 Content-Type
         *
         * @param contentType Content-Type 值
         *
         * @return this
         */
        public Request contentType(String contentType) {
            this.headers.put("Content-Type", contentType);
            return this;
        }

        /**
         * 设置 Accept
         *
         * @param accept Accept 值
         *
         * @return this
         */
        public Request accept(String accept) {
            this.headers.put("Accept", accept);
            return this;
        }

        /**
         * 设置 Authorization
         *
         * @param token Authorization 值
         *
         * @return this
         */
        public Request authorization(String token) {
            this.headers.put("Authorization", token);
            return this;
        }

        /**
         * 设置 Bearer Token 认证
         *
         * @param token Token 值
         *
         * @return this
         */
        public Request bearer(String token) {
            return authorization("Bearer " + token);
        }

        /**
         * 添加查询参数
         *
         * @param name  参数名
         * @param value 参数值
         *
         * @return this
         */
        public Request query(String name, String value) {
            this.queryParams.put(name, value);
            return this;
        }

        /**
         * 批量添加查询参数
         *
         * @param params 参数 Map
         *
         * @return this
         */
        public Request query(Map<String, String> params) {
            this.queryParams.putAll(params);
            return this;
        }

        /**
         * 设置字符串请求体
         *
         * @param body 请求体
         *
         * @return this
         */
        public Request body(String body) {
            this.body = body;
            this.formUrlEncoded = false;
            this.multipart = false;
            return this;
        }

        /**
         * 设置字节数组请求体
         *
         * @param body 请求体
         *
         * @return this
         */
        public Request body(byte[] body) {
            this.body = body;
            this.formUrlEncoded = false;
            this.multipart = false;
            return this;
        }

        /**
         * 设置对象请求体
         *
         * @param body 请求体对象
         *
         * @return this
         */
        public Request body(Object body) {
            this.body = body;
            this.formUrlEncoded = false;
            this.multipart = false;
            return this;
        }

        /**
         * 设置 BodyPublisher 请求体
         *
         * @param bodyPublisher BodyPublisher
         *
         * @return this
         */
        public Request body(HttpRequest.BodyPublisher bodyPublisher) {
            this.body = bodyPublisher;
            this.formUrlEncoded = false;
            this.multipart = false;
            return this;
        }

        /**
         * 添加表单字段
         *
         * @param name  字段名
         * @param value 字段值
         *
         * @return this
         */
        public Request form(String name, Object value) {
            if (this.body == null) {
                this.body = new HashMap<String, Object>();
            }
            if (this.body instanceof Map) {
                @SuppressWarnings("unchecked") Map<String, Object> form = (Map<String, Object>) this.body;
                form.put(name, value);
            }
            this.formUrlEncoded = true;
            this.multipart = false;
            return this;
        }

        /**
         * 设置表单数据
         *
         * @param formData 表单数据
         *
         * @return this
         */
        public Request form(Map<String, Object> formData) {
            this.body = formData;
            this.formUrlEncoded = true;
            this.multipart = false;
            return this;
        }

        /**
         * 设置 multipart 表单
         *
         * @param name     字段名
         * @param filename 文件名
         * @param stream   文件流
         *
         * @return this
         */
        public Request multipartForm(String name, String filename, InputStream stream) {
            this.body = new Object[]{name, filename, stream};
            this.formUrlEncoded = false;
            this.multipart = true;
            return this;
        }

        /**
         * 设置超时时间（毫秒）
         *
         * @param millis 超时毫秒数
         *
         * @return this
         */
        public Request timeout(long millis) {
            this.timeout = millis;
            return this;
        }

        /**
         * 设置超时时间
         *
         * @param duration 超时时长
         *
         * @return this
         */
        public Request timeout(Duration duration) {
            this.timeout = duration.toMillis();
            return this;
        }

        /**
         * 启用不安全模式（禁用 SSL 验证）
         * <p>
         * <b>警告：仅用于开发和测试环境！</b>
         * </p>
         *
         * @return this
         */
        public Request insecure() {
            this.insecure = true;
            return this;
        }

        /**
         * 同步执行请求
         *
         * @return 响应对象
         */
        public Response execute() {
            HttpRequest httpRequest = buildRequest();
            HttpResponse<?> response = sendRequest(httpRequest);
            return new Response(response);
        }

        /**
         * 同步执行并转换为指定类型
         *
         * @param type 目标类型
         * @param <T>  类型参数
         *
         * @return 转换后的结果
         */
        public <T> T executeAs(Class<T> type) {
            this.responseType = type;
            HttpRequest httpRequest = buildRequest();
            HttpResponse<?> response = sendRequest(httpRequest);
            return convertResponse(response, type);
        }

        /**
         * 同步执行并返回字符串
         *
         * @return 响应字符串
         */
        public String executeAsString() {
            return executeAs(String.class);
        }

        /**
         * 同步执行并返回字节数组
         *
         * @return 响应字节数组
         */
        public byte[] executeAsBytes() {
            return executeAs(byte[].class);
        }

        /**
         * 同步执行并返回输入流
         *
         * @return 响应输入流
         */
        public InputStream executeAsStream() {
            return executeAs(InputStream.class);
        }

        /**
         * 异步执行请求
         *
         * @return CompletableFuture
         */
        public CompletableFuture<Response> executeAsync() {
            HttpRequest httpRequest = buildRequest();
            return sendAsyncRequest(httpRequest).thenApply(Response::new).exceptionally(ex -> {
                if (errorHandler != null && ex instanceof Exception) {
                    errorHandler.accept((Exception) ex);
                }
                return null;
            });
        }

        /**
         * 异步执行并转换为指定类型
         *
         * @param type 目标类型
         * @param <T>  类型参数
         *
         * @return CompletableFuture
         */
        public <T> CompletableFuture<T> executeAsyncAs(Class<T> type) {
            this.responseType = type;
            HttpRequest httpRequest = buildRequest();
            return sendAsyncRequest(httpRequest).thenApply(response -> convertResponse(response, type)).exceptionally(ex -> {
                if (errorHandler != null && ex instanceof Exception) {
                    errorHandler.accept((Exception) ex);
                }
                return null;
            });
        }

        /**
         * 异步执行并返回字符串
         *
         * @return CompletableFuture
         */
        public CompletableFuture<String> executeAsyncAsString() {
            return executeAsyncAs(String.class);
        }

        /**
         * 下载文件
         *
         * @param filePath 保存路径
         *
         * @return 文件路径
         */
        public Path download(String filePath) {
            HttpRequest httpRequest = buildRequest();
            try {
                HttpClient httpClient = insecure ? getInsecureClient() : client;
                HttpResponse<Path> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofFile(Path.of(filePath)));
                return response.body();
            } catch (IOException | InterruptedException e) {
                throw new ExtException(EmojiSymbol.HTTP, e);
            }
        }

        /**
         * 设置错误处理器
         *
         * @param errorHandler 错误处理器
         *
         * @return this
         */
        public Request onError(Consumer<Exception> errorHandler) {
            this.errorHandler = errorHandler;
            return this;
        }

        /**
         * 设置响应处理器
         *
         * @param responseHandler 响应处理器
         *
         * @return this
         */
        public Request onResponse(BiConsumer<HttpRequest, HttpResponse<?>> responseHandler) {
            this.responseHandler = responseHandler;
            return this;
        }

        /**
         * 设置 Content-Type 为 JSON
         *
         * @return this
         */
        public Request contentTypeJson() {
            return contentType("application/json; charset=UTF-8");
        }

        /**
         * 设置 Content-Type 为表单
         *
         * @return this
         */
        public Request contentTypeForm() {
            return contentType("application/x-www-form-urlencoded; charset=UTF-8");
        }

        /**
         * 设置 Accept 为 JSON
         *
         * @return this
         */
        public Request acceptJson() {
            return accept("application/json");
        }

        /**
         * 设置 Accept 为 XML
         *
         * @return this
         */
        public Request acceptXml() {
            return accept("application/xml");
        }

        private HttpRequest buildRequest() {
            String requestUrl = buildUrl();
            Duration duration = Duration.ofMillis(timeout > 0 ? timeout : defaultTimeout);
            HttpRequest.Builder builder = HttpRequest.newBuilder().uri(URI.create(requestUrl)).timeout(duration);
            applyMethod(builder);
            applyHeaders(builder);
            return builder.build();
        }

        private String buildUrl() {
            if (queryParams.isEmpty()) {
                return url;
            }
            StringBuilder sb = new StringBuilder(url);
            String separator = url.contains("?") ? "&" : "?";
            sb.append(separator);
            queryParams.forEach((k, v) -> {
                sb.append(k).append("=").append(java.net.URLEncoder.encode(v, StandardCharsets.UTF_8));
                sb.append("&");
            });
            return sb.substring(0, sb.length() - 1);
        }

        private void applyMethod(HttpRequest.Builder builder) {
            switch (method) {
                case GET -> builder.GET();
                case POST -> builder.POST(buildBodyPublisher());
                case PUT -> builder.PUT(buildBodyPublisher());
                case DELETE -> builder.DELETE();
                case PATCH -> builder.method("PATCH", buildBodyPublisher());
                case HEAD -> builder.method("HEAD", HttpRequest.BodyPublishers.noBody());
                case OPTIONS -> builder.method("OPTIONS", HttpRequest.BodyPublishers.noBody());
            }
        }

        private void applyHeaders(HttpRequest.Builder builder) {
            if (!headers.containsKey("Content-Type") && method != HttpMethod.GET && method != HttpMethod.HEAD && method != HttpMethod.DELETE) {
                if (formUrlEncoded) {
                    builder.header("Content-Type", "application/x-www-form-urlencoded");
                } else if (multipart) {
                    builder.header("Content-Type", "multipart/form-data");
                } else {
                    builder.header("Content-Type", defaultContentType);
                }
            }
            headers.forEach(builder::header);
        }

        private HttpRequest.BodyPublisher buildBodyPublisher() {
            switch (body) {
                case null -> {
                    return HttpRequest.BodyPublishers.noBody();
                }
                case HttpRequest.BodyPublisher bp -> {
                    return bp;
                }
                case String s -> {
                    return HttpRequest.BodyPublishers.ofString(s, StandardCharsets.UTF_8);
                }
                case byte[] bytes -> {
                    return HttpRequest.BodyPublishers.ofByteArray(bytes);
                }
                case Map map -> {
                    @SuppressWarnings("unchecked") Map<String, Object> form = (Map<String, Object>) body;
                    StringJoiner sj = new StringJoiner("&");
                    form.forEach((k, v) -> sj.add(k + "=" + java.net.URLEncoder.encode(String.valueOf(v), StandardCharsets.UTF_8)));
                    return HttpRequest.BodyPublishers.ofString(sj.toString(), StandardCharsets.UTF_8);
                }
                default -> {
                }
            }
            return HttpRequest.BodyPublishers.ofString(body.toString(), StandardCharsets.UTF_8);
        }

        private HttpResponse<?> sendRequest(HttpRequest httpRequest) {
            try {
                HttpClient httpClient = insecure ? getInsecureClient() : client;
                if (byte[].class == responseType) {
                    HttpResponse<byte[]> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofByteArray());
                    if (responseHandler != null) {
                        responseHandler.accept(httpRequest, response);
                    }
                    return response;
                } else if (InputStream.class == responseType) {
                    HttpResponse<InputStream> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofInputStream());
                    if (responseHandler != null) {
                        responseHandler.accept(httpRequest, response);
                    }
                    return response;
                } else {
                    HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
                    if (responseHandler != null) {
                        responseHandler.accept(httpRequest, response);
                    }
                    return response;
                }
            } catch (IOException | InterruptedException e) {
                if (errorHandler != null) {
                    errorHandler.accept(e);
                }
                throw new ExtException(EmojiSymbol.HTTP, e);
            }
        }

        private CompletableFuture<HttpResponse<?>> sendAsyncRequest(HttpRequest httpRequest) {
            HttpClient httpClient = insecure ? getInsecureClient() : client;
            if (byte[].class == responseType) {
                return httpClient.sendAsync(httpRequest, HttpResponse.BodyHandlers.ofByteArray()).thenApply(response -> {
                    if (responseHandler != null) {
                        responseHandler.accept(httpRequest, response);
                    }
                    return response;
                });
            } else if (InputStream.class == responseType) {
                return httpClient.sendAsync(httpRequest, HttpResponse.BodyHandlers.ofInputStream()).thenApply(response -> {
                    if (responseHandler != null) {
                        responseHandler.accept(httpRequest, response);
                    }
                    return response;
                });
            } else {
                return httpClient.sendAsync(httpRequest, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8)).thenApply(response -> {
                    if (responseHandler != null) {
                        responseHandler.accept(httpRequest, response);
                    }
                    return response;
                });
            }
        }

        @SuppressWarnings("unchecked")
        private <T> T convertResponse(HttpResponse<?> response, Class<T> type) {
            if (type == String.class) {
                if (response.body() instanceof String s) {
                    return (T) s;
                }
                return (T) new String((byte[]) response.body(), StandardCharsets.UTF_8);
            }
            if (type == byte[].class) {
                if (response.body() instanceof byte[] bytes) {
                    return (T) bytes;
                }
                return (T) ((String) response.body()).getBytes(StandardCharsets.UTF_8);
            }
            if (type == InputStream.class) {
                return (T) response.body();
            }
            throw new UnsupportedOperationException("Unsupported response type: " + type);
        }

        /**
         * 获取请求 URL
         *
         * @return URL
         */
        public String getUrl() {
            return url;
        }

        /**
         * 获取 HTTP 方法
         *
         * @return HTTP 方法
         */
        public HttpMethod getMethod() {
            return method;
        }

        /**
         * 获取请求头
         *
         * @return 请求头 Map
         */
        public Map<String, String> getHeaders() {
            return headers;
        }

        /**
         * 获取查询参数
         *
         * @return 查询参数 Map
         */
        public Map<String, String> getQueryParams() {
            return queryParams;
        }

        /**
         * 获取请求体
         *
         * @return 请求体
         */
        public @Nullable Object getBody() {
            return body;
        }

        /**
         * 获取超时时间
         *
         * @return 超时时间（毫秒）
         */
        public long getTimeout() {
            return timeout;
        }

        /**
         * 获取响应类型
         *
         * @return 响应类型
         */
        public Class<?> getResponseType() {
            return responseType;
        }
    }

    /**
     * HTTP 响应包装类
     */
    public static class Response {

        private final HttpResponse<?> response;

        /**
         * 构造响应包装
         *
         * @param response 原始响应
         */
        public Response(HttpResponse<?> response) {
            this.response = response;
        }

        /**
         * 获取响应体字符串
         *
         * @return 响应字符串
         */
        public String asString() {
            Object body = response.body();
            if (body instanceof String s) {
                return s;
            }
            if (body instanceof byte[] bytes) {
                return new String(bytes, StandardCharsets.UTF_8);
            }
            return body.toString();
        }

        /**
         * 获取响应体字节数组
         *
         * @return 响应字节数组
         */
        public byte[] asBytes() {
            Object body = response.body();
            if (body instanceof byte[] bytes) {
                return bytes;
            }
            if (body instanceof String s) {
                return s.getBytes(StandardCharsets.UTF_8);
            }
            return body.toString().getBytes(StandardCharsets.UTF_8);
        }

        /**
         * 获取响应体输入流
         *
         * @return 响应输入流
         */
        public InputStream asStream() {
            return (InputStream) response.body();
        }

        /**
         * 获取状态码
         *
         * @return HTTP 状态码
         */
        public int statusCode() {
            return response.statusCode();
        }

        /**
         * 是否成功（2xx）
         *
         * @return 是否成功
         */
        public boolean isSuccess() {
            return statusCode() >= 200 && statusCode() < 300;
        }

        /**
         * 是否重定向（3xx）
         *
         * @return 是否重定向
         */
        public boolean isRedirect() {
            return statusCode() >= 300 && statusCode() < 400;
        }

        /**
         * 是否客户端错误（4xx）
         *
         * @return 是否客户端错误
         */
        public boolean isClientError() {
            return statusCode() >= 400 && statusCode() < 500;
        }

        /**
         * 是否服务端错误（5xx）
         *
         * @return 是否服务端错误
         */
        public boolean isServerError() {
            return statusCode() >= 500;
        }

        /**
         * 获取响应头
         *
         * @param name 头名称
         *
         * @return 头值
         */
        public String header(String name) {
            return response.headers().firstValue(name).orElse(null);
        }

        /**
         * 获取 Content-Type
         *
         * @return Content-Type 值
         */
        public String contentType() {
            return header("Content-Type");
        }

        /**
         * 获取 Content-Length
         *
         * @return Content-Length 值，无法解析返回 -1
         */
        public long contentLength() {
            String length = header("Content-Length");
            if (length != null) {
                try {
                    return Long.parseLong(length);
                } catch (NumberFormatException e) {
                    return -1;
                }
            }
            return -1;
        }

        /**
         * 获取指定类型的响应体
         *
         * @param type 目标类型
         * @param <T>  类型参数
         *
         * @return 转换后的响应体
         */
        @SuppressWarnings("unchecked")
        public <T> T body(Class<T> type) {
            if (type == String.class) {
                return (T) asString();
            }
            if (type == byte[].class) {
                return (T) asBytes();
            }
            if (type == InputStream.class) {
                return (T) asStream();
            }
            throw new UnsupportedOperationException("Unsupported body type: " + type);
        }

        /**
         * 获取原始响应对象
         *
         * @return HttpResponse
         */
        public HttpResponse<?> getRawResponse() {
            return response;
        }

        @Override
        public String toString() {
            return String.format("Response{statusCode=%d, contentType='%s', bodyLength=%d}", statusCode(), contentType(), contentLength());
        }
    }
}
