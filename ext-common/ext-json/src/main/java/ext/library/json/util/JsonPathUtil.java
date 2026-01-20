package ext.library.json.util;

import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import com.jayway.jsonpath.ReadContext;

/**
 * JsonPath 工具类
 * <p>
 * 基于 <a href="https://github.com/json-path/JsonPath">JsonPath</a> 封装，
 * 提供便捷的 JSON 路径查询能力。
 * <p>
 * 适用于一次性读取；多次读取建议使用 {@link #parse(String)} 获取 {@link ReadContext} 复用。
 */
public final class JsonPathUtil {

    private JsonPathUtil() {
    }

    /**
     * 解析 JSON 字符串为 ReadContext
     * <p>
     * 多次读取同一 JSON 时，应复用 ReadContext 以提升性能。
     *
     * @param json JSON 字符串
     * @return ReadContext 实例
     */
    public static ReadContext parse(String json) {
        return JsonPath.parse(json);
    }

    /**
     * 使用 JsonPath 读取指定路径的值
     * <p>
     * 适用于一次性读取，多次读取建议使用 {@link #parse(String)} 获取 ReadContext 复用。
     *
     * @param json JSON 字符串
     * @param path JsonPath 路径表达式
     * @param <T>  返回值类型
     * @return 路径对应的值
     */
    public static <T> T read(String json, String path) {
        return JsonPath.read(json, path);
    }

    /**
     * 检查指定路径是否存在
     *
     * @param json JSON 字符串
     * @param path JsonPath 路径表达式
     * @return 路径存在返回 true，否则返回 false
     */
    public static boolean hasPath(String json, String path) {
        return hasPath(parse(json), path);
    }

    /**
     * 检查指定路径是否存在
     *
     * @param context ReadContext 实例
     * @param path    JsonPath 路径表达式
     * @return 路径存在返回 true，否则返回 false
     */
    public static boolean hasPath(ReadContext context, String path) {
        try {
            context.read(path);
            return true;
        } catch (PathNotFoundException e) {
            return false;
        }
    }

    /**
     * 读取指定路径的值，路径不存在时返回默认值
     *
     * @param json         JSON 字符串
     * @param path         JsonPath 路径表达式
     * @param defaultValue 默认值
     * @param <T>          返回值类型
     * @return 路径对应的值，路径不存在时返回默认值
     */
    public static <T> T readOrDefault(String json, String path, T defaultValue) {
        return readOrDefault(parse(json), path, defaultValue);
    }

    /**
     * 读取指定路径的值，路径不存在时返回默认值
     *
     * @param context      ReadContext 实例
     * @param path         JsonPath 路径表达式
     * @param defaultValue 默认值
     * @param <T>          返回值类型
     * @return 路径对应的值，路径不存在时返回默认值
     */
    public static <T> T readOrDefault(ReadContext context, String path, T defaultValue) {
        try {
            T value = context.read(path);
            return value != null ? value : defaultValue;
        } catch (PathNotFoundException e) {
            return defaultValue;
        }
    }

}
