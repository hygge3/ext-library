package ext.library.json.util;

import ext.library.tool.constant.EmojiSymbol;
import ext.library.tool.exception.ToolException;
import org.jspecify.annotations.Nullable;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

import java.util.List;
import java.util.Objects;

/**
 * JsonNode 工具类
 */
public final class JsonNodeUtil {

    private JsonNodeUtil() {
    }

    // region JsonNode 与对象互转

    /**
     * JsonNode 转对象
     *
     * @param jsonNode  JSON 节点
     * @param valueType 目标类型
     * @param <T>       泛型标记
     *
     * @return 转换结果
     */
    public static <T> T toObj(JsonNode jsonNode, Class<T> valueType) {
        try {
            return JsonUtil.getMapper().treeToValue(jsonNode, valueType);
        } catch (JacksonException e) {
            throw new ToolException(EmojiSymbol.JSON, e);
        }
    }

    /**
     * JsonNode 转集合
     *
     * @param jsonNode    JSON 节点（应为数组节点）
     * @param elementType 集合元素类型
     * @param <T>         泛型标记
     *
     * @return 转换结果
     */
    public static <T> List<T> toList(JsonNode jsonNode, Class<T> elementType) {
        try {
            return JsonUtil.getMapper().readerForListOf(elementType).readValue(jsonNode);
        } catch (JacksonException e) {
            throw new ToolException(EmojiSymbol.JSON, e);
        }
    }

    /**
     * 对象转 JsonNode
     *
     * @param fromValue 源对象
     * @param <T>       JsonNode 子类型
     *
     * @return JsonNode 节点
     */
    public static <T extends JsonNode> T toNode(Object fromValue) {
        return JsonUtil.getMapper().valueToTree(fromValue);
    }

    // endregion

    // region JsonNode 操作

    /**
     * 从 JsonNode 中提取特定字段并转换为指定类型
     *
     * @param node      JSON 节点
     * @param fieldName 字段名
     * @param clazz     目标类型
     * @param <T>       泛型标记
     *
     * @return 字段值，字段不存在时返回 null
     */
    public static <T> @Nullable T getFieldValue(JsonNode node, String fieldName, Class<T> clazz) {
        JsonNode valueNode = node.get(fieldName);
        if (valueNode == null || valueNode.isNull()) {
            return null;
        }
        return toObj(valueNode, clazz);
    }

    /**
     * 解析 JSON 字符串为 JsonNode
     *
     * @param json JSON 字符串
     *
     * @return JsonNode 节点
     */
    public static JsonNode parseNode(String json) {
        try {
            return JsonUtil.getMapper().readTree(Objects.requireNonNull(json, "json is null"));
        } catch (JacksonException e) {
            throw new ToolException(EmojiSymbol.JSON, e);
        }
    }

    /**
     * 创建空的 ObjectNode
     *
     * @return ObjectNode 实例
     */
    public static ObjectNode createObjectNode() {
        return JsonUtil.getMapper().createObjectNode();
    }

    /**
     * 创建空的 ArrayNode
     *
     * @return ArrayNode 实例
     */
    public static ArrayNode createArrayNode() {
        return JsonUtil.getMapper().createArrayNode();
    }

    // endregion

}
