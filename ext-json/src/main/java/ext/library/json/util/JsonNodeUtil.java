package ext.library.json.util;

import ext.library.tool.exception.ToolException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

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
    public static <T> T toObj(JsonNode jsonNode, Class<T> valueType) {
        try {
            return JsonUtil.mapper.treeToValue(jsonNode, valueType);
        } catch (JacksonException e) {
            throw new ToolException(e);
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
    public static <T> List<T> toList(JsonNode jsonNode, Class<T> elementType) {
        try {
            return JsonUtil.mapper.readerForListOf(elementType).readValue(jsonNode);
        } catch (JacksonException e) {
            throw new ToolException(e);
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
    public static <T extends JsonNode> T toNode(Object fromValue) {
        return JsonUtil.mapper.valueToTree(fromValue);
    }

    // endregion

    // region JsonNode 操作

    /**
     * 从 JsonNode 中提取特定字段
     */
    public static <T> T getNodeValue(JsonNode node, String fieldName, Class<T> clazz) {
        JsonNode valueNode = node.get(fieldName);
        try {
            return JsonUtil.mapper.treeToValue(valueNode, clazz);
        } catch (JacksonException e) {
            throw new ToolException(e);
        }
    }

    /**
     * 修改 JsonNode 并转换为 JSON 字符串
     */
    public static String modifyNode(String json, String fieldName, Object newValue) {
        try {
            ObjectNode node = (ObjectNode) JsonUtil.mapper.readTree(json);
            switch (newValue) {
                case String s -> node.put(fieldName, s);
                case Integer i -> node.put(fieldName, i);
                case Boolean b -> node.put(fieldName, b);
                case Double v -> node.put(fieldName, v);
                default -> node.putPOJO(fieldName, newValue);
            }
            return node.toString();
        } catch (JacksonException e) {
            throw new ToolException(e);
        }
    }

    /**
     * 将 json 字符串转成 JsonNode
     *
     * @param json jsonString
     *
     * @return jsonString json 字符串
     */
    public static JsonNode toNode(String json) {
        try {
            return JsonUtil.mapper.readTree(Objects.requireNonNull(json, "jsonString is null"));
        } catch (JacksonException e) {
            throw new ToolException(e);
        }
    }

    /**
     * 创建 ObjectNode
     *
     * @return {@code ObjectNode }
     */
    public static ObjectNode createObjectNode() {
        return JsonUtil.mapper.createObjectNode();
    }

    /**
     * 创建 ArrayNode
     *
     * @return {@code ArrayNode }
     */
    public static ArrayNode createArrayNode() {
        return JsonUtil.mapper.createArrayNode();
    }

    // endregion

}