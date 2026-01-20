package ext.library.json.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import ext.library.json.module.ExtJacksonModule;
import ext.library.tool.constant.EmojiSymbol;
import ext.library.tool.exception.ToolException;
import org.jspecify.annotations.Nullable;
import org.springframework.util.StringUtils;
import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.SerializationFeature;
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

/**
 * JSON 工具类
 */
public final class JsonUtil {

    private static final JsonMapper MAPPER = JsonMapper.builder()
            // 注册扩展模块
            .addModule(new ExtJacksonModule())
            // 序列化配置：不序列化空值/空集合
            .changeDefaultPropertyInclusion(incl -> incl.withValueInclusion(JsonInclude.Include.NON_EMPTY))
            // 序列化配置：空对象不报错
            .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
            // 反序列化配置：忽略未知属性
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            // 反序列化配置：浮点数使用 BigDecimal（避免精度丢失）
            .enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS)
            .build();

    private JsonUtil() {
    }

    /**
     * 获取 JsonMapper 实例
     *
     * @return JsonMapper
     */
    public static JsonMapper getMapper() {
        return MAPPER;
    }

    // region 类型构造

    private static MapType getMapType(Class<?> keyClass, Class<?> valueClass) {
        return MAPPER.getTypeFactory().constructMapType(Map.class, keyClass, valueClass);
    }

    private static CollectionLikeType getListType(Class<?> elementClass) {
        return MAPPER.getTypeFactory().constructCollectionLikeType(List.class, elementClass);
    }

    // endregion

    // region 序列化

    /**
     * 将对象序列化成 JSON 字符串
     *
     * @param obj 对象
     * @return JSON 字符串，null 返回空字符串
     */
    public static String toJson(@Nullable Object obj) {
        return serialize(obj, false);
    }

    /**
     * 将对象序列化成格式化的 JSON 字符串
     *
     * @param obj 对象
     * @return 格式化的 JSON 字符串，null 返回空字符串
     */
    public static String toPrettyJson(@Nullable Object obj) {
        return serialize(obj, true);
    }

    /**
     * 序列化对象并排除指定字段
     * <p>
     * 需要在目标类上添加 {@code @JsonFilter("filterName")} 注解
     *
     * @param obj             对象
     * @param filterName      过滤器名称（需与类上的 @JsonFilter 值一致）
     * @param fieldsToExclude 要排除的字段名
     * @return JSON 字符串
     */
    public static String toJsonExcluding(Object obj, String filterName, String... fieldsToExclude) {
        try {
            FilterProvider filters = new SimpleFilterProvider()
                    .addFilter(filterName, SimpleBeanPropertyFilter.serializeAllExcept(fieldsToExclude));
            return MAPPER.writer(filters).writeValueAsString(obj);
        } catch (JacksonException e) {
            throw new ToolException(EmojiSymbol.JSON, e);
        }
    }

    private static String serialize(@Nullable Object obj, boolean pretty) {
        if (obj == null) {
            return "";
        }
        if (obj instanceof String str) {
            return str;
        }
        if (obj instanceof BigDecimal bd) {
            return bd.toPlainString();
        }
        try {
            return pretty
                    ? MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(obj)
                    : MAPPER.writeValueAsString(obj);
        } catch (JacksonException e) {
            throw new ToolException(EmojiSymbol.JSON, e);
        }
    }

    // endregion

    // region 反序列化

    /**
     * JSON 字符串反序列化为对象
     *
     * @param json      JSON 字符串
     * @param valueType 目标类型
     * @param <T>       泛型
     * @return 反序列化对象
     */
    public static <T> T readObj(String json, Class<T> valueType) {
        try {
            return MAPPER.readValue(json, valueType);
        } catch (JacksonException e) {
            throw new ToolException(EmojiSymbol.JSON, e);
        }
    }

    /**
     * JSON 字符串反序列化为 List
     *
     * @param json         JSON 字符串
     * @param elementClass 元素类型
     * @param <T>          泛型
     * @return List 集合
     */
    public static <T> List<T> readList(String json, Class<T> elementClass) {
        return readValue(json, getListType(elementClass));
    }

    /**
     * JSON 字符串反序列化为 Map
     *
     * @param json JSON 字符串
     * @return Map 集合
     */
    public static Map<String, Object> readMap(String json) {
        return readMap(json, String.class, Object.class);
    }

    /**
     * JSON 字符串反序列化为 Map
     *
     * @param json       JSON 字符串
     * @param valueClass 值类型
     * @param <V>        值泛型
     * @return Map 集合
     */
    public static <V> Map<String, V> readMap(String json, Class<V> valueClass) {
        return readMap(json, String.class, valueClass);
    }

    /**
     * JSON 字符串反序列化为 Map
     *
     * @param json       JSON 字符串
     * @param keyClass   键类型
     * @param valueClass 值类型
     * @param <K>        键泛型
     * @param <V>        值泛型
     * @return Map 集合
     */
    public static <K, V> Map<K, V> readMap(String json, Class<K> keyClass, Class<V> valueClass) {
        return readValue(json, getMapType(keyClass, valueClass));
    }

    /**
     * JSON 字符串反序列化为复杂泛型对象
     * <p>
     * 示例: {@code readGeneric(json, new TypeReference<List<Map<String, User>>>() {})}
     *
     * @param json          JSON 字符串
     * @param typeReference 类型引用
     * @param <T>           泛型
     * @return 反序列化对象
     */
    public static <T> T readGeneric(String json, TypeReference<T> typeReference) {
        try {
            return MAPPER.readValue(json, typeReference);
        } catch (JacksonException e) {
            throw new ToolException(EmojiSymbol.JSON, e);
        }
    }

    private static <T> T readValue(String json, TypeBase toValueType) {
        try {
            return MAPPER.readValue(json, toValueType);
        } catch (JacksonException e) {
            throw new ToolException(EmojiSymbol.JSON, e);
        }
    }

    // endregion

    // region 类型转换

    /**
     * 对象类型转换
     * <p>
     * 通过 JSON 序列化/反序列化实现类型转换
     *
     * @param fromValue   源对象
     * @param toValueType 目标类型
     * @param <T>         泛型
     * @return 转换后的对象
     */
    public static <T> T convert(Object fromValue, Class<T> toValueType) {
        return MAPPER.convertValue(fromValue, toValueType);
    }

    // endregion

    // region 校验

    /**
     * 校验 JSON 格式是否有效
     *
     * @param json JSON 字符串
     * @return 格式有效返回 true，否则返回 false
     */
    public static boolean isValidJson(String json) {
        if (!StringUtils.hasText(json)) {
            return false;
        }
        try (var parser = MAPPER.createParser(json)) {
            parser.nextToken();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // endregion

}
