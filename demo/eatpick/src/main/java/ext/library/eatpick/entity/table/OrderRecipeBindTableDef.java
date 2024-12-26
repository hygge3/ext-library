package ext.library.eatpick.entity.table;

import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.table.TableDef;

import java.io.Serial;

/**
 * 订单菜谱关联 表定义层。
 *
 * @author Auto Codegen By Ext
 * @since 2024-12-18
 */
public class OrderRecipeBindTableDef extends TableDef {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 订单菜谱关联
     */
    public static final OrderRecipeBindTableDef ORDER_RECIPE_BIND = new OrderRecipeBindTableDef();

    /**
     * 订单 ID
     */
    public final QueryColumn ORDER_ID = new QueryColumn(this, "order_id");

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
    public final QueryColumn[] DEFAULT_COLUMNS = new QueryColumn[]{ORDER_ID, RECIPE_ID};

    public OrderRecipeBindTableDef() {
        super("", "order_recipe_bind");
    }

    private OrderRecipeBindTableDef(String schema, String name, String alisa) {
        super(schema, name, alisa);
    }

    public OrderRecipeBindTableDef as(String alias) {
        String key = getNameWithSchema() + "." + alias;
        return getCache(key, k -> new OrderRecipeBindTableDef("", "order_recipe_bind", alias));
    }

}
