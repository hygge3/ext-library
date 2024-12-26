package ext.library.eatpick.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import java.io.Serializable;

import java.io.Serial;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 菜谱食材关联 实体类。
 *
 * @author Auto Codegen By Ext
 * @since 2024-12-18
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("recipe_ingredient_bind")
public class RecipeIngredientBind implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 菜谱 ID
     */
    @Id(keyType = KeyType.Auto)
    private Long recipeId;

    /**
     * 食材 ID
     */
    @Id
    private Long ingredientId;

    /**
     * 数量
     */
    private Double quantity;

    /**
     * 单位，如克、毫升、个等
     */
    private String unit;

}
