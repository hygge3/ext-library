package ext.library.eatpick.entity.table;

import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.table.TableDef;

import java.io.Serial;

/**
 * 菜谱步骤 表定义层。
 *
 * @author Auto Codegen By Ext
 * @since 2024-12-18
 */
public class RecipeStepTableDef extends TableDef {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 菜谱步骤
     */
    public static final RecipeStepTableDef RECIPE_STEP = new RecipeStepTableDef();

    /**
     * 描述
     */
    public final QueryColumn DESC = new QueryColumn(this, "desc");

    /**
     * 步骤序号
     */
    public final QueryColumn ORDER = new QueryColumn(this, "order");

    /**
     * 菜谱 ID
     */
    public final QueryColumn RECIPE_ID = new QueryColumn(this, "recipe_id");

    /**
     * 所有字段。
     */
    public final QueryColumn ALL_COLUMNS = new QueryColumn(this, "*");

    /**
     * 默认字段，不包含逻辑删除或者 large 等字段。
     */
    public final QueryColumn[] DEFAULT_COLUMNS = new QueryColumn[]{RECIPE_ID, ORDER, DESC};

    public RecipeStepTableDef() {
        super("", "recipe_step");
    }

    private RecipeStepTableDef(String schema, String name, String alisa) {
        super(schema, name, alisa);
    }

    public RecipeStepTableDef as(String alias) {
        String key = getNameWithSchema() + "." + alias;
        return getCache(key, k -> new RecipeStepTableDef("", "recipe_step", alias));
    }

}
