package ext.library.http;

import java.io.File;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import ext.library.json.util.JsonUtil;
import okhttp3.Cookie;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.jetbrains.annotations.NotNull;

/**
 * 响应接口
 */
public interface ResponseSpec {

    /**
     * Returns the HTTP code.
     *
     * @return code
     */
    int code();

    /**
     * Returns the HTTP status message.
     *
     * @return message
     */
    String message();

    /**
     * Returns the HTTP isSuccessful.
     *
     * @return boolean
     */
    default boolean isOk() {
        return false;
    }

    /**
     * Returns the HTTP is not successful.
     *
     * @return boolean
     */
    default boolean isNotOk() {
        return !isOk();
    }

    /**
     * Returns the is Redirect.
     *
     * @return is Redirect
     */
    boolean isRedirect();

    /**
     * Returns the Headers.
     *
     * @return Headers
     */
    Headers headers();

    /**
     * Returns the Headers value.
     *
     * @return Headers value
     */
    default String header(String name) {
        return this.headers().get(name);
    }

    /**
     * Returns the Headers value.
     *
     * @return Headers value
     */
    default <T> T header(@NotNull Function<Headers, T> consumer) {
        return consumer.apply(this.headers());
    }

    /**
     * Headers Consumer.
     *
     * @param consumer Consumer
     * @return Headers
     */
    default ResponseSpec headers(@NotNull Consumer<Headers> consumer) {
        consumer.accept(this.headers());
        return this;
    }

    /**
     * Returns the Cookies.
     *
     * @return Cookie List
     */
    List<Cookie> cookies();

    /**
     * Returns the Cookie value.
     *
     * @return Cookie
     */
    default Cookie cookie(String name) {
        for (Cookie cookie : cookies()) {
            if (cookie.name().equals(name)) {
                return cookie;
            }
        }
        return null;
    }

    /**
     * Returns the Cookie value.
     *
     * @return Cookie
     */
    default Cookie cookie(Predicate<Cookie> predicate) {
        for (Cookie cookie : cookies()) {
            if (predicate.test(cookie)) {
                return cookie;
            }
        }
        return null;
    }

    /**
     * 读取消费 cookie
     *
     * @param consumer Consumer
     * @return ResponseSpec
     */
    default ResponseSpec cookies(@NotNull Consumer<List<Cookie>> consumer) {
        consumer.accept(this.cookies());
        return this;
    }

    /**
     * Returns body String.
     *
     * @return body String
     */
    String asString();

    /**
     * Returns body String.
     *
     * @param charset Charset
     * @return body String
     */
    String asString(Charset charset);

    /**
     * Returns body to byte arrays.
     *
     * @return byte arrays
     */
    byte[] asBytes();

    /**
     * Returns body to InputStream.
     *
     * @return InputStream
     */
    InputStream asStream();

    /**
     * Returns body to JsonNode.
     *
     * @return JsonNode
     */
    JsonNode asJsonNode();

    /**
     * jackson json path 语法读取节点
     *
     * @param jsonPtrExpr json path 表达式
     * @return JsonNode
     */
    default JsonNode atJsonPath(String jsonPtrExpr) {
        return this.asJsonNode().at(jsonPtrExpr);
    }

    /**
     * jackson json path 语法读取节点
     *
     * @param jsonPtrExpr json path 表达式
     * @param valueType   value value type
     * @return JsonNode
     */
    default <T> T atJsonPathValue(String jsonPtrExpr, Class<T> valueType) {
        return JsonUtil.treeToObj(atJsonPath(jsonPtrExpr), valueType);
    }

    /**
     * jackson json path 语法读取节点
     *
     * @param jsonPtrExpr json path 表达式
     * @param valueType   value value type
     * @return JsonNode
     */
    default <T> T atJsonPathValue(String jsonPtrExpr, JavaType valueType) {
        return JsonUtil.treeToObj(atJsonPath(jsonPtrExpr), valueType);
    }

    /**
     * jackson json path 语法读取节点
     *
     * @param jsonPtrExpr   json path 表达式
     * @param typeReference value Type Reference
     * @return JsonNode
     */
    default <T> T atJsonPathValue(String jsonPtrExpr, TypeReference<T> typeReference) {
        return JsonUtil.convert(atJsonPath(jsonPtrExpr), typeReference);
    }

    /**
     * jackson json path 语法读取节点
     *
     * @param jsonPtrExpr json path 表达式
     * @param valueType   value value type
     * @return List
     */
    default <T> List<T> atJsonPathList(String jsonPtrExpr, Class<T> valueType) {
        return atJsonPathValue(jsonPtrExpr, JsonUtil.getListType(valueType));
    }

    /**
     * Returns body to Object.
     *
     * @param valueType value value type
     * @return Object
     */
    <T> T asValue(Class<T> valueType);

    /**
     * Returns body to Object.
     *
     * @param typeReference value Type Reference
     * @return Object
     */
    <T> T asValue(TypeReference<T> typeReference);

    /**
     * Returns body to List.
     *
     * @param valueType value type
     * @return List
     */
    <T> List<T> asList(Class<T> valueType);

    /**
     * Returns body to Map.
     *
     * @param keyClass  key type
     * @param valueType value type
     * @return Map
     */
    <K, V> Map<K, V> asMap(Class<?> keyClass, Class<?> valueType);

    /**
     * Returns body to Map.
     *
     * @param valueType value 类型
     * @return Map
     */
    <V> Map<String, V> asMap(Class<?> valueType);

    /**
     * 转换成文件上传 part
     *
     * @param name 表单名
     * @return Part
     */
    default MultipartBody.Part asPart(String name) {
        return asPart(name, null);
    }

    /**
     * 转换成文件上传 part
     *
     * @param name     表单名
     * @param fileName 文件名
     * @return Part
     */
    default MultipartBody.Part asPart(String name, String fileName) {
        RequestBody requestBody = RequestBody.create(asBytes(), contentType());
        return MultipartBody.Part.createFormData(name, fileName, requestBody);
    }

    /**
     * toFile.
     *
     * @param file File
     * @return File
     */
    File toFile(File file);

    /**
     * toFile.
     *
     * @param path Path
     * @return Path
     */
    Path toFile(Path path);

    /**
     * Returns contentType.
     *
     * @return contentType
     */
    MediaType contentType();

    /**
     * Returns contentLength.
     *
     * @return contentLength
     */
    long contentLength();

    /**
     * Returns rawRequest.
     *
     * @return Request
     */
    Request rawRequest();

    /**
     * rawRequest Consumer.
     *
     * @param consumer Consumer
     * @return ResponseSpec
     */
    default ResponseSpec rawRequest(@NotNull Consumer<Request> consumer) {
        consumer.accept(this.rawRequest());
        return this;
    }

    /**
     * Returns rawResponse.
     *
     * @return Response
     */
    Response rawResponse();

    /**
     * rawResponse Consumer.
     *
     * @param consumer Consumer
     * @return Response
     */
    default ResponseSpec rawResponse(@NotNull Consumer<Response> consumer) {
        consumer.accept(this.rawResponse());
        return this;
    }

    /**
     * Returns rawBody.
     *
     * @return ResponseBody
     */
    ResponseBody rawBody();

    /**
     * rawBody Consumer.
     *
     * @param consumer Consumer
     * @return ResponseBody
     */
    default ResponseSpec rawBody(@NotNull Consumer<ResponseBody> consumer) {
        consumer.accept(this.rawBody());
        return this;
    }

}
