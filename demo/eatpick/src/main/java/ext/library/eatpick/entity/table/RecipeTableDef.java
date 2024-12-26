package ext.library.eatpick.entity.table;

import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.table.TableDef;

import java.io.Serial;

/**
 * 菜谱 表定义层。
 *
 * @author Auto Codegen By Ext
 * @since 2024-12-18
 */
public class RecipeTableDef extends TableDef {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 菜谱
     */
    public static final RecipeTableDef RECIPE = new RecipeTableDef();

    /**
     * 主键
     */
    public final QueryColumn ID = new QueryColumn(this, "id");

    /**
     * 描述
     */
    public final QueryColumn DESC = new QueryColumn(this, "desc");

    /**
     * 名称
     */
    public final QueryColumn NAME = new QueryColumn(this, "name");

    /**
     * 图片
     */
    public final QueryColumn COVER = new QueryColumn(this, "cover");

    /**
     * 口味
     */
    public final QueryColumn TASTE = new QueryColumn(this, "taste");

    /**
     * 是否展示
     */
    public final QueryColumn DISPLAY = new QueryColumn(this, "display");

    /**
     * 记录创建时间
     */
    public final QueryColumn CREATE_TIME = new QueryColumn(this, "create_time");

    /**
     * 记录删除时间
     */
    public final QueryColumn DELETE_TIME = new QueryColumn(this, "delete_time");

    /**
     * 难度
     */
    public final QueryColumn DIFFICULTY = new QueryColumn(this, "difficulty");

    /**
     * 记录上次修改时间
     */
    public final QueryColumn UPDATE_TIME = new QueryColumn(this, "update_time");

    /**
     * 烹饪需要时间，单位：min
     */
    public final QueryColumn COOKING_TIME = new QueryColumn(this, "cooking_time");

    /**
     * 辣度
     */
    public final QueryColumn PUNGENCY_DEGREE = new QueryColumn(this, "pungency_degree");

    /**
     * 所有字段。
     */
    public final QueryColumn ALL_COLUMNS = new QueryColumn(this, "*");

    /**
     * 默认字段，不包含逻辑删除或者 large 等字段。
     */
    public final QueryColumn[] DEFAULT_COLUMNS = new QueryColumn[]{ID, COVER, NAME, DESC, TASTE, PUNGENCY_DEGREE, COOKING_TIME, DIFFICULTY, DISPLAY, CREATE_TIME, UPDATE_TIME, };

    public RecipeTableDef() {
        super("", "recipe");
    }

    private RecipeTableDef(String schema, String name, String alisa) {
        super(schema, name, alisa);
    }

    public RecipeTableDef as(String alias) {
        String key = getNameWithSchema() + "." + alias;
        return getCache(key, k -> new RecipeTableDef("", "recipe", alias));
    }

}
