package ext.library.eatpick.param;

import jakarta.validation.constraints.NotBlank;

import ext.library.eatpick.entity.Category;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AutoMapper(target = Category.class)
public class CategoryParam {

    /**
     * 图片
     */
    private String banner;

    /**
     * 名称
     */
    @NotBlank
    private String name;

    /**
     * 描述
     */
    private String desc;
}
