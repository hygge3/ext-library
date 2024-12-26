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
 * 菜谱分类关联 实体类。
 *
 * @author Auto Codegen By Ext
 * @since 2024-12-18
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("recipe_category_bind")
public class RecipeCategoryBind implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 菜谱 ID
     */
    @Id(keyType = KeyType.Auto)
    private Long recipeId;

    /**
     * 分类 ID
     */
    @Id
    private Long categoryId;

}
