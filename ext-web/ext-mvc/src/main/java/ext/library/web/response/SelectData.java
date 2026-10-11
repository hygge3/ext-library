package ext.library.web.response;

/**
 * 下拉框所对应数据
 */
public class SelectData<T> {

    /**
     * 显示的数据
     */
    private String name;

    /**
     * 选中获取的属性
     */
    private T value;

    /**
     * 是否被选中
     */
    private Boolean selected;

    /**
     * 是否禁用
     */
    private Boolean disabled;

    /**
     * 分组标识
     */
    private String type;

    /**
     * 附加属性
     */
    private T attributes;

    /**
     * 获取显示的数据
     *
     * @return 显示的数据
     */
    public String getName() {
        return name;
    }

    /**
     * 设置显示的数据
     *
     * @param name 显示的数据
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * 获取选中获取的属性
     *
     * @return 选中获取的属性
     */
    public T getValue() {
        return value;
    }

    /**
     * 设置选中获取的属性
     *
     * @param value 选中获取的属性
     */
    public void setValue(T value) {
        this.value = value;
    }

    /**
     * 获取是否被选中
     *
     * @return 是否被选中
     */
    public Boolean getSelected() {
        return selected;
    }

    /**
     * 设置是否被选中
     *
     * @param selected 是否被选中
     */
    public void setSelected(Boolean selected) {
        this.selected = selected;
    }

    /**
     * 获取是否禁用
     *
     * @return 是否禁用
     */
    public Boolean getDisabled() {
        return disabled;
    }

    /**
     * 设置是否禁用
     *
     * @param disabled 是否禁用
     */
    public void setDisabled(Boolean disabled) {
        this.disabled = disabled;
    }

    /**
     * 获取分组标识
     *
     * @return 分组标识
     */
    public String getType() {
        return type;
    }

    /**
     * 设置分组标识
     *
     * @param type 分组标识
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * 获取附加属性
     *
     * @return 附加属性
     */
    public T getAttributes() {
        return attributes;
    }

    /**
     * 设置附加属性
     *
     * @param attributes 附加属性
     */
    public void setAttributes(T attributes) {
        this.attributes = attributes;
    }
}
