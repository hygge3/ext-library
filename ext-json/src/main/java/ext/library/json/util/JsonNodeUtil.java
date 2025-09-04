package ext.library.json.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.List;
import java.util.Objects;

/**
 * JsonNode 工具类
 */
@UtilityClass
public class JsonNodeUtil {

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
    @SneakyThrows(JsonProcessingException.class)
    public <T> T treeToObj(JsonNode jsonNode, Class<T> valueType) {
        return CustomizeMapper.MAPPER.treeToValue(jsonNode, valueType);
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
    @SneakyThrows(JsonProcessingException.class)
    public <T> T treeToObj(JsonNode jsonNode, JavaType valueType) {
        return CustomizeMapper.MAPPER.treeToValue(jsonNode, valueType);
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
    @SneakyThrows(IOException.class)
    public <T> List<T> treeToList(JsonNode jsonNode, Class<T> elementType) {
        return CustomizeMapper.MAPPER.readerForListOf(elementType).readValue(jsonNode);
    }

    /**
     * 对象转 JsonNode
     *
     * @param fromValue fromValue
     * @param <T>       泛型标记
     *
     * @return 转换结果
     */
    public <T extends JsonNode> T objToTree(Object fromValue) {
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
    @SneakyThrows({JsonProcessingException.class})
    public JsonNode readTree(String json) {
        return CustomizeMapper.MAPPER.readTree(Objects.requireNonNull(json, "jsonString is null"));
    }

    /**
     * 将 InputStream 转成 JsonNode
     *
     * @param in InputStream
     *
     * @return jsonString json 字符串
     */
    @SneakyThrows({IOException.class})
    public JsonNode readTree(InputStream in) {
        return CustomizeMapper.MAPPER.readTree(Objects.requireNonNull(in, "InputStream in is null"));
    }

    /**
     * 将 java.io.Reader 转成 JsonNode
     *
     * @param reader java.io.Reader
     *
     * @return jsonString json 字符串
     */
    @SneakyThrows({IOException.class})
    public JsonNode readTree(Reader reader) {
        return CustomizeMapper.MAPPER.readTree(Objects.requireNonNull(reader, "Reader in is null"));
    }

    /**
     * 将 json 字符串转成 JsonNode
     *
     * @param content content
     *
     * @return jsonString json 字符串
     */
    @SneakyThrows({IOException.class})
    public JsonNode readTree(byte[] content) {
        return CustomizeMapper.MAPPER.readTree(Objects.requireNonNull(content, "byte[] content is null"));
    }

    /**
     * 创建 ObjectNode
     *
     * @return {@code ObjectNode }
     */
    public ObjectNode createObjectNode() {
        return CustomizeMapper.MAPPER.createObjectNode();
    }

    /**
     * 创建 ArrayNode
     *
     * @return {@code ArrayNode }
     */
    public ArrayNode createArrayNode() {
        return CustomizeMapper.MAPPER.createArrayNode();
    }

    // endregion

}