package ext.library.eatpick.param;

import java.util.List;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import ext.library.eatpick.entity.Recipe;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AutoMapper(target = Recipe.class)
public class RecipeParam {

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
    @NotBlank
    private String desc;

    /**
     * 口味
     */
    @NotBlank
    private String taste;

    /**
     * 辣度
     */
    private Integer pungencyDegree;

    /**
     * 烹饪需要时间
     */
    @Min(0)
    private Integer cookingTime;

    /**
     * 难度
     */
    @Min(0)
    @Max(5)
    private Integer difficulty;

    /** 食材列表 */
    @NotNull
    @NotEmpty
    private List<RecipeIngredientParam> ingredients;

    /** 步骤列表 */
    @NotNull
    @NotEmpty
    private List<RecipeStepParam> steps;

}
