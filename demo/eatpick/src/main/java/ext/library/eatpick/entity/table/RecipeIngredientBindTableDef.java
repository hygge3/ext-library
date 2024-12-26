package ext.library.eatpick.entity.table;

import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.table.TableDef;

import java.io.Serial;

/**
 * 菜谱食材关联 表定义层。
 *
 * @author Auto Codegen By Ext
 * @since 2024-12-18
 */
public class RecipeIngredientBindTableDef extends TableDef {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 菜谱食材关联
     */
    public static final RecipeIngredientBindTableDef RECIPE_INGREDIENT_BIND = new RecipeIngredientBindTableDef();

    /**
     * 单位，如克、毫升、个等
     */
    public final QueryColumn UNIT = new QueryColumn(this, "unit");

    /**
     * 数量
     */
    public final QueryColumn QUANTITY = new QueryColumn(this, "quantity");

    /**
     * 菜谱 ID
     */
    public final QueryColumn RECIPE_ID = new QueryColumn(this, "recipe_id");

    /**
     * 食材 ID
     */
    public final QueryColumn INGREDIENT_ID = new QueryColumn(this, "ingredient_id");

    /**
     * 所有字段。
     */
    public final QueryColumn ALL_COLUMNS = new QueryColumn(this, "*");

    /**
     * 默认字段，不包含逻辑删除或者 large 等字段。
     */
    public final QueryColumn[] DEFAULT_COLUMNS = new QueryColumn[]{RECIPE_ID, INGREDIENT_ID, QUANTITY, UNIT};

    public RecipeIngredientBindTableDef() {
        super("", "recipe_ingredient_bind");
    }

    private RecipeIngredientBindTableDef(String schema, String name, String alisa) {
        super(schema, name, alisa);
    }

    public RecipeIngredientBindTableDef as(String alias) {
        String key = getNameWithSchema() + "." + alias;
        return getCache(key, k -> new RecipeIngredientBindTableDef("", "recipe_ingredient_bind", alias));
    }

}
