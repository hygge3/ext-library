package ext.library.eatpick.vo;

import java.util.List;

import ext.library.eatpick.entity.Ingredient;
import ext.library.eatpick.entity.Recipe;
import ext.library.eatpick.entity.RecipeStep;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AutoMapper(target = Recipe.class)
public class RecipeVO {

    /**
     * 主键
     */
    private Long id;

    /**
     * 图片
     */
    private String cover;

    /**
     * 名称
     */
    private String name;

    /**
     * 描述
     */
    private String desc;

    /**
     * 口味
     */
    private String taste;

    /**
     * 辣度
     */
    private Integer pungencyDegree;

    /**
     * 烹饪需要时间，单位：min
     */
    private Long cookingTime;

    /**
     * 难度
     */
    private Integer difficulty;

    /**
     * 是否展示
     */
    private Boolean display;

    /** 食材 */
    private List<Ingredient> ingredients;

    /** 步骤 */
    private List<RecipeStep> steps;
}
