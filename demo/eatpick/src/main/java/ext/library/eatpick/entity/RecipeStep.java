package ext.library.eatpick.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.Table;
import java.io.Serializable;

import java.io.Serial;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 菜谱步骤 实体类。
 *
 * @author Auto Codegen By Ext
 * @since 2024-12-18
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("recipe_step")
public class RecipeStep implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 菜谱 ID
     */
    @Id
    private Long recipeId;

    /**
     * 步骤序号
     */
    @Id
    private Integer order;

    /**
     * 描述
     */
    private String desc;

}
