package ext.library.tool.domain.dict;

/**
 * 字典接口
 * <p>
 * 定义枚举字典的标准结构，实现此接口的枚举可通过 {@link DictUtil} 转换为前端字典列表。
 *
 * @since 2025.01.01
 */
public interface Dict {

    /**
     * 获取字典键值
     *
     * @return 字典键（通常为数字编码）
     */
    int getKey();

    /**
     * 获取字典标签
     *
     * @return 字典标签（用于显示的文本）
     */
    String getLabel();
}
