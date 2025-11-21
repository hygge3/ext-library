package ext.library.json.util;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.ReadContext;
import com.jayway.jsonpath.TypeRef;
import com.jayway.jsonpath.spi.json.JacksonJsonProvider;
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider;

import java.util.List;
import java.util.Objects;

/**
 * JSON-Path 工具类
 */
public class JsonPathUtil {

    /**
     * 使用 JsonPath 读取指定内容
     *
     * @param json json 字符串
     * @param path 路径
     *
     * @return {@link T }
     */
    public static <T> T getObj(String json, String path) {
        return getContext(json).read(path);
    }

    /**
     * 使用 JsonPath 读取内容
     * <p>
     * 返回 JSONPath 读取对象
     *
     * @param json json 字符串
     *
     * @return {@link DocumentContext }
     */
    public static DocumentContext getContext(String json) {
        // 创建自定义配置
        Configuration configuration = Configuration.builder().jsonProvider(new JacksonJsonProvider(JsonUtil.mapper)).mappingProvider(new JacksonMappingProvider(JsonUtil.mapper)).options(Option.DEFAULT_PATH_LEAF_TO_NULL).build();

        return JsonPath.using(configuration).parse(json);
    }

    /**
     * 存在指定路径
     *
     * @param json json 内容
     * @param path 路径
     *
     * @return {@link ReadContext }
     */
    public static boolean has(String json, String path) {
        return Objects.nonNull(getContext(json).read(path));
    }

    /**
     * JSONPath 读取 (强制转换为接收类型)
     *
     * @param json json 内容
     *
     * @return {@link ReadContext }
     */
    public static <T> T get(String json, String path) {
        return getContext(json).read(path);
    }

    /**
     * JSONPath 读取对象
     *
     * @param json      json 内容
     * @param valueType value 类型
     *
     * @return {@link ReadContext }
     */
    public static <T> T getObj(String json, String path, Class<T> valueType) {
        return getContext(json).read(path, valueType);
    }

    /**
     * JSONPath 读取集合
     *
     * @param json         json 内容
     * @param elementClass 元素类型
     *
     * @return {@link ReadContext }
     */
    public static <T> List<T> getList(String json, String path, Class<T> elementClass) {
        return getContext(json).read(path, new TypeRef<List<T>>() {});
    }

    /**
     * 设置 JSON 对象中指定路径的值为新值，并返回更新后的 JSON 字符串。
     *
     * @param json     原始 JSON 字符串
     * @param path     要设置的 JSON 路径
     * @param newValue 新的值
     *
     * @return 更新后的 JSON 字符串
     */
    public static String set(String json, String path, Object newValue) {
        return getContext(json).set(path, newValue).jsonString();
    }

    /**
     * 将指定的值放入 JSON 对象中指定路径的键下，并返回修改后的 JSON 字符串。
     *
     * @param json     原始 JSON 字符串
     * @param jsonPath JSON 对象中的路径
     * @param key      要放入的键
     * @param value    要放入的值
     *
     * @return 修改后的 JSON 字符串
     */
    public static String put(String json, String jsonPath, String key, Object value) {
        return getContext(json).put(jsonPath, key, value).jsonString();
    }

}