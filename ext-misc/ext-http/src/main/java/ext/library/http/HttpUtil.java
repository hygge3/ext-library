package ext.library.http;

import ext.library.tool.constant.EmojiSymbol;
import ext.library.tool.runtime.Exceptions;
import ext.library.tool.exception.ExtException;
import ext.library.tool.util.StringUtil;
import org.jspecify.annotations.Nullable;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.io.InputStream;
import java.net.Authenticator;
import java.net.CookieHandler;
import java.net.ProxySelector;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
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

    private static volatile HttpClient client;
    private static HttpClientProps httpClientProps;
    private static long defaultTimeout = 1200000;
    private static String defaultContentType = "application/json";

    static {
        initDefaultClient();
    }

    private HttpUtil() {
    }

    private static void initDefaultClient() {
        httpClientProps = new HttpClientProps();
        client = HttpClient.newBuilder()
                .version(httpClientProps.getVersion())
                .connectTimeout(Duration.ofMillis(httpClientProps.getConnectTimeout()))
                .followRedirects(httpClientProps.getRedirect())
                .executor(Executors.newVirtualThreadPerTaskExecutor())
                .build();
    }

    public static void setDefaultTimeout(long timeout) {
        HttpUtil.defaultTimeout = timeout;
    }

    public static void setDefaultContentType(String contentType) {
        HttpUtil.defaultContentType = contentType;
    }

    public static HttpClient getClient() {
        return client;
    }

    public static void setClient(HttpClient client) {
        HttpUtil.client = client;
    }

    public static Request request(String url) {
        return new Request(url);
    }

    public static Request get(String url) {
        return new Request(url).method(HttpMethod.GET);
    }

    public static Request post(String url) {
        return new Request(url).method(HttpMethod.POST);
    }

    public static Request put(String url) {
        return new Request(url).method(HttpMethod.PUT);
    }

    public static Request delete(String url) {
        return new Request(url).method(HttpMethod.DELETE);
    }

    public static Request patch(String url) {
        return new Request(url).method(HttpMethod.PATCH);
    }

    @SuppressWarnings("unchecked")
    private static <T> T getResData(HttpRequest httpRequest, Class<T> resClass) {
        T t;
        try {
            if (byte[].class == resClass) {
                t = (T) client.send(httpRequest, HttpResponse.BodyHandlers.ofByteArray()).body();
            } else if (String.class == resClass) {
                t = (T) client.send(httpRequest, HttpResponse.BodyHandlers.ofString()).body();
            } else if (InputStream.class == resClass) {
                t = (T) client.send(httpRequest, HttpResponse.BodyHandlers.ofInputStream()).body();
            } else {
                throw new UnsupportedOperationException(StringUtil.format("不支持的返回类型:[{}]", resClass));
            }
            return t;
        } catch (IOException | InterruptedException e) {
            throw new ExtException(EmojiSymbol.HTTP, e);
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> HttpResponse<T> getRes(HttpRequest httpRequest, Class<T> resClass) {
        HttpResponse<T> response;
        try {
            if (byte[].class == resClass) {
                response = (HttpResponse<T>) client.send(httpRequest, HttpResponse.BodyHandlers.ofByteArray());
            } else if (String.class == resClass) {
                response = (HttpResponse<T>) client.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            } else if (InputStream.class == resClass) {
                response = (HttpResponse<T>) client.send(httpRequest, HttpResponse.BodyHandlers.ofInputStream());
            } else {
                throw new UnsupportedOperationException(StringUtil.format("不支持的返回类型:[{}]", resClass));
            }
            return response;
        } catch (IOException | InterruptedException e) {
            throw new ExtException(EmojiSymbol.HTTP, e);
        }
    }

    public static HttpRequest buildGetRequest(String url, Map<String, String> headerMap, long timeout) {
        if (timeout <= 0) {
            timeout = httpClientProps.getDefaultReadTimeout();
        }
        Duration duration = Duration.ofMillis(timeout);
        return HttpRequest.newBuilder()
                .GET()
                .headers(createHeader(headerMap, httpClientProps.getDefaultContentType()))
                .uri(URI.create(url))
                .timeout(duration)
                .build();
    }

    public static HttpRequest buildDeleteRequest(String url, Map<String, String> headerMap, long timeout) {
        if (timeout <= 0) {
            timeout = httpClientProps.getDefaultReadTimeout();
        }
        Duration duration = Duration.ofMillis(timeout);
        return HttpRequest.newBuilder()
                .DELETE()
                .headers(createHeader(headerMap, httpClientProps.getDefaultContentType()))
                .uri(URI.create(url))
                .timeout(duration)
                .build();
    }

    public static HttpRequest buildPostRequest(String url, Map<String, String> headerMap, Map<String, Object> form, long timeout) {
        StringJoiner sj = new StringJoiner("&");
        form.forEach((k, v) -> sj.add(k + "=" + v));
        HttpRequest.BodyPublisher bodyPublisher = HttpRequest.BodyPublishers.ofString(sj.toString(), StandardCharsets.UTF_8);
        return buildPostRequest(url, headerMap, bodyPublisher, timeout);
    }

    public static HttpRequest buildPutRequest(String url, Map<String, String> headerMap, Map<String, Object> form, long timeout) {
        StringJoiner sj = new StringJoiner("&");
        form.forEach((k, v) -> sj.add(k + "=" + v));
        HttpRequest.BodyPublisher bodyPublisher = HttpRequest.BodyPublishers.ofString(sj.toString(), StandardCharsets.UTF_8);
        return buildPutRequest(url, headerMap, bodyPublisher, timeout);
    }

    public static HttpRequest buildPostRequest(String url, Map<String, String> headerMap, String requestBody, long timeout) {
        return buildPostRequest(url, headerMap, HttpRequest.BodyPublishers.ofString(requestBody, StandardCharsets.UTF_8), timeout);
    }

    public static HttpRequest buildPutRequest(String url, Map<String, String> headerMap, String requestBody, long timeout) {
        return buildPutRequest(url, headerMap, HttpRequest.BodyPublishers.ofString(requestBody, StandardCharsets.UTF_8), timeout);
    }

    public static HttpRequest buildPostRequest(String url, Map<String, String> headerMap, HttpRequest.BodyPublisher bodyPublisher, long timeout) {
        if (timeout <= 0) {
            timeout = httpClientProps.getDefaultReadTimeout();
        }
        Duration duration = Duration.ofMillis(timeout);
        return HttpRequest.newBuilder()
                .POST(bodyPublisher)
                .headers(createHeader(headerMap, httpClientProps.getDefaultContentType()))
                .uri(URI.create(url))
                .timeout(duration)
                .build();
    }

    public static HttpRequest buildPutRequest(String url, Map<String, String> headerMap, HttpRequest.BodyPublisher bodyPublisher, long timeout) {
        if (timeout <= 0) {
            timeout = httpClientProps.getDefaultReadTimeout();
        }
        Duration duration = Duration.ofMillis(timeout);
        return HttpRequest.newBuilder()
                .PUT(bodyPublisher)
                .headers(createHeader(headerMap, httpClientProps.getDefaultContentType()))
                .uri(URI.create(url))
                .timeout(duration)
                .build();
    }

    private static String[] createHeader(@Nullable Map<String, String> headerMap, String contentType) {
        if (headerMap == null) {
            headerMap = new HashMap<>();
            headerMap.put("Content-Type", contentType);
        } else {
            headerMap = new HashMap<>(headerMap);
            Set<String> headerKeys = headerMap.keySet();
            if (headerKeys.stream().noneMatch("Content-Type"::equalsIgnoreCase)) {
                headerMap.put("Content-Type", contentType);
            }
        }
        String[] result = new String[headerMap.size() * 2];
        int index = 0;
        for (Map.Entry<String, String> entry : headerMap.entrySet()) {
            result[index++] = entry.getKey();
            result[index++] = entry.getValue();
        }
        return result;
    }

    public enum HttpMethod {
        GET,
        POST,
        PUT,
        DELETE,
        PATCH,
        HEAD,
        OPTIONS
    }

    public static class HttpClientProps {

        private final HttpClient.Version version = HttpClient.Version.HTTP_1_1;
        private final HttpClient.Redirect redirect = HttpClient.Redirect.NORMAL;
        private final int connectTimeout = 10000;
        private final int defaultReadTimeout = 1200000;
        private final String defaultContentType = "application/json";
        private Executor executor;
        private Authenticator authenticator;
        private ProxySelector proxySelector;
        private CookieHandler cookieHandler;

        public HttpClientProps() {
            TrustManager[] trustAllCertificates = new TrustManager[]{new X509TrustManager() {
                @Override
                public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
                }

                @Override
                public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
                }

                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }
            }};
            SSLParameters sslParameters = new SSLParameters();
            sslParameters.setEndpointIdentificationAlgorithm("");

            try {
                SSLContext sslContext = SSLContext.getInstance("TLS");
                System.setProperty("jdk.internal.httpclient.disableHostnameVerification", "true");
                sslContext.init(null, trustAllCertificates, new SecureRandom());
            } catch (NoSuchAlgorithmException | KeyManagementException e) {
                Exceptions.log(e);
            }
        }

        public HttpClient.Version getVersion() {
            return version;
        }

        public HttpClient.Redirect getRedirect() {
            return redirect;
        }

        public int getConnectTimeout() {
            return connectTimeout;
        }

        public int getDefaultReadTimeout() {
            return defaultReadTimeout;
        }

        public String getDefaultContentType() {
            return defaultContentType;
        }

        public Executor getExecutor() {
            return executor;
        }

        public void setExecutor(Executor executor) {
            this.executor = executor;
        }

        public Authenticator getAuthenticator() {
            return authenticator;
        }

        public void setAuthenticator(Authenticator authenticator) {
            this.authenticator = authenticator;
        }

        public ProxySelector getProxySelector() {
            return proxySelector;
        }

        public void setProxySelector(ProxySelector proxySelector) {
            this.proxySelector = proxySelector;
        }

        public CookieHandler getCookieHandler() {
            return cookieHandler;
        }

        public void setCookieHandler(CookieHandler cookieHandler) {
            this.cookieHandler = cookieHandler;
        }
    }

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
        private Consumer<Exception> errorHandler;
        private BiConsumer<HttpRequest, HttpResponse<?>> responseHandler;

        public Request(String url) {
            this.url = url;
            this.method = HttpMethod.GET;
            this.headers = new HashMap<>();
            this.queryParams = new HashMap<>();
            this.timeout = defaultTimeout;
            this.responseType = String.class;
        }

        public Request(String url, HttpMethod method) {
            this(url);
            this.method = method;
        }

        public Request method(HttpMethod method) {
            this.method = method;
            return this;
        }

        public Request get() {
            return method(HttpMethod.GET);
        }

        public Request post() {
            return method(HttpMethod.POST);
        }

        public Request put() {
            return method(HttpMethod.PUT);
        }

        public Request delete() {
            return method(HttpMethod.DELETE);
        }

        public Request patch() {
            return method(HttpMethod.PATCH);
        }

        public Request header(String name, String value) {
            this.headers.put(name, value);
            return this;
        }

        public Request headers(Map<String, String> headers) {
            this.headers.putAll(headers);
            return this;
        }

        public Request contentType(String contentType) {
            this.headers.put("Content-Type", contentType);
            return this;
        }

        public Request accept(String accept) {
            this.headers.put("Accept", accept);
            return this;
        }

        public Request authorization(String token) {
            this.headers.put("Authorization", token);
            return this;
        }

        public Request bearer(String token) {
            return authorization("Bearer " + token);
        }

        public Request query(String name, String value) {
            this.queryParams.put(name, value);
            return this;
        }

        public Request query(Map<String, String> params) {
            this.queryParams.putAll(params);
            return this;
        }

        public Request body(String body) {
            this.body = body;
            this.formUrlEncoded = false;
            this.multipart = false;
            return this;
        }

        public Request body(byte[] body) {
            this.body = body;
            this.formUrlEncoded = false;
            this.multipart = false;
            return this;
        }

        public Request body(Object body) {
            this.body = body;
            this.formUrlEncoded = false;
            this.multipart = false;
            return this;
        }

        public Request body(HttpRequest.BodyPublisher bodyPublisher) {
            this.body = bodyPublisher;
            this.formUrlEncoded = false;
            this.multipart = false;
            return this;
        }

        public Request form(String name, Object value) {
            if (this.body == null) {
                this.body = new HashMap<String, Object>();
            }
            if (this.body instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> form = (Map<String, Object>) this.body;
                form.put(name, value);
            }
            this.formUrlEncoded = true;
            this.multipart = false;
            return this;
        }

        public Request form(Map<String, Object> formData) {
            this.body = formData;
            this.formUrlEncoded = true;
            this.multipart = false;
            return this;
        }

        public Request multipartForm(String name, String filename, InputStream stream) {
            this.body = new Object[]{name, filename, stream};
            this.formUrlEncoded = false;
            this.multipart = true;
            return this;
        }

        public Request timeout(long millis) {
            this.timeout = millis;
            return this;
        }

        public Request timeout(Duration duration) {
            this.timeout = duration.toMillis();
            return this;
        }

        public Response execute() {
            HttpRequest httpRequest = buildRequest();
            HttpResponse<?> response = sendRequest(httpRequest);
            return new Response(response);
        }

        public <T> T executeAs(Class<T> type) {
            this.responseType = type;
            HttpRequest httpRequest = buildRequest();
            HttpResponse<?> response = sendRequest(httpRequest);
            return convertResponse(response, type);
        }

        public String executeAsString() {
            return executeAs(String.class);
        }

        public byte[] executeAsBytes() {
            return executeAs(byte[].class);
        }

        public InputStream executeAsStream() {
            return executeAs(InputStream.class);
        }

        public CompletableFuture<Response> executeAsync() {
            HttpRequest httpRequest = buildRequest();
            return sendAsyncRequest(httpRequest)
                    .thenApply(Response::new)
                    .exceptionally(ex -> {
                        if (errorHandler != null && ex instanceof Exception) {
                            errorHandler.accept((Exception) ex);
                        }
                        return null;
                    });
        }

        public <T> CompletableFuture<T> executeAsyncAs(Class<T> type) {
            this.responseType = type;
            HttpRequest httpRequest = buildRequest();
            return sendAsyncRequest(httpRequest)
                    .thenApply(response -> convertResponse(response, type))
                    .exceptionally(ex -> {
                        if (errorHandler != null && ex instanceof Exception) {
                            errorHandler.accept((Exception) ex);
                        }
                        return null;
                    });
        }

        public CompletableFuture<String> executeAsyncAsString() {
            return executeAsyncAs(String.class);
        }

        public Path download(String filePath) {
            HttpRequest httpRequest = buildRequest();
            try {
                HttpResponse<Path> response = client.send(httpRequest, HttpResponse.BodyHandlers.ofFile(Path.of(filePath)));
                return response.body();
            } catch (IOException | InterruptedException e) {
                throw new ExtException(EmojiSymbol.HTTP, e);
            }
        }

        public Request onError(Consumer<Exception> errorHandler) {
            this.errorHandler = errorHandler;
            return this;
        }

        public Request onResponse(BiConsumer<HttpRequest, HttpResponse<?>> responseHandler) {
            this.responseHandler = responseHandler;
            return this;
        }

        public Request contentTypeJson() {
            return contentType("application/json; charset=UTF-8");
        }

        public Request contentTypeForm() {
            return contentType("application/x-www-form-urlencoded; charset=UTF-8");
        }

        public Request acceptJson() {
            return accept("application/json");
        }

        public Request acceptXml() {
            return accept("application/xml");
        }

        private HttpRequest buildRequest() {
            String requestUrl = buildUrl();
            Duration duration = Duration.ofMillis(timeout > 0 ? timeout : defaultTimeout);
            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(URI.create(requestUrl))
                    .timeout(duration);
            applyMethod(builder);
            applyHeaders(builder);
            applyBody(builder);
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
                case HEAD -> builder.HEAD();
                case OPTIONS -> builder.method("OPTIONS", HttpRequest.BodyPublishers.noBody());
            }
        }

        private void applyHeaders(HttpRequest.Builder builder) {
            if (!headers.containsKey("Content-Type") && method != HttpMethod.GET && method != HttpMethod.HEAD) {
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

        private void applyBody(HttpRequest.Builder builder) {
            if (method == HttpMethod.GET || method == HttpMethod.DELETE || method == HttpMethod.HEAD) {
                return;
            }
            if (body == null) {
                return;
            }
            builder.header("Content-Type", getBodyContentType());
            HttpRequest.BodyPublisher bodyPublisher = buildBodyPublisher();
            if (bodyPublisher != null) {
                switch (method) {
                    case POST -> builder.POST(bodyPublisher);
                    case PUT -> builder.PUT(bodyPublisher);
                    case PATCH -> builder.method("PATCH", bodyPublisher);
                    default -> {
                    }
                }
            }
        }

        private String getBodyContentType() {
            String contentType = headers.get("Content-Type");
            if (contentType != null) {
                return contentType;
            }
            if (formUrlEncoded) {
                return "application/x-www-form-urlencoded";
            }
            if (multipart) {
                return "multipart/form-data";
            }
            return defaultContentType;
        }

        private HttpRequest.BodyPublisher buildBodyPublisher() {
            if (body == null) {
                return HttpRequest.BodyPublishers.noBody();
            }
            if (body instanceof HttpRequest.BodyPublisher) {
                return (HttpRequest.BodyPublisher) body;
            }
            if (body instanceof String) {
                return HttpRequest.BodyPublishers.ofString((String) body, StandardCharsets.UTF_8);
            }
            if (body instanceof byte[]) {
                return HttpRequest.BodyPublishers.ofByteArray((byte[]) body);
            }
            if (body instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> form = (Map<String, Object>) body;
                StringJoiner sj = new StringJoiner("&");
                form.forEach((k, v) -> sj.add(k + "=" + java.net.URLEncoder.encode(String.valueOf(v), StandardCharsets.UTF_8)));
                return HttpRequest.BodyPublishers.ofString(sj.toString(), StandardCharsets.UTF_8);
            }
            if (body instanceof Object[]) {
                return HttpRequest.BodyPublishers.ofString(body.toString(), StandardCharsets.UTF_8);
            }
            return HttpRequest.BodyPublishers.ofString(body.toString(), StandardCharsets.UTF_8);
        }

        private HttpResponse<?> sendRequest(HttpRequest httpRequest) {
            try {
                if (byte[].class == responseType) {
                    HttpResponse<byte[]> response = client.send(httpRequest, HttpResponse.BodyHandlers.ofByteArray());
                    if (responseHandler != null) {
                        responseHandler.accept(httpRequest, response);
                    }
                    return response;
                } else if (InputStream.class == responseType) {
                    HttpResponse<InputStream> response = client.send(httpRequest, HttpResponse.BodyHandlers.ofInputStream());
                    if (responseHandler != null) {
                        responseHandler.accept(httpRequest, response);
                    }
                    return response;
                } else {
                    HttpResponse<String> response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
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
            if (byte[].class == responseType) {
                return client.sendAsync(httpRequest, HttpResponse.BodyHandlers.ofByteArray())
                        .thenApply(response -> {
                            if (responseHandler != null) {
                                responseHandler.accept(httpRequest, response);
                            }
                            return response;
                        });
            } else if (InputStream.class == responseType) {
                return client.sendAsync(httpRequest, HttpResponse.BodyHandlers.ofInputStream())
                        .thenApply(response -> {
                            if (responseHandler != null) {
                                responseHandler.accept(httpRequest, response);
                            }
                            return response;
                        });
            } else {
                return client.sendAsync(httpRequest, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8))
                        .thenApply(response -> {
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
                if (response.body() instanceof String) {
                    return (T) response.body();
                }
                return (T) new String((byte[]) response.body(), StandardCharsets.UTF_8);
            }
            if (type == byte[].class) {
                if (response.body() instanceof byte[]) {
                    return (T) response.body();
                }
                return (T) ((String) response.body()).getBytes(StandardCharsets.UTF_8);
            }
            if (type == InputStream.class) {
                return (T) response.body();
            }
            throw new UnsupportedOperationException("Unsupported response type: " + type);
        }

        public String getUrl() {
            return url;
        }

        public HttpMethod getMethod() {
            return method;
        }

        public Map<String, String> getHeaders() {
            return headers;
        }

        public Map<String, String> getQueryParams() {
            return queryParams;
        }

        public Object getBody() {
            return body;
        }

        public long getTimeout() {
            return timeout;
        }

        public Class<?> getResponseType() {
            return responseType;
        }
    }

    public static class Response {

        private final HttpResponse<?> response;

        public Response(HttpResponse<?> response) {
            this.response = response;
        }

        public String asString() {
            Object body = response.body();
            if (body instanceof String) {
                return (String) body;
            }
            if (body instanceof byte[]) {
                return new String((byte[]) body, StandardCharsets.UTF_8);
            }
            return body.toString();
        }

        public byte[] asBytes() {
            Object body = response.body();
            if (body instanceof byte[]) {
                return (byte[]) body;
            }
            if (body instanceof String) {
                return ((String) body).getBytes(StandardCharsets.UTF_8);
            }
            return body.toString().getBytes(StandardCharsets.UTF_8);
        }

        public InputStream asStream() {
            return (InputStream) response.body();
        }

        public int statusCode() {
            return response.statusCode();
        }

        public boolean isSuccess() {
            return statusCode() >= 200 && statusCode() < 300;
        }

        public boolean isRedirect() {
            return statusCode() >= 300 && statusCode() < 400;
        }

        public boolean isClientError() {
            return statusCode() >= 400 && statusCode() < 500;
        }

        public boolean isServerError() {
            return statusCode() >= 500;
        }

        public String header(String name) {
            return response.headers().firstValue(name).orElse(null);
        }

        public String contentType() {
            return header("Content-Type");
        }

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

        public HttpResponse<?> getRawResponse() {
            return response;
        }

        @Override
        public String toString() {
            return String.format("Response{statusCode=%d, contentType='%s', bodyLength=%d}",
                    statusCode(), contentType(), contentLength());
        }
    }
}
