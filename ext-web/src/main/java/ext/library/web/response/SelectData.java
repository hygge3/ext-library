package ext.library.web.response;

import io.swagger.v3.oas.annotations.media.Schema;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

/**
 * 下拉框所对应的视图类
 */
@Schema(title = "下拉框数据")
public class SelectData<T> {

    /**
     * 显示的数据
     */
    @Schema(title = "显示的数据", requiredMode = REQUIRED)
    private String name;

    /**
     * 选中获取的属性
     */
    @Schema(title = "选中获取的属性", requiredMode = REQUIRED)
    private T value;

    /**
     * 是否被选中
     */
    @Schema(title = "是否被选中")
    private Boolean selected;

    /**
     * 是否禁用
     */
    @Schema(title = "是否禁用")
    private Boolean disabled;

    /**
     * 分组标识
     */
    @Schema(title = "分组标识")
    private String type;

    /**
     * 附加属性
     */
    @Schema(title = "附加属性")
    private T attributes;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }

    public Boolean getSelected() {
        return selected;
    }

    public void setSelected(Boolean selected) {
        this.selected = selected;
    }

    public Boolean getDisabled() {
        return disabled;
    }

    public void setDisabled(Boolean disabled) {
        this.disabled = disabled;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public T getAttributes() {
        return attributes;
    }

    public void setAttributes(T attributes) {
        this.attributes = attributes;
    }
}