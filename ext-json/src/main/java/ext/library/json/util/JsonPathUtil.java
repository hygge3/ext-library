package ext.library.json.util;

import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.ReadContext;

import java.util.Map;
import java.util.Objects;

/**
 * JSON-Path 工具类
 */
public class JsonPathUtil {

    /**
     * 使用 JsonPath 读取指定内容
     * <p>
     * 适用于一次性读取使用，多次读取建议使用原 SDK, <a href="https://github.com/json-path/JsonPath">查看文档</a>
     *
     * @param json json 字符串
     * @param path 路径
     *
     * @return {@link T }
     */
    public static <T> T readPath(String json, String path) {
        return readPath(json).read(path);
    }

    /**
     * 使用 JsonPath 读取内容
     * <p>
     * 返回 JSONPath 读取对象
     *
     * @param json json 字符串
     *
     * @return {@link ReadContext }
     */
    public static ReadContext readPath(String json) {
        return JsonPath.parse(json);
    }

    /**
     * 存在指定路径
     *
     * @param context json 内容
     * @param path    路径
     *
     * @return {@link ReadContext }
     */
    public static Boolean existPath(ReadContext context, String path) {
        return Objects.nonNull(context.read(path));
    }

    /**
     * JSONPath 读取对象
     *
     * @param context   内容
     * @param valueType value 类型
     *
     * @return {@link ReadContext }
     */
    public static <T> T readObj(ReadContext context, Class<T> valueType) {
        return JsonUtil.readObj(context.jsonString(), valueType);
    }

    /**
     * JSONPath 读取对象
     *
     * @param context 内容
     *
     * @return {@link ReadContext }
     */
    public static Map<String, Object> readMap(ReadContext context) {
        return JsonUtil.readMap(context.jsonString());
    }

}