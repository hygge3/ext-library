package ext.library.http;

import ext.library.tool.core.Exceptions;
import ext.library.tool.util.StringUtil;
import lombok.Getter;
import lombok.Setter;

import jakarta.annotation.Nonnull;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.File;
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
import java.util.Optional;
import java.util.Set;
import java.util.StringJoiner;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * JDK 的 HttpClient 工具类
 *
 * @since jdk11
 */
public class HttpUtil {

    /**
     * 获取 Http 客户端
     */
    @Getter
    private static volatile HttpClient client;
    private static HttpClientProps httpClientProps;

    // region init

    static {
        client = HttpClient.newBuilder()
                // http 协议版本 1.1 或者 2
                .version(HttpClient.Version.HTTP_1_1)
                // 连接超时时间，单位为毫秒
                .connectTimeout(Duration.ofMinutes(1))
                // 连接完成之后的转发策略
                .followRedirects(HttpClient.Redirect.ALWAYS)
                // 指定虚拟线程池
                .executor(Executors.newVirtualThreadPerTaskExecutor())
                // 认证，默认情况下 Authenticator.getDefault() 是 null 值，会报错
                //.authenticator(Authenticator.getDefault())
                // 代理地址
                //.proxy(ProxySelector.of(new InetSocketAddress("http://www.baidu.com", 8080)))
                // 缓存，默认情况下 CookieHandler.getDefault() 是 null 值，会报错
                //.cookieHandler(CookieHandler.getDefault())
                .build();
    }

    public HttpUtil(HttpClientProps httpClientProps) {
        if (client == null) {
            synchronized (HttpUtil.class) {
                if (client == null) {
                    HttpUtil.httpClientProps = httpClientProps;
                    HttpClient.Builder builder = HttpClient.newBuilder().version(httpClientProps.getVersion()).connectTimeout(Duration.ofMillis(httpClientProps.getConnectTimeout())).followRedirects(httpClientProps.getRedirect());
                    Optional.ofNullable(httpClientProps.getAuthenticator()).ifPresent(builder::authenticator);
                    Optional.ofNullable(httpClientProps.getCookieHandler()).ifPresent(builder::cookieHandler);
                    Optional.ofNullable(httpClientProps.getProxySelector()).ifPresent(builder::proxy);
                    Optional.ofNullable(httpClientProps.getExecutor()).ifPresent(builder::executor);
                    client = builder.build();
                }
            }
        }
    }
    // endregion
    // region GET

    /**
     * 同步 GET 请求，返回值解析为字符串
     *
     * @param url 网址
     *
     * @return {@code String }
     *
     * @throws IOException          IO 异常
     * @throws InterruptedException 中断异常
     */
    public static String get(String url) throws IOException, InterruptedException {
        return get(url, Map.of());
    }

    /**
     * 同步 GET 请求，返回值解析为字符串
     *
     * @param url       访问 URL
     * @param headerMap header 键值对
     *
     * @throws IOException          IO 异常
     * @throws InterruptedException 中断异常
     */
    public static String get(String url, Map<String, String> headerMap) throws IOException, InterruptedException {
        return get(url, headerMap, httpClientProps.getDefaultReadTimeout());
    }

    /**
     * 同步 GET 请求，返回值解析为字符串
     *
     * @param url       访问 URL
     * @param headerMap header 键值对
     * @param timeout   超时时间
     *
     * @throws IOException          IO 异常
     * @throws InterruptedException 中断异常
     */
    public static String get(String url, Map<String, String> headerMap, long timeout) throws IOException, InterruptedException {
        return get(url, headerMap, timeout, String.class);
    }

    /**
     * 同步 GET 请求，返回值支持的解析类型有 byte[]、String、InputStream
     *
     * @param url       访问 URL
     * @param headerMap header 键值对
     * @param timeout   超时时间
     * @param resClass  返回类型，支持 byte[].class、String.class、InputStream.class，其他类型会抛出 UnsupportedOperationException
     *
     * @throws IOException          IO 异常
     * @throws InterruptedException 中断异常
     */
    public static <T> T get(String url, Map<String, String> headerMap, long timeout, Class<T> resClass) throws IOException, InterruptedException {
        HttpRequest httpRequest = buildGetRequest(url, headerMap, timeout);
        return getResData(httpRequest, resClass);
    }

    /**
     * 同步 GET 请求，返回值支持的解析类型有 byte[]、String、InputStream
     *
     * @param url       访问 URL
     * @param headerMap header 键值对
     * @param timeout   超时时间
     * @param resClass  返回类型，支持 byte[].class、String.class、InputStream.class，其他类型会抛出 UnsupportedOperationException
     *
     * @return {@code HttpResponse<T> }
     *
     * @throws IOException          IO 异常
     * @throws InterruptedException 中断异常
     */
    public static <T> HttpResponse<T> getResponse(String url, Map<String, String> headerMap, long timeout, Class<T> resClass) throws IOException, InterruptedException {
        HttpRequest httpRequest = buildGetRequest(url, headerMap, timeout);
        return getRes(httpRequest, resClass);
    }

    /**
     * 同步 GET 请求，返回 byte[]
     *
     * @param url       访问 URL
     * @param headerMap header 键值对
     * @param timeout   超时时间
     *
     * @return java.util.concurrent.CompletableFuture<java.net.http.HttpResponse < byte [ ]>>
     */
    public static CompletableFuture<HttpResponse<byte[]>> getByteResponseAsync(String url, Map<String, String> headerMap, long timeout) {
        HttpRequest httpRequest = buildGetRequest(url, headerMap, timeout);
        return client.sendAsync(httpRequest, HttpResponse.BodyHandlers.ofByteArray());
    }

    /**
     * 同步 GET 请求，返回 String
     *
     * @param url       访问 URL
     * @param headerMap header 键值对
     * @param timeout   超时时间
     *
     * @return java.util.concurrent.CompletableFuture<java.net.http.HttpResponse < byte [ ]>>
     */
    public static CompletableFuture<HttpResponse<String>> getStringResponseAsync(String url, Map<String, String> headerMap, long timeout) {
        HttpRequest httpRequest = buildGetRequest(url, headerMap, timeout);
        return client.sendAsync(httpRequest, HttpResponse.BodyHandlers.ofString());
    }

    /**
     * 同步 GET 请求，返回 InputStream
     *
     * @param url       访问 URL
     * @param headerMap header 键值对
     * @param timeout   超时时间
     *
     * @return java.util.concurrent.CompletableFuture<java.net.http.HttpResponse < byte [ ]>>
     */
    public static CompletableFuture<HttpResponse<InputStream>> getInputStreamResponseAsync(String url, Map<String, String> headerMap, long timeout) {
        HttpRequest httpRequest = buildGetRequest(url, headerMap, timeout);
        return client.sendAsync(httpRequest, HttpResponse.BodyHandlers.ofInputStream());
    }

    // endregion
    // region POST

    /**
     * 同步 POST 请求，通过请求体传送数据
     *
     * @param url         访问 URL
     * @param requestBody 请求体
     *
     * @return java.net.http.HttpResponse<T>
     *
     * @throws IOException          IO 异常
     * @throws InterruptedException 中断异常
     */
    public static String post(String url, String requestBody) throws IOException, InterruptedException {
        return post(url, Map.of(), requestBody, httpClientProps.getDefaultReadTimeout());
    }

    /**
     * 同步 POST 请求，通过 form 传送数据
     *
     * @param url  访问 URL
     * @param form form 表单
     *
     * @return java.net.http.HttpResponse<T>
     *
     * @throws IOException          IO 异常
     * @throws InterruptedException 中断异常
     */
    public static String post(String url, Map<String, Object> form) throws IOException, InterruptedException {
        return post(url, Map.of(), form, httpClientProps.getDefaultReadTimeout());
    }

    /**
     * 同步 POST 请求，通过请求体传送数据
     *
     * @param url         访问 URL
     * @param headerMap   header 键值对
     * @param requestBody 请求体
     *
     * @return java.net.http.HttpResponse<T>
     *
     * @throws IOException          IO 异常
     * @throws InterruptedException 中断异常
     */
    public static String post(String url, Map<String, String> headerMap, String requestBody) throws IOException, InterruptedException {
        return post(url, headerMap, requestBody, httpClientProps.getDefaultReadTimeout());
    }

    /**
     * 同步 POST 请求，通过 Form 传送数据
     *
     * @param url       访问 URL
     * @param headerMap header 键值对
     * @param form      表单
     *
     * @return java.net.http.HttpResponse<T>
     *
     * @throws IOException          IO 异常
     * @throws InterruptedException 中断异常
     */
    public static String post(String url, Map<String, String> headerMap, Map<String, Object> form) throws IOException, InterruptedException {
        return post(url, headerMap, form, httpClientProps.getDefaultReadTimeout());
    }

    /**
     * 同步 POST 请求，通过请求体传送数据
     *
     * @param url         访问 URL
     * @param headerMap   header 键值对
     * @param requestBody 请求体
     * @param timeout     超时时间
     *
     * @return java.net.http.HttpResponse<T>
     *
     * @throws IOException          IO 异常
     * @throws InterruptedException 中断异常
     */
    public static String post(String url, Map<String, String> headerMap, String requestBody, long timeout) throws IOException, InterruptedException {
        return post(url, headerMap, requestBody, timeout, String.class);
    }

    /**
     * 同步 POST 请求，通过 FORM 传送数据
     *
     * @param url       访问 URL
     * @param headerMap header 键值对
     * @param form      表单
     * @param timeout   超时时间
     *
     * @return java.net.http.HttpResponse<T>
     *
     * @throws IOException          IO 异常
     * @throws InterruptedException 中断异常
     */
    public static String post(String url, Map<String, String> headerMap, Map<String, Object> form, long timeout) throws IOException, InterruptedException {
        return post(url, headerMap, form, timeout, String.class);
    }

    /**
     * 同步 POST 请求，通过请求体传送数据，返回值支持的解析类型有 byte[]、String、InputStream
     *
     * @param url         访问 URL
     * @param headerMap   header 键值对
     * @param requestBody 请求体
     * @param timeout     超时时间
     * @param resClass    返回类型，支持 byte[].class、String.class、InputStream.class，其他类型会抛出 UnsupportedOperationException
     *
     * @return java.net.http.HttpResponse<T>
     *
     * @throws IOException          IO 异常
     * @throws InterruptedException 中断异常
     */
    public static <T> T post(String url, Map<String, String> headerMap, String requestBody, long timeout, Class<T> resClass) throws IOException, InterruptedException {
        HttpRequest httpRequest = buildPostRequest(url, headerMap, requestBody, timeout);
        return getResData(httpRequest, resClass);
    }

    /**
     * 同步 POST 请求，通过 FORM 传送数据，返回值支持的解析类型有 byte[]、String、InputStream
     *
     * @param url       访问 URL
     * @param headerMap header 键值对
     * @param form      form 表单
     * @param timeout   超时时间
     * @param resClass  返回类型，支持 byte[].class、String.class、InputStream.class，其他类型会抛出 UnsupportedOperationException
     *
     * @return java.net.http.HttpResponse<T>
     *
     * @throws IOException          IO 异常
     * @throws InterruptedException 中断异常
     */
    public static <T> T post(String url, Map<String, String> headerMap, Map<String, Object> form, long timeout, Class<T> resClass) throws IOException, InterruptedException {
        return postResponse(url, headerMap, form, timeout, resClass).body();
    }

    /**
     * 同步 POST 请求，通过请求体传送数据，返回值支持的解析类型有 byte[]、String、InputStream
     *
     * @param url         访问 URL
     * @param headerMap   header 键值对
     * @param requestBody 请求体
     * @param timeout     超时时间
     * @param resClass    返回类型，支持 byte[].class、String.class、InputStream.class，其他类型会抛出 UnsupportedOperationException
     *
     * @return java.net.http.HttpResponse<T>
     *
     * @throws IOException          IO 异常
     * @throws InterruptedException 中断异常
     */
    public static <T> HttpResponse<T> postResponse(String url, Map<String, String> headerMap, String requestBody, long timeout, Class<T> resClass) throws IOException, InterruptedException {
        return postResponse(url, headerMap, HttpRequest.BodyPublishers.ofString(requestBody, StandardCharsets.UTF_8), timeout, resClass);
    }

    /**
     * 同步 POST 请求，通过 FORM 表单传送数据，返回值支持的解析类型有 byte[]、String、InputStream
     *
     * @param url       访问 URL
     * @param headerMap header 键值对
     * @param form      表单
     * @param timeout   超时时间
     * @param resClass  返回类型，支持 byte[].class、String.class、InputStream.class，其他类型会抛出 UnsupportedOperationException
     *
     * @return java.net.http.HttpResponse<T>
     *
     * @throws IOException          IO 异常
     * @throws InterruptedException 中断异常
     */
    public static <T> HttpResponse<T> postResponse(String url, Map<String, String> headerMap, Map<String, Object> form, long timeout, Class<T> resClass) throws IOException, InterruptedException {
        String[] headers = createHeader(headerMap, "application/x-www-form-urlencoded");
        Map<String, String> newHeader = new HashMap<>();
        for (int i = 0; i < headers.length; i = i + 2) {
            newHeader.put(headers[i], headers[i + 1]);
        }
        HttpRequest httpRequest = buildPostRequest(url, newHeader, form, timeout);
        return getRes(httpRequest, resClass);
    }

    /**
     * 同步 POST 请求，返回值支持的解析类型有 byte[]、String、InputStream
     *
     * @param url           访问 URL
     * @param headerMap     header 键值对
     * @param bodyPublisher 请求体
     * @param timeout       超时时间
     * @param resClass      返回类型，支持 byte[].class、String.class、InputStream.class，其他类型会抛出 UnsupportedOperationException
     *
     * @return java.net.http.HttpResponse<T>
     *
     * @throws IOException          IO 异常
     * @throws InterruptedException 中断异常
     */
    public static <T> HttpResponse<T> postResponse(String url, Map<String, String> headerMap, HttpRequest.BodyPublisher bodyPublisher, long timeout, Class<T> resClass) throws IOException, InterruptedException {
        HttpRequest httpRequest = buildPostRequest(url, headerMap, bodyPublisher, timeout);
        return getRes(httpRequest, resClass);
    }

    /**
     * 异步 POST 请求，通过 form 表单传送数据
     *
     * @param url       访问 URL
     * @param headerMap header 键值对
     * @param form      表单
     * @param timeout   超时时间
     *
     * @return java.net.http.HttpResponse<T>
     */
    public static CompletableFuture<HttpResponse<byte[]>> postByteResponseAsync(String url, Map<String, String> headerMap, Map<String, Object> form, long timeout) {
        HttpRequest httpRequest = buildPostRequest(url, headerMap, form, timeout);
        return client.sendAsync(httpRequest, HttpResponse.BodyHandlers.ofByteArray());
    }

    /**
     * 异步 POST 请求，通过 form 表单传送数据
     *
     * @param url       访问 URL
     * @param headerMap header 键值对
     * @param form      表单
     * @param timeout   超时时间
     *
     * @return java.net.http.HttpResponse<T>
     */
    public static CompletableFuture<HttpResponse<InputStream>> postInputStreamResponseAsync(String url, Map<String, String> headerMap, Map<String, Object> form, long timeout) {
        HttpRequest httpRequest = buildPostRequest(url, headerMap, form, timeout);
        return client.sendAsync(httpRequest, HttpResponse.BodyHandlers.ofInputStream());
    }

    /**
     * 异步 POST 请求，通过 form 表单传送数据
     *
     * @param url       访问 URL
     * @param headerMap header 键值对
     * @param form      表单
     * @param timeout   超时时间
     *
     * @return java.net.http.HttpResponse<T>
     */
    public static CompletableFuture<HttpResponse<String>> postStringResponseAsync(String url, Map<String, String> headerMap, Map<String, Object> form, long timeout) {
        HttpRequest httpRequest = buildPostRequest(url, headerMap, form, timeout);
        return client.sendAsync(httpRequest, HttpResponse.BodyHandlers.ofString());
    }

    /**
     * 异步 POST 请求，通过请求体传送数据
     *
     * @param url         访问 URL
     * @param headerMap   header 键值对
     * @param requestBody 请求体
     * @param timeout     超时时间
     *
     * @return java.net.http.HttpResponse<T>
     *
     * @throws IOException          IO 异常
     * @throws InterruptedException 中断异常
     */
    public static CompletableFuture<HttpResponse<byte[]>> postByteResponse(String url, Map<String, String> headerMap, String requestBody, long timeout) throws IOException, InterruptedException {
        HttpRequest httpRequest = buildPostRequest(url, headerMap, requestBody, timeout);
        return client.sendAsync(httpRequest, HttpResponse.BodyHandlers.ofByteArray());
    }

    /**
     * 异步 POST 请求，通过请求体传送数据
     *
     * @param url         访问 URL
     * @param headerMap   header 键值对
     * @param requestBody 请求体
     * @param timeout     超时时间
     *
     * @return java.net.http.HttpResponse<T>
     *
     * @throws IOException          IO 异常
     * @throws InterruptedException 中断异常
     */
    public static CompletableFuture<HttpResponse<InputStream>> postInputStreamResponse(String url, Map<String, String> headerMap, String requestBody, long timeout) throws IOException, InterruptedException {
        HttpRequest httpRequest = buildPostRequest(url, headerMap, requestBody, timeout);
        return client.sendAsync(httpRequest, HttpResponse.BodyHandlers.ofInputStream());
    }

    /**
     * 异步 POST 请求，通过请求体传送数据
     *
     * @param url         访问 URL
     * @param headerMap   header 键值对
     * @param requestBody 请求体
     * @param timeout     超时时间
     *
     * @return java.net.http.HttpResponse<T>
     *
     * @throws IOException          IO 异常
     * @throws InterruptedException 中断异常
     */
    public static CompletableFuture<HttpResponse<String>> postStringResponseAsync(String url, Map<String, String> headerMap, String requestBody, long timeout) throws IOException, InterruptedException {
        HttpRequest httpRequest = buildPostRequest(url, headerMap, requestBody, timeout);
        return client.sendAsync(httpRequest, HttpResponse.BodyHandlers.ofString());
    }

    // endregion
    // region Download

    /**
     * 同步下载文件，构建 httpRequest 的方式参见
     * {@link #buildGetRequest(String, Map, long)}
     * {@link #buildPostRequest(String, Map, String, long)}
     * {@link #buildPostRequest(String, Map, Map, long)}
     * {@link #buildPostRequest(String, Map, HttpRequest.BodyPublisher, long)}
     *
     * @param httpRequest 请求
     * @param filePath    文件路径
     *
     * @return {@code Path }
     *
     * @throws IOException          IO 异常
     * @throws InterruptedException 中断异常
     */
    public static Path download(HttpRequest httpRequest, String filePath) throws IOException, InterruptedException {
        HttpResponse<Path> httpResponse = client.send(httpRequest, HttpResponse.BodyHandlers.ofFile(new File(filePath).toPath()));
        return httpResponse.body();
    }

    /**
     * 同步下载文件，构建 httpRequest 的方式参见
     * {@link #buildGetRequest(String, Map, long)}
     * {@link #buildPostRequest(String, Map, String, long)}
     * {@link #buildPostRequest(String, Map, Map, long)}
     * {@link #buildPostRequest(String, Map, HttpRequest.BodyPublisher, long)}
     *
     * @param httpRequest 请求
     * @param filePath    文件路径
     *
     * @return {@code HttpResponse<Path> }
     *
     * @throws IOException          IO 异常
     * @throws InterruptedException 中断异常
     */
    public static HttpResponse<Path> downloadResponse(HttpRequest httpRequest, String filePath) throws IOException, InterruptedException {
        return client.send(httpRequest, HttpResponse.BodyHandlers.ofFile(new File(filePath).toPath()));
    }

    // endregion
    // region PUT

    /**
     * 同步 PUT 请求，通过请求体传送数据
     *
     * @param url         访问 URL
     * @param requestBody 请求体
     *
     * @return java.net.http.HttpResponse<T>
     *
     * @throws IOException          IO 异常
     * @throws InterruptedException 中断异常
     */
    public static String put(String url, String requestBody) throws IOException, InterruptedException {
        return put(url, Map.of(), requestBody, httpClientProps.getDefaultReadTimeout());
    }

    /**
     * 同步 PUT 请求，通过 form 传送数据
     *
     * @param url  访问 URL
     * @param form form 表单
     *
     * @return java.net.http.HttpResponse<T>
     *
     * @throws IOException          IO 异常
     * @throws InterruptedException 中断异常
     */
    public static String put(String url, Map<String, Object> form) throws IOException, InterruptedException {
        return put(url, Map.of(), form, httpClientProps.getDefaultReadTimeout());
    }

    /**
     * 同步 PUT 请求，通过请求体传送数据
     *
     * @param url         访问 URL
     * @param headerMap   header 键值对
     * @param requestBody 请求体
     *
     * @return java.net.http.HttpResponse<T>
     *
     * @throws IOException          IO 异常
     * @throws InterruptedException 中断异常
     */
    public static String put(String url, Map<String, String> headerMap, String requestBody) throws IOException, InterruptedException {
        return put(url, headerMap, requestBody, httpClientProps.getDefaultReadTimeout());
    }

    /**
     * 同步 PUT 请求，通过 Form 传送数据
     *
     * @param url       访问 URL
     * @param headerMap header 键值对
     * @param form      表单
     *
     * @return java.net.http.HttpResponse<T>
     *
     * @throws IOException          IO 异常
     * @throws InterruptedException 中断异常
     */
    public static String put(String url, Map<String, String> headerMap, Map<String, Object> form) throws IOException, InterruptedException {
        return put(url, headerMap, form, httpClientProps.getDefaultReadTimeout());
    }

    /**
     * 同步 PUT 请求，通过请求体传送数据
     *
     * @param url         访问 URL
     * @param headerMap   header 键值对
     * @param requestBody 请求体
     * @param timeout     超时时间
     *
     * @return java.net.http.HttpResponse<T>
     *
     * @throws IOException          IO 异常
     * @throws InterruptedException 中断异常
     */
    public static String put(String url, Map<String, String> headerMap, String requestBody, long timeout) throws IOException, InterruptedException {
        return put(url, headerMap, requestBody, timeout, String.class);
    }

    /**
     * 同步 PUT 请求，通过 FORM 传送数据
     *
     * @param url       访问 URL
     * @param headerMap header 键值对
     * @param form      表单
     * @param timeout   超时时间
     *
     * @return java.net.http.HttpResponse<T>
     *
     * @throws IOException          IO 异常
     * @throws InterruptedException 中断异常
     */
    public static String put(String url, Map<String, String> headerMap, Map<String, Object> form, long timeout) throws IOException, InterruptedException {
        return put(url, headerMap, form, timeout, String.class);
    }

    /**
     * 同步 PUT 请求，通过请求体传送数据，返回值支持的解析类型有 byte[]、String、InputStream
     *
     * @param url         访问 URL
     * @param headerMap   header 键值对
     * @param requestBody 请求体
     * @param timeout     超时时间
     * @param resClass    返回类型，支持 byte[].class、String.class、InputStream.class，其他类型会抛出 UnsupportedOperationException
     *
     * @return java.net.http.HttpResponse<T>
     *
     * @throws IOException          IO 异常
     * @throws InterruptedException 中断异常
     */
    public static <T> T put(String url, Map<String, String> headerMap, String requestBody, long timeout, Class<T> resClass) throws IOException, InterruptedException {
        HttpRequest httpRequest = buildPutRequest(url, headerMap, requestBody, timeout);
        return getResData(httpRequest, resClass);
    }

    /**
     * 同步 PUT 请求，通过 FORM 传送数据，返回值支持的解析类型有 byte[]、String、InputStream
     *
     * @param url       访问 URL
     * @param headerMap header 键值对
     * @param form      form 表单
     * @param timeout   超时时间
     * @param resClass  返回类型，支持 byte[].class、String.class、InputStream.class，其他类型会抛出 UnsupportedOperationException
     *
     * @return java.net.http.HttpResponse<T>
     *
     * @throws IOException          IO 异常
     * @throws InterruptedException 中断异常
     */
    public static <T> T put(String url, Map<String, String> headerMap, Map<String, Object> form, long timeout, Class<T> resClass) throws IOException, InterruptedException {
        return putResponse(url, headerMap, form, timeout, resClass).body();
    }

    /**
     * 同步 Put 请求，通过请求体传送数据，返回值支持的解析类型有 byte[]、String、InputStream
     *
     * @param url         访问 URL
     * @param headerMap   header 键值对
     * @param requestBody 请求体
     * @param timeout     超时时间
     * @param resClass    返回类型，支持 byte[].class、String.class、InputStream.class，其他类型会抛出 UnsupportedOperationException
     *
     * @return java.net.http.HttpResponse<T>
     *
     * @throws IOException          IO 异常
     * @throws InterruptedException 中断异常
     */
    public static <T> HttpResponse<T> putResponse(String url, Map<String, String> headerMap, String requestBody, long timeout, Class<T> resClass) throws IOException, InterruptedException {
        return putResponse(url, headerMap, HttpRequest.BodyPublishers.ofString(requestBody, StandardCharsets.UTF_8), timeout, resClass);
    }

    /**
     * 同步 Put 请求，通过 FORM 表单传送数据，返回值支持的解析类型有 byte[]、String、InputStream
     *
     * @param url       访问 URL
     * @param headerMap header 键值对
     * @param form      表单
     * @param timeout   超时时间
     * @param resClass  返回类型，支持 byte[].class、String.class、InputStream.class，其他类型会抛出 UnsupportedOperationException
     *
     * @return java.net.http.HttpResponse<T>
     *
     * @throws IOException          IO 异常
     * @throws InterruptedException 中断异常
     */
    public static <T> HttpResponse<T> putResponse(String url, Map<String, String> headerMap, Map<String, Object> form, long timeout, Class<T> resClass) throws IOException, InterruptedException {
        String[] headers = createHeader(headerMap, "application/x-www-form-urlencoded");
        Map<String, String> newHeader = new HashMap<>();
        for (int i = 0; i < headers.length; i = i + 2) {
            newHeader.put(headers[i], headers[i + 1]);
        }
        HttpRequest httpRequest = buildPutRequest(url, newHeader, form, timeout);
        return getRes(httpRequest, resClass);
    }

    /**
     * 同步 Put 请求，返回值支持的解析类型有 byte[]、String、InputStream
     *
     * @param url           访问 URL
     * @param headerMap     header 键值对
     * @param bodyPublisher 请求体
     * @param timeout       超时时间
     * @param resClass      返回类型，支持 byte[].class、String.class、InputStream.class，其他类型会抛出 UnsupportedOperationException
     *
     * @return java.net.http.HttpResponse<T>
     *
     * @throws IOException          IO 异常
     * @throws InterruptedException 中断异常
     */
    public static <T> HttpResponse<T> putResponse(String url, Map<String, String> headerMap, HttpRequest.BodyPublisher bodyPublisher, long timeout, Class<T> resClass) throws IOException, InterruptedException {
        HttpRequest httpRequest = buildPutRequest(url, headerMap, bodyPublisher, timeout);
        return getRes(httpRequest, resClass);
    }

    /**
     * 异步 PUT 请求，通过 form 表单传送数据
     *
     * @param url       访问 URL
     * @param headerMap header 键值对
     * @param form      表单
     * @param timeout   超时时间
     *
     * @return java.net.http.HttpResponse<T>
     */
    public static CompletableFuture<HttpResponse<byte[]>> putByteResponseAsync(String url, Map<String, String> headerMap, Map<String, Object> form, long timeout) {
        HttpRequest httpRequest = buildPutRequest(url, headerMap, form, timeout);
        return client.sendAsync(httpRequest, HttpResponse.BodyHandlers.ofByteArray());
    }

    /**
     * 异步 PUT 请求，通过 form 表单传送数据
     *
     * @param url       访问 URL
     * @param headerMap header 键值对
     * @param form      表单
     * @param timeout   超时时间
     *
     * @return java.net.http.HttpResponse<T>
     */
    public static CompletableFuture<HttpResponse<InputStream>> putInputStreamResponseAsync(String url, Map<String, String> headerMap, Map<String, Object> form, long timeout) {
        HttpRequest httpRequest = buildPutRequest(url, headerMap, form, timeout);
        return client.sendAsync(httpRequest, HttpResponse.BodyHandlers.ofInputStream());
    }

    /**
     * 异步 PUT 请求，通过 form 表单传送数据
     *
     * @param url       访问 URL
     * @param headerMap header 键值对
     * @param form      表单
     * @param timeout   超时时间
     *
     * @return java.net.http.HttpResponse<T>
     */
    public static CompletableFuture<HttpResponse<String>> putStringResponseAsync(String url, Map<String, String> headerMap, Map<String, Object> form, long timeout) {
        HttpRequest httpRequest = buildPutRequest(url, headerMap, form, timeout);
        return client.sendAsync(httpRequest, HttpResponse.BodyHandlers.ofString());
    }

    /**
     * 异步 PUT 请求，通过请求体传送数据
     *
     * @param url         访问 URL
     * @param headerMap   header 键值对
     * @param requestBody 请求体
     * @param timeout     超时时间
     *
     * @return java.net.http.HttpResponse<T>
     *
     * @throws IOException          IO 异常
     * @throws InterruptedException 中断异常
     */
    public static CompletableFuture<HttpResponse<byte[]>> putByteResponse(String url, Map<String, String> headerMap, String requestBody, long timeout) throws IOException, InterruptedException {
        HttpRequest httpRequest = buildPutRequest(url, headerMap, requestBody, timeout);
        return client.sendAsync(httpRequest, HttpResponse.BodyHandlers.ofByteArray());
    }

    /**
     * 异步 Put 请求，通过请求体传送数据
     *
     * @param url         访问 URL
     * @param headerMap   header 键值对
     * @param requestBody 请求体
     * @param timeout     超时时间
     *
     * @return java.net.http.HttpResponse<T>
     *
     * @throws IOException          IO 异常
     * @throws InterruptedException 中断异常
     */
    public static CompletableFuture<HttpResponse<InputStream>> putInputStreamResponse(String url, Map<String, String> headerMap, String requestBody, long timeout) throws IOException, InterruptedException {
        HttpRequest httpRequest = buildPutRequest(url, headerMap, requestBody, timeout);
        return client.sendAsync(httpRequest, HttpResponse.BodyHandlers.ofInputStream());
    }

    /**
     * 异步 Put 请求，通过请求体传送数据
     *
     * @param url         访问 URL
     * @param headerMap   header 键值对
     * @param requestBody 请求体
     * @param timeout     超时时间
     *
     * @return java.net.http.HttpResponse<T>
     *
     * @throws IOException          IO 异常
     * @throws InterruptedException 中断异常
     */
    public static CompletableFuture<HttpResponse<String>> putStringResponseAsync(String url, Map<String, String> headerMap, String requestBody, long timeout) throws IOException, InterruptedException {
        HttpRequest httpRequest = buildPutRequest(url, headerMap, requestBody, timeout);
        return client.sendAsync(httpRequest, HttpResponse.BodyHandlers.ofString());
    }

    // endregion
    // region DELETE

    /**
     * 删除
     *
     * @param url 访问 URL
     *
     * @return {@code String }
     *
     * @throws IOException          IO 异常
     * @throws InterruptedException 中断异常
     */
    public static String delete(String url) throws IOException, InterruptedException {
        return delete(url, Map.of());
    }

    /**
     * 删除
     *
     * @param url       访问 URL
     * @param headerMap header 键值对
     *
     * @return {@code String }
     *
     * @throws IOException          IO 异常
     * @throws InterruptedException 中断异常
     */
    public static String delete(String url, Map<String, String> headerMap) throws IOException, InterruptedException {
        return delete(url, headerMap, httpClientProps.getDefaultReadTimeout());
    }

    /**
     * 删除
     *
     * @param url       访问 URL
     * @param headerMap header 键值对
     * @param timeout   超时时间
     *
     * @return {@code String }
     *
     * @throws IOException          IO 异常
     * @throws InterruptedException 中断异常
     */
    public static String delete(String url, Map<String, String> headerMap, long timeout) throws IOException, InterruptedException {
        return delete(url, headerMap, timeout, String.class);
    }

    /**
     * 删除
     *
     * @param url       访问 URL
     * @param headerMap header 键值对
     * @param timeout   超时时间
     * @param resClass  返回类型，支持 byte[].class、String.class、InputStream.class，其他类型会抛出 UnsupportedOperationException
     *
     * @return {@code T }
     *
     * @throws IOException          IO 异常
     * @throws InterruptedException 中断异常
     */
    public static <T> T delete(String url, Map<String, String> headerMap, long timeout, Class<T> resClass) throws IOException, InterruptedException {
        HttpRequest httpRequest = buildDeleteRequest(url, headerMap, timeout);
        return getResData(httpRequest, resClass);
    }

    /**
     * 同步 DELETE 请求，返回值支持的解析类型有 byte[]、String、InputStream
     *
     * @param url       访问 URL
     * @param headerMap header 键值对
     * @param timeout   超时时间
     * @param resClass  返回类型，支持 byte[].class、String.class、InputStream.class，其他类型会抛出 UnsupportedOperationException
     *
     * @return java.net.http.HttpResponse<T>
     *
     * @throws IOException          IO 异常
     * @throws InterruptedException 中断异常
     */
    public static <T> HttpResponse<T> deleteResponse(String url, Map<String, String> headerMap, long timeout, Class<T> resClass) throws IOException, InterruptedException {
        HttpRequest httpRequest = buildDeleteRequest(url, headerMap, timeout);
        return getRes(httpRequest, resClass);
    }

    /**
     * 同步 DELETE 请求，返回 byte[]
     *
     * @param url       访问 URL
     * @param headerMap header 键值对
     * @param timeout   超时时间
     *
     * @return java.util.concurrent.CompletableFuture<java.net.http.HttpResponse < byte [ ]>>
     */
    public static CompletableFuture<HttpResponse<byte[]>> deleteByteResponseAsync(String url, Map<String, String> headerMap, long timeout) {
        HttpRequest httpRequest = buildDeleteRequest(url, headerMap, timeout);
        return client.sendAsync(httpRequest, HttpResponse.BodyHandlers.ofByteArray());
    }

    /**
     * 同步 DELETE 请求，返回 String
     *
     * @param url       访问 URL
     * @param headerMap header 键值对
     * @param timeout   超时时间
     *
     * @return java.util.concurrent.CompletableFuture<java.net.http.HttpResponse < byte [ ]>>
     */
    public static CompletableFuture<HttpResponse<String>> deleteStringResponseAsync(String url, Map<String, String> headerMap, long timeout) {
        HttpRequest httpRequest = buildDeleteRequest(url, headerMap, timeout);
        return client.sendAsync(httpRequest, HttpResponse.BodyHandlers.ofString());
    }

    /**
     * 同步 Delete 请求，返回 InputStream
     *
     * @param url       访问 URL
     * @param headerMap header 键值对
     * @param timeout   超时时间
     *
     * @return java.util.concurrent.CompletableFuture<java.net.http.HttpResponse < byte [ ]>>
     */
    public static CompletableFuture<HttpResponse<InputStream>> deleteInputStreamResponseAsync(String url, Map<String, String> headerMap, long timeout) {
        HttpRequest httpRequest = buildDeleteRequest(url, headerMap, timeout);
        return client.sendAsync(httpRequest, HttpResponse.BodyHandlers.ofInputStream());
    }

    // endregion
    // region common methods

    @SuppressWarnings("unchecked")
    private static <T> T getResData(HttpRequest httpRequest, Class<T> resClass) throws IOException, InterruptedException {
        T t;
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
    }

    @SuppressWarnings("unchecked")
    private static <T> HttpResponse<T> getRes(HttpRequest httpRequest, Class<T> resClass) throws IOException, InterruptedException {
        HttpResponse<T> response;
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
    }

    // region 构建请求

    public static HttpRequest buildGetRequest(String url, Map<String, String> headerMap, long timeout) {
        if (timeout <= 0) {
            timeout = httpClientProps.defaultReadTimeout;
        }
        Duration duration = Duration.ofMillis(timeout);
        return HttpRequest.newBuilder().GET()
                .headers(createHeader(headerMap, httpClientProps.defaultContentType))
                .uri(URI.create(url)).timeout(duration).build();
    }

    public static HttpRequest buildDeleteRequest(String url, Map<String, String> headerMap, long timeout) {
        if (timeout <= 0) {
            timeout = httpClientProps.defaultReadTimeout;
        }
        Duration duration = Duration.ofMillis(timeout);
        return HttpRequest.newBuilder().DELETE()
                .headers(createHeader(headerMap, httpClientProps.defaultContentType))
                .uri(URI.create(url)).timeout(duration).build();
    }

    public static HttpRequest buildPostRequest(String url, Map<String, String> headerMap, @Nonnull Map<String, Object> form, long timeout) {
        StringJoiner sj = new StringJoiner("&");
        form.forEach((k, v) -> sj.add(k + "=" + v.toString()));
        HttpRequest.BodyPublisher bodyPublisher = HttpRequest.BodyPublishers.ofString(sj.toString(), StandardCharsets.UTF_8);
        return buildPostRequest(url, headerMap, bodyPublisher, timeout);
    }

    public static HttpRequest buildPutRequest(String url, Map<String, String> headerMap, @Nonnull Map<String, Object> form, long timeout) {
        StringJoiner sj = new StringJoiner("&");
        form.forEach((k, v) -> sj.add(k + "=" + v.toString()));
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
            timeout = httpClientProps.defaultReadTimeout;
        }
        Duration duration = Duration.ofMillis(timeout);
        return HttpRequest.newBuilder().POST(bodyPublisher)
                .headers(createHeader(headerMap, httpClientProps.defaultContentType))
                .uri(URI.create(url)).timeout(duration).build();
    }

    public static HttpRequest buildPutRequest(String url, Map<String, String> headerMap, HttpRequest.BodyPublisher bodyPublisher, long timeout) {
        if (timeout <= 0) {
            timeout = httpClientProps.defaultReadTimeout;
        }
        Duration duration = Duration.ofMillis(timeout);
        return HttpRequest.newBuilder().PUT(bodyPublisher)
                .headers(createHeader(headerMap, httpClientProps.defaultContentType))
                .uri(URI.create(url)).timeout(duration).build();
    }

    // endregion

    private static String[] createHeader(Map<String, String> headerMap, String contentType) {
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
    // endregion

    @Setter
    @Getter
    public static class HttpClientProps {

        /**
         * http 版本
         */
        private HttpClient.Version version = HttpClient.Version.HTTP_1_1;

        /**
         * 转发策略
         */
        private HttpClient.Redirect redirect = HttpClient.Redirect.NORMAL;

        /**
         * 线程池
         */
        private Executor executor;

        /**
         * 认证
         */
        private Authenticator authenticator;

        /**
         * 代理
         */
        private ProxySelector proxySelector;

        /**
         * cookiehandler
         */
        private CookieHandler cookieHandler;

        /**
         * 连接超时时间毫秒
         */
        private int connectTimeout = 10000;

        /**
         * 默认读取数据超时时间
         */
        private int defaultReadTimeout = 1200000;

        /**
         * 默认 content-type
         */
        private String defaultContentType = "application/json";

        public HttpClientProps() {
            TrustManager[] trustAllCertificates = new TrustManager[]{new X509TrustManager() {
                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return null; // Not relevant.
                }

                @Override
                public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
                    // TODO Auto-generated method stub
                }

                @Override
                public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
                    // TODO Auto-generated method stub
                }
            }};
            SSLParameters sslParameters = new SSLParameters();
            sslParameters.setEndpointIdentificationAlgorithm("");

            try {
                SSLContext sslContext = SSLContext.getInstance("TLS");
                System.setProperty("jdk.internal.httpclient.disableHostnameVerification", "true");// 取消主机名验证
                sslContext.init(null, trustAllCertificates, new SecureRandom());
            } catch (NoSuchAlgorithmException | KeyManagementException e) {
                Exceptions.log(e);
            }
        }
    }
}