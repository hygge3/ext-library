package ext.library.eatpick.entity.table;

import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.table.TableDef;

import java.io.Serial;

/**
 * 食材 表定义层。
 *
 * @author Auto Codegen By Ext
 * @since 2024-12-18
 */
public class IngredientTableDef extends TableDef {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 食材
     */
    public static final IngredientTableDef INGREDIENT = new IngredientTableDef();

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
     * 记录创建时间
     */
    public final QueryColumn CREATE_TIME = new QueryColumn(this, "create_time");

    /**
     * 记录删除时间
     */
    public final QueryColumn DELETE_TIME = new QueryColumn(this, "delete_time");

    /**
     * 记录上次修改时间
     */
    public final QueryColumn UPDATE_TIME = new QueryColumn(this, "update_time");

    /**
     * 所有字段。
     */
    public final QueryColumn ALL_COLUMNS = new QueryColumn(this, "*");

    /**
     * 默认字段，不包含逻辑删除或者 large 等字段。
     */
    public final QueryColumn[] DEFAULT_COLUMNS = new QueryColumn[]{ID, COVER, NAME, DESC, CREATE_TIME, UPDATE_TIME, };

    public IngredientTableDef() {
        super("", "ingredient");
    }

    private IngredientTableDef(String schema, String name, String alisa) {
        super(schema, name, alisa);
    }

    public IngredientTableDef as(String alias) {
        String key = getNameWithSchema() + "." + alias;
        return getCache(key, k -> new IngredientTableDef("", "ingredient", alias));
    }

}
