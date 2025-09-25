package ext.library.json.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import ext.library.tool.core.Exceptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.List;
import java.util.Objects;

/**
 * JsonNode 工具类
 */
public class JsonNodeUtil {
    private static final Logger log = LoggerFactory.getLogger(JsonNodeUtil.class);


    // region JsonNode 与对象互转

    /**
     * jsonNode 转对象
     *
     * @param jsonNode  JSON 节点
     * @param valueType valueType
     * @param <T>       泛型标记
     *
     * @return 转换结果
     */
    public static <T> T treeToObj(JsonNode jsonNode, Class<T> valueType) {
        try {
            return CustomizeMapper.MAPPER.treeToValue(jsonNode, valueType);
        } catch (JsonProcessingException e) {
            throw Exceptions.unchecked(e);
        }
    }

    /**
     * tree 转对象
     *
     * @param jsonNode  JSON 节点
     * @param valueType valueType
     * @param <T>       泛型标记
     *
     * @return 转换结果
     */
    public static <T> T treeToObj(JsonNode jsonNode, JavaType valueType) {
        try {
            return CustomizeMapper.MAPPER.treeToValue(jsonNode, valueType);
        } catch (JsonProcessingException e) {
            throw Exceptions.unchecked(e);
        }
    }

    /**
     * tree 转带泛型的集合
     *
     * @param jsonNode    JSON 节点
     * @param elementType elementType
     * @param <T>         泛型标记
     *
     * @return 转换结果
     */
    public static <T> List<T> treeToList(JsonNode jsonNode, Class<T> elementType) {
        try {
            return CustomizeMapper.MAPPER.readerForListOf(elementType).readValue(jsonNode);
        } catch (IOException e) {
            throw Exceptions.unchecked(e);
        }
    }

    /**
     * 对象转 JsonNode
     *
     * @param fromValue fromValue
     * @param <T>       泛型标记
     *
     * @return 转换结果
     */
    public static <T extends JsonNode> T objToTree(Object fromValue) {
        return CustomizeMapper.MAPPER.valueToTree(fromValue);
    }

    // endregion

    // region JsonNode 操作

    /**
     * 将 json 字符串转成 JsonNode
     *
     * @param json jsonString
     *
     * @return jsonString json 字符串
     */
    public static JsonNode readTree(String json) {
        try {
            return CustomizeMapper.MAPPER.readTree(Objects.requireNonNull(json, "jsonString is null"));
        } catch (JsonProcessingException e) {
            throw Exceptions.unchecked(e);
        }
    }

    /**
     * 将 InputStream 转成 JsonNode
     *
     * @param in InputStream
     *
     * @return jsonString json 字符串
     */
    public static JsonNode readTree(InputStream in) {
        try {
            return CustomizeMapper.MAPPER.readTree(Objects.requireNonNull(in, "InputStream in is null"));
        } catch (IOException e) {
            throw Exceptions.unchecked(e);
        }
    }

    /**
     * 将 java.io.Reader 转成 JsonNode
     *
     * @param reader java.io.Reader
     *
     * @return jsonString json 字符串
     */
    public static JsonNode readTree(Reader reader) {
        try {
            return CustomizeMapper.MAPPER.readTree(Objects.requireNonNull(reader, "Reader in is null"));
        } catch (IOException e) {
            throw Exceptions.unchecked(e);
        }
    }

    /**
     * 将 json 字符串转成 JsonNode
     *
     * @param content content
     *
     * @return jsonString json 字符串
     */
    public static JsonNode readTree(byte[] content) {
        try {
            return CustomizeMapper.MAPPER.readTree(Objects.requireNonNull(content, "byte[] content is null"));
        } catch (IOException e) {
            throw Exceptions.unchecked(e);
        }
    }

    /**
     * 创建 ObjectNode
     *
     * @return {@code ObjectNode }
     */
    public static ObjectNode createObjectNode() {
        return CustomizeMapper.MAPPER.createObjectNode();
    }

    /**
     * 创建 ArrayNode
     *
     * @return {@code ArrayNode }
     */
    public static ArrayNode createArrayNode() {
        return CustomizeMapper.MAPPER.createArrayNode();
    }

    // endregion

}