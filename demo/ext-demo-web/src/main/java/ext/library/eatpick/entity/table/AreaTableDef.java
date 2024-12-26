package ext.library.eatpick.entity.table;

import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.table.TableDef;

import java.io.Serial;

/**
 * 地区 - 区/县 表定义层。
 *
 * @author Auto Codegen By Ext
 * @since 2024-11-29
 */
public class AreaTableDef extends TableDef {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 地区 - 区/县
     */
    public static final AreaTableDef AREA = new AreaTableDef();

    /**
     * 编码
     */
    public final QueryColumn CODE = new QueryColumn(this, "code");

    /**
     * 名称
     */
    public final QueryColumn NAME = new QueryColumn(this, "name");

    /**
     * 是否显示
     */
    public final QueryColumn DISPLAY = new QueryColumn(this, "display");

    /**
     * 市级编码
     */
    public final QueryColumn CITY_CODE = new QueryColumn(this, "city_code");

    /**
     * 省级编码
     */
    public final QueryColumn PROVINCE_CODE = new QueryColumn(this, "province_code");

    /**
     * 所有字段。
     */
    public final QueryColumn ALL_COLUMNS = new QueryColumn(this, "*");

    /**
     * 默认字段，不包含逻辑删除或者 large 等字段。
     */
    public final QueryColumn[] DEFAULT_COLUMNS = new QueryColumn[]{CODE, NAME, CITY_CODE, PROVINCE_CODE, DISPLAY};

    public AreaTableDef() {
        super("", "region_area");
    }

    private AreaTableDef(String schema, String name, String alisa) {
        super(schema, name, alisa);
    }

    public AreaTableDef as(String alias) {
        String key = getNameWithSchema() + "." + alias;
        return getCache(key, k -> new AreaTableDef("", "region_area", alias));
    }

}
