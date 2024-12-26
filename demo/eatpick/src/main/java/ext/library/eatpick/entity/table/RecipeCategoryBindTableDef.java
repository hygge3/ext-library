package ext.library.eatpick.entity.table;

import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.table.TableDef;

import java.io.Serial;

/**
 * 菜谱分类关联 表定义层。
 *
 * @author Auto Codegen By Ext
 * @since 2024-12-18
 */
public class RecipeCategoryBindTableDef extends TableDef {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 菜谱分类关联
     */
    public static final RecipeCategoryBindTableDef RECIPE_CATEGORY_BIND = new RecipeCategoryBindTableDef();

    /**
     * 菜谱 ID
     */
    public final QueryColumn RECIPE_ID = new QueryColumn(this, "recipe_id");

    /**
     * 分类 ID
     */
    public final QueryColumn CATEGORY_ID = new QueryColumn(this, "category_id");

    /**
     * 所有字段。
     */
    public final QueryColumn ALL_COLUMNS = new QueryColumn(this, "*");

    /**
     * 默认字段，不包含逻辑删除或者 large 等字段。
     */
    public final QueryColumn[] DEFAULT_COLUMNS = new QueryColumn[]{RECIPE_ID, CATEGORY_ID};

    public RecipeCategoryBindTableDef() {
        super("", "recipe_category_bind");
    }

    private RecipeCategoryBindTableDef(String schema, String name, String alisa) {
        super(schema, name, alisa);
    }

    public RecipeCategoryBindTableDef as(String alias) {
        String key = getNameWithSchema() + "." + alias;
        return getCache(key, k -> new RecipeCategoryBindTableDef("", "recipe_category_bind", alias));
    }

}
