package ext.library.eatpick.param;

import jakarta.validation.constraints.NotBlank;

import ext.library.eatpick.entity.Ingredient;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AutoMapper(target = Ingredient.class)
public class IngredientParam {

    /**
     * 图片
     */
    @NotBlank
    private String cover;

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
