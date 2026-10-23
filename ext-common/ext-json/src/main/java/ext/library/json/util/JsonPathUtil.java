package ext.library.json.util;

import com.jayway.jsonpath.InvalidJsonException;
import com.jayway.jsonpath.InvalidPathException;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.JsonPathException;
import com.jayway.jsonpath.PathNotFoundException;
import com.jayway.jsonpath.ReadContext;
import ext.library.tool.constant.EmojiSymbol;
import ext.library.tool.exception.ToolException;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * JsonPath 工具类
 * <p>
 * 基于 <a href="https://github.com/json-path/JsonPath">JsonPath</a> 封装，
 * 提供便捷的 JSON 路径查询能力。
 * <p>
 * 适用于一次性读取；多次读取建议使用 {@link #parse(String)} 获取 {@link ReadContext} 复用。
 * <p>
 * 所有异常统一包装为 {@link ToolException}；路径不存在返回默认值或 false，不抛异常。
 */
public final class JsonPathUtil {

    private JsonPathUtil() {
    }

    /**
     * 路径表达式编译缓存
     * <p>
     * JsonPath 路径通常来自代码常量，编译结果可复用，避免每次查询重复编译。
     */
    private static final Map<String, JsonPath> PATHS = new ConcurrentHashMap<>();

    /**
     * 获取编译后的路径表达式（带缓存）
     *
     * @param path JsonPath 路径表达式
     * @return 编译后的 JsonPath
     */
    private static JsonPath pathOf(String path) {
        JsonPath compiled = PATHS.get(path);
        if (compiled != null) {
            return compiled;
        }
        try {
            return PATHS.computeIfAbsent(path, JsonPath::compile);
        } catch (InvalidPathException e) {
            throw new ToolException(EmojiSymbol.JSON, e);
        }
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
        try {
            return JsonPath.parse(json);
        } catch (InvalidJsonException e) {
            throw new ToolException(EmojiSymbol.JSON, e);
        }
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
        return read(parse(json), path);
    }

    /**
     * 使用 JsonPath 读取指定路径的值
     * <p>
     * 适用于一次性读取，多次读取建议复用 ReadContext。
     *
     * @param context ReadContext 实例
     * @param path    JsonPath 路径表达式
     * @param <T>     返回值类型
     * @return 路径对应的值
     */
    public static <T> T read(ReadContext context, String path) {
        try {
            return context.read(pathOf(path));
        } catch (JsonPathException e) {
            // JsonPathException 是 PathNotFound/InvalidPath/InvalidJson 的公共父类
            throw new ToolException(EmojiSymbol.JSON, e);
        }
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
            context.read(pathOf(path));
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
            T value = context.read(pathOf(path));
            return value != null ? value : defaultValue;
        } catch (PathNotFoundException e) {
            return defaultValue;
        }
    }

}
