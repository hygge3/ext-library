package ext.library.json.util;

import ext.library.json.module.CustomModule;
import ext.library.tool.constant.Symbol;
import ext.library.tool.exception.ToolException;
import org.jspecify.annotations.Nullable;
import org.springframework.util.StringUtils;
import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.ser.FilterProvider;
import tools.jackson.databind.ser.std.SimpleBeanPropertyFilter;
import tools.jackson.databind.ser.std.SimpleFilterProvider;
import tools.jackson.databind.type.CollectionLikeType;
import tools.jackson.databind.type.MapType;
import tools.jackson.databind.type.TypeBase;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * JSON 工具类
 */
public class JsonUtil {
    protected static final JsonMapper mapper = JsonMapper.builder()
            // 注册自定义模块
            .addModule(new CustomModule()).build();

    // region java 类型转换获取

    /**
     * clazz 获取 JavaType
     *
     * @param clazz Class
     *
     * @return MapType
     */
    private static JavaType getType(Class<?> clazz) {
        return mapper.getTypeFactory().constructType(clazz);
    }

    /**
     * 封装 map type
     *
     * @param keyClass   key 类型
     * @param valueClass value 类型
     *
     * @return MapType
     */
    private static MapType getMapType(Class<?> keyClass, Class<?> valueClass) {
        return mapper.getTypeFactory().constructMapType(Map.class, keyClass, valueClass);
    }

    /**
     * 封装 map type
     *
     * @param elementClass 集合值类型
     *
     * @return CollectionLikeType
     */
    private static CollectionLikeType getListType(Class<?> elementClass) {
        return mapper.getTypeFactory().constructCollectionLikeType(List.class, elementClass);
    }

    // endregion

    // region java 序列化为 json

    /**
     * 将对象序列化成 json 字符串
     *
     * @param obj javaBean
     *
     * @return {@code String } json 字符串
     */
    public static String toJson(@Nullable Object obj) {
        if (Objects.isNull(obj)) {
            return Symbol.EMPTY;
        }
        if (obj instanceof String str) {
            return str;
        }
        if (obj instanceof BigDecimal bd) {
            return bd.toPlainString();
        }
        try {
            return mapper.writeValueAsString(obj);
        } catch (JacksonException e) {
            throw new ToolException(e);
        }
    }

    /**
     * 过滤指定字段
     */
    public static String toJsonWithFilter(Object obj, String filterName, String... fieldsToExclude) {
        try {
            FilterProvider filters = new SimpleFilterProvider().addFilter(filterName,
                    SimpleBeanPropertyFilter.serializeAllExcept(fieldsToExclude));
            return mapper.writer(filters).writeValueAsString(obj);
        } catch (JacksonException e) {
            throw new ToolException(e);
        }
    }

    /**
     * 将对象序列化成 json 字符串，格式美化
     *
     * @param obj javaBean
     *
     * @return jsonString json 字符串
     */
    public static String toPrettyJson(@Nullable Object obj) {
        if (Objects.isNull(obj)) {
            return Symbol.EMPTY;
        }
        if (obj instanceof String str) {
            return str;
        }
        if (obj instanceof BigDecimal bd) {
            return bd.toPlainString();
        }
        try {
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
        } catch (JacksonException e) {
            throw new ToolException(e);
        }
    }

    // endregion

    // region json 反序列化为 java

    private static <T> T readValue(String json, TypeBase toValueType) {
        try {
            return mapper.readValue(json, toValueType);
        } catch (JacksonException e) {
            throw new ToolException(e);
        }
    }

    /**
     * 将 json 反序列化成对象
     *
     * @param json      jsonString
     * @param valueType class
     * @param <T>       T 泛型标记
     *
     * @return Bean
     */
    public static <T> T readObj(String json, Class<T> valueType) {
        try {
            return mapper.readValue(json, valueType);
        } catch (JacksonException e) {
            throw new ToolException(e);
        }
    }

    /**
     * 将 json 反序列化成集合
     *
     * @param json         jsonString
     * @param elementClass 集合中的元素类型
     * @param <T>          泛型
     *
     * @return {@link List}<{@link T}>
     */
    public static <T> List<T> readList(String json, Class<T> elementClass) {
        try {
            return readValue(json, getListType(elementClass));
        } catch (JacksonException e) {
            throw new ToolException(e);
        }
    }

    /**
     * 将 json 反序列化成 Map 集合
     *
     * @param json jsonString
     *
     * @return {@link Map}<{@link String}, {@link Object}>
     */
    public static Map<String, Object> readMap(String json) {
        return readMap(json, String.class, Object.class);
    }

    /**
     * 将 json 反序列化成 Map 集合
     *
     * @param json       jsonString
     * @param valueClass 值类型
     * @param <V>        泛型
     *
     * @return {@link Map}<{@link String}, {@link Object}>
     */
    public static <V> Map<String, V> readMap(String json, Class<?> valueClass) {
        return readMap(json, String.class, valueClass);
    }

    /**
     * 将 json 反序列化成 Map 集合
     *
     * @param json       jsonString
     * @param keyClass   key 类型
     * @param valueClass 值类型
     * @param <K>        泛型
     * @param <V>        泛型
     *
     * @return {@link Map}<{@link String}, {@link Object}>
     */
    public static <K, V> Map<K, V> readMap(String json, Class<?> keyClass, Class<?> valueClass) {
        try {
            return readValue(json, getMapType(keyClass, valueClass));
        } catch (JacksonException e) {
            throw new ToolException(e);
        }
    }

    /**
     * JSON 字符串转复杂对象（如 Map 嵌套）
     */
    public static <T> T toComplexObject(String json, TypeReference<T> typeReference) {
        try {
            return mapper.readValue(json, typeReference);
        } catch (JacksonException e) {
            throw new ToolException(e);
        }
    }

    // endregion

    // region json 实现对象类型转换

    /**
     * jackson 的类型转换
     *
     * @param fromValue   来源对象
     * @param toValueType 转换的类型
     * @param <T>         泛型标记
     *
     * @return 转换结果
     */
    public static <T> T convert(Object fromValue, Class<T> toValueType) {
        return mapper.convertValue(fromValue, toValueType);
    }

    // endregion

    // region json 格式校验

    /**
     * 检验 json 格式
     *
     * @param json json 字符串
     *
     * @return 是否成功
     */
    public static boolean isValidJson(String json) {
        if (!StringUtils.hasText(json)) {
            return false;
        }
        try (var parser = mapper.createParser(json)) {
            // 读取一个 token 来验证基本结构
            parser.nextToken();
            return true;
        } catch (Throwable e) {
            return false;
        }
    }

    // endregion

}