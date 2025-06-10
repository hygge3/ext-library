package ext.library.json.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionLikeType;
import com.fasterxml.jackson.databind.type.MapType;
import ext.library.tool.$;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

/**
 * JSON 工具类
 */
@UtilityClass
public class JsonUtil {

    // region java 类型转换获取

    /**
     * clazz 获取 JavaType
     *
     * @param clazz Class
     * @return MapType
     */
    public JavaType getType(Class<?> clazz) {
        return CustomizeMapper.MAPPER.getTypeFactory().constructType(clazz);
    }

    /**
     * 封装 map type，keyClass String
     *
     * @param valueClass value 类型
     * @return MapType
     */
    public MapType getMapType(Class<?> valueClass) {
        return getMapType(String.class, valueClass);
    }

    /**
     * 封装 map type
     *
     * @param keyClass   key 类型
     * @param valueClass value 类型
     * @return MapType
     */
    public MapType getMapType(Class<?> keyClass, Class<?> valueClass) {
        return CustomizeMapper.MAPPER.getTypeFactory().constructMapType(Map.class, keyClass, valueClass);
    }

    /**
     * 封装 map type
     *
     * @param elementClass 集合值类型
     * @return CollectionLikeType
     */
    public CollectionLikeType getListType(Class<?> elementClass) {
        return CustomizeMapper.MAPPER.getTypeFactory().constructCollectionLikeType(List.class, elementClass);
    }

    /**
     * 封装参数化类型
     *
     * <p>
     * 例如：Map.class, String.class, String.class 对应 Map[String, String]
     * </p>
     *
     * @param parametrized     泛型参数化
     * @param parameterClasses 泛型参数类型
     * @return JavaType
     */
    public JavaType getParametricType(Class<?> parametrized, Class<?>... parameterClasses) {
        return CustomizeMapper.MAPPER.getTypeFactory().constructParametricType(parametrized, parameterClasses);
    }

    /**
     * 封装参数化类型，用来构造复杂的泛型
     *
     * <p>
     * 例如：Map.class, String.class, String.class 对应 Map[String, String]
     * </p>
     *
     * @param parametrized   泛型参数化
     * @param parameterTypes 泛型参数类型
     * @return JavaType
     */
    public JavaType getParametricType(Class<?> parametrized, JavaType... parameterTypes) {
        return CustomizeMapper.MAPPER.getTypeFactory().constructParametricType(parametrized, parameterTypes);
    }

    // endregion

    // region java 序列化为 json

    /**
     * 将对象序列化成 json 字符串
     *
     * @param obj javaBean
     * @return {@code String } json 字符串
     */
    @SneakyThrows(JsonProcessingException.class)
    public String toJson(Object obj) {
        if ($.isNull(obj)) {
            return "";
        }
        if (obj instanceof String str) {
            return str;
        }
        if (obj instanceof BigDecimal bd) {
            return bd.toPlainString();
        }
        return CustomizeMapper.MAPPER.writeValueAsString(obj);
    }

    /**
     * 将对象序列化成 json 字符串，格式美化
     *
     * @param obj javaBean
     * @return jsonString json 字符串
     */
    @SneakyThrows(JsonProcessingException.class)
    public String toPrettyJson(Object obj) {
        if ($.isNull(obj)) {
            return "";
        }
        if (obj instanceof String str) {
            return str;
        }
        if (obj instanceof BigDecimal bd) {
            return bd.toPlainString();
        }
        return CustomizeMapper.MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
    }

    /**
     * 将对象序列化成 json byte 数组
     *
     * @param obj javaBean
     * @return jsonString json 字符串
     */
    @SneakyThrows(JsonProcessingException.class)
    public byte[] toJsonAsBytes(Object obj) {
        if ($.isNull(obj)) {
            return "".getBytes();
        }
        if (obj instanceof String str) {
            return str.getBytes();
        }
        if (obj instanceof BigDecimal bd) {
            return bd.toPlainString().getBytes();
        }
        return CustomizeMapper.MAPPER.writeValueAsBytes(obj);
    }

    // endregion

    // region json 序列化为 javaBean

    /**
     * 将 json 反序列化成对象
     *
     * @param json      jsonString
     * @param valueType class
     * @param <T>       T 泛型标记
     * @return Bean
     */
    @SneakyThrows({JsonMappingException.class, JsonProcessingException.class})
    public <T> T readObj(String json, Class<T> valueType) {
        return CustomizeMapper.MAPPER.readValue(json, valueType);
    }

    /**
     * 将 json byte 数组反序列化成对象
     *
     * @param content   json bytes
     * @param valueType class
     * @param <T>       T 泛型标记
     * @return Bean
     */
    @SneakyThrows({IOException.class})
    public <T> T readObj(byte[] content, Class<T> valueType) {
        return CustomizeMapper.MAPPER.readValue(content, valueType);
    }

    /**
     * 将 json 反序列化成对象
     *
     * @param in        InputStream
     * @param valueType class
     * @param <T>       T 泛型标记
     * @return Bean
     */
    @SneakyThrows({IOException.class})
    public <T> T readObj(InputStream in, Class<T> valueType) {
        return CustomizeMapper.MAPPER.readValue(in, valueType);
    }

    /**
     * 将 java.io.Reader 反序列化成对象
     *
     * @param reader    java.io.Reader
     * @param valueType class
     * @param <T>       T 泛型标记
     * @return Bean
     */
    @SneakyThrows({IOException.class})
    public <T> T readObj(Reader reader, Class<T> valueType) {
        return CustomizeMapper.MAPPER.readValue(reader, valueType);
    }

    /**
     * 将 json 反序列化成对象
     *
     * @param json     jsonString
     * @param javaType JavaType
     * @param <T>      T 泛型标记
     * @return Bean
     */
    @SneakyThrows({JsonMappingException.class, JsonProcessingException.class})
    public <T> T readObj(String json, JavaType javaType) {
        return CustomizeMapper.MAPPER.readValue(json, javaType);
    }

    /**
     * 将 json 反序列化成对象
     *
     * @param content  bytes
     * @param javaType JavaType
     * @param <T>      T 泛型标记
     * @return Bean
     */
    @SneakyThrows(IOException.class)
    public <T> T readObj(byte[] content, JavaType javaType) {
        return CustomizeMapper.MAPPER.readValue(content, javaType);
    }

    /**
     * 将 json 反序列化成对象
     *
     * @param in       InputStream
     * @param javaType JavaType
     * @param <T>      T 泛型标记
     * @return Bean
     */
    @SneakyThrows(IOException.class)
    public <T> T readObj(InputStream in, JavaType javaType) {
        return CustomizeMapper.MAPPER.readValue(in, javaType);
    }

    /**
     * 将 java.io.Reader 反序列化成对象
     *
     * @param reader   java.io.Reader
     * @param javaType JavaType
     * @param <T>      T 泛型标记
     * @return Bean
     */
    @SneakyThrows(IOException.class)
    public <T> T readObj(Reader reader, JavaType javaType) {
        return CustomizeMapper.MAPPER.readValue(reader, javaType);
    }

    /**
     * 将 json 反序列化成对象
     *
     * @param json          jsonString
     * @param typeReference 泛型类型
     * @param <T>           T 泛型标记
     * @return Bean
     */
    @SneakyThrows({JsonMappingException.class, JsonProcessingException.class})
    public <T> T readObj(String json, TypeReference<T> typeReference) {
        return CustomizeMapper.MAPPER.readValue(json, typeReference);
    }

    /**
     * 将 json 反序列化成对象
     *
     * @param content       bytes
     * @param typeReference 泛型类型
     * @param <T>           T 泛型标记
     * @return Bean
     */
    @SneakyThrows({IOException.class})
    public <T> T readObj(byte[] content, TypeReference<T> typeReference) {
        return CustomizeMapper.MAPPER.readValue(content, typeReference);
    }

    /**
     * 将 json 反序列化成对象
     *
     * @param in            InputStream
     * @param typeReference 泛型类型
     * @param <T>           T 泛型标记
     * @return Bean
     */
    @SneakyThrows(IOException.class)
    public <T> T readObj(InputStream in, TypeReference<T> typeReference) {
        return CustomizeMapper.MAPPER.readValue(in, typeReference);
    }

    /**
     * 将 java.io.Reader 反序列化成对象
     *
     * @param reader        java.io.Reader
     * @param typeReference 泛型类型
     * @param <T>           T 泛型标记
     * @return Bean
     */
    @SneakyThrows(IOException.class)
    public <T> T readObj(Reader reader, TypeReference<T> typeReference) {
        return CustomizeMapper.MAPPER.readValue(reader, typeReference);
    }

    // endregion

    // region json 序列化为集合

    /**
     * 将 json 反序列化成集合
     *
     * @param json         jsonString
     * @param elementClass 集合中的元素类型
     * @param <T>          泛型
     * @return {@link List}<{@link T}>
     */
    public <T> List<T> readList(String json, Class<T> elementClass) {
        return readObj(json, getListType(elementClass));
    }

    /**
     * 读取集合
     *
     * @param content      bytes
     * @param elementClass elementClass
     * @param <T>          泛型
     * @return 集合
     */
    @SneakyThrows(IOException.class)
    public <T> List<T> readList(byte[] content, Class<T> elementClass) {
        return CustomizeMapper.MAPPER.readValue(content, getListType(elementClass));
    }

    /**
     * 读取集合
     *
     * @param content      InputStream
     * @param elementClass elementClass
     * @param <T>          泛型
     * @return 集合
     */
    @SneakyThrows(IOException.class)
    public <T> List<T> readList(InputStream content, Class<T> elementClass) {
        return CustomizeMapper.MAPPER.readValue(content, getListType(elementClass));
    }

    /**
     * 读取集合
     *
     * @param reader       java.io.Reader
     * @param elementClass elementClass
     * @param <T>          泛型
     * @return 集合
     */
    @SneakyThrows(IOException.class)
    public <T> List<T> readList(Reader reader, Class<T> elementClass) {
        return CustomizeMapper.MAPPER.readValue(reader, getListType(elementClass));
    }

    // endregion

    // region json 序列化为 Map 集合

    /**
     * 将 json 反序列化成 Map 集合
     *
     * @param json jsonString
     * @return {@link Map}<{@link String}, {@link Object}>
     */
    public Map<String, Object> readMap(String json) {
        return readMap(json, String.class, Object.class);
    }

    /**
     * 读取集合
     *
     * @param content bytes
     * @return 集合
     */
    public Map<String, Object> readMap(byte[] content) {
        return readMap(content, Object.class);
    }

    /**
     * 读取集合
     *
     * @param content InputStream
     * @return 集合
     */
    public Map<String, Object> readMap(InputStream content) {
        return readMap(content, Object.class);
    }

    /**
     * 读取集合
     *
     * @param reader java.io.Reader
     * @return 集合
     */
    public Map<String, Object> readMap(Reader reader) {
        return readMap(reader, Object.class);
    }

    /**
     * 将 json 反序列化成 Map 集合
     *
     * @param json       jsonString
     * @param valueClass 值类型
     * @param <V>        泛型
     * @return {@link Map}<{@link String}, {@link Object}>
     */
    public <V> Map<String, V> readMap(String json, Class<?> valueClass) {
        return readMap(json, String.class, valueClass);
    }

    /**
     * 读取集合
     *
     * @param content    bytes
     * @param valueClass 值类型
     * @param <V>        泛型
     * @return 集合
     */
    public <V> Map<String, V> readMap(byte[] content, Class<?> valueClass) {
        return readMap(content, String.class, valueClass);
    }

    /**
     * 读取集合
     *
     * @param content    InputStream
     * @param valueClass 值类型
     * @param <V>        泛型
     * @return 集合
     */
    public <V> Map<String, V> readMap(InputStream content, Class<?> valueClass) {
        return readMap(content, String.class, valueClass);
    }

    /**
     * 读取集合
     *
     * @param reader     java.io.Reader
     * @param valueClass 值类型
     * @param <V>        泛型
     * @return 集合
     */
    public <V> Map<String, V> readMap(Reader reader, Class<?> valueClass) {
        return readMap(reader, String.class, valueClass);
    }

    /**
     * 将 json 反序列化成 Map 集合
     *
     * @param json       jsonString
     * @param keyClass   key 类型
     * @param valueClass 值类型
     * @param <K>        泛型
     * @param <V>        泛型
     * @return {@link Map}<{@link String}, {@link Object}>
     */
    public <K, V> Map<K, V> readMap(String json, Class<?> keyClass, Class<?> valueClass) {
        return readObj(json, getMapType(keyClass, valueClass));
    }

    /**
     * 读取集合
     *
     * @param content    bytes
     * @param keyClass   key 类型
     * @param valueClass 值类型
     * @param <K>        泛型
     * @param <V>        泛型
     * @return 集合
     */
    @SneakyThrows(IOException.class)
    public <K, V> Map<K, V> readMap(byte[] content, Class<?> keyClass, Class<?> valueClass) {
        return CustomizeMapper.MAPPER.readValue(content, getMapType(keyClass, valueClass));
    }

    /**
     * 读取集合
     *
     * @param content    InputStream
     * @param keyClass   key 类型
     * @param valueClass 值类型
     * @param <K>        泛型
     * @param <V>        泛型
     * @return 集合
     */
    @SneakyThrows(IOException.class)
    public <K, V> Map<K, V> readMap(InputStream content, Class<?> keyClass, Class<?> valueClass) {
        return CustomizeMapper.MAPPER.readValue(content, getMapType(keyClass, valueClass));
    }

    /**
     * 读取集合
     *
     * @param reader     java.io.Reader
     * @param keyClass   key 类型
     * @param valueClass 值类型
     * @param <K>        泛型
     * @param <V>        泛型
     * @return 集合
     */
    @SneakyThrows(IOException.class)
    public <K, V> Map<K, V> readMap(Reader reader, Class<?> keyClass, Class<?> valueClass) {
        return CustomizeMapper.MAPPER.readValue(reader, getMapType(keyClass, valueClass));
    }

    // endregion

    // region json 实现对象类型转换

    /**
     * jackson 的类型转换
     *
     * @param fromValue   来源对象
     * @param toValueType 转换的类型
     * @param <T>         泛型标记
     * @return 转换结果
     */
    public <T> T convert(Object fromValue, Class<T> toValueType) {
        return CustomizeMapper.MAPPER.convertValue(fromValue, toValueType);
    }

    /**
     * jackson 的类型转换
     *
     * @param fromValue   来源对象
     * @param toValueType 转换的类型
     * @param <T>         泛型标记
     * @return 转换结果
     */
    public <T> T convert(Object fromValue, JavaType toValueType) {
        return CustomizeMapper.MAPPER.convertValue(fromValue, toValueType);
    }

    /**
     * jackson 的类型转换
     *
     * @param fromValue      来源对象
     * @param toValueTypeRef 泛型类型
     * @param <T>            泛型标记
     * @return 转换结果
     */
    public <T> T convert(Object fromValue, TypeReference<T> toValueTypeRef) {
        return CustomizeMapper.MAPPER.convertValue(fromValue, toValueTypeRef);
    }

    // endregion

    // region 校验

    /**
     * 检验 json 格式
     *
     * @param json json 字符串
     * @return 是否成功
     */
    public boolean isValidJson(String json) {
        ObjectMapper mapper = CustomizeMapper.MAPPER.copy();
        mapper.enable(DeserializationFeature.FAIL_ON_TRAILING_TOKENS);
        mapper.enable(DeserializationFeature.FAIL_ON_READING_DUP_TREE_KEY);
        try {
            mapper.readTree(json);
            return true;
        } catch (Throwable e) {
            return false;
        }
    }

    // endregion

}
