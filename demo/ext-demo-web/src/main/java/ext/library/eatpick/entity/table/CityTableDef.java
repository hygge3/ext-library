package ext.library.eatpick.entity.table;

import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.table.TableDef;

import java.io.Serial;

/**
 * 地区 - 市 表定义层。
 *
 * @author Auto Codegen By Ext
 * @since 2024-11-29
 */
public class CityTableDef extends TableDef {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 地区 - 市
     */
    public static final CityTableDef CITY = new CityTableDef();

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
    public final QueryColumn[] DEFAULT_COLUMNS = new QueryColumn[]{CODE, NAME, PROVINCE_CODE, DISPLAY};

    public CityTableDef() {
        super("", "region_city");
    }

    private CityTableDef(String schema, String name, String alisa) {
        super(schema, name, alisa);
    }

    public CityTableDef as(String alias) {
        String key = getNameWithSchema() + "." + alias;
        return getCache(key, k -> new CityTableDef("", "region_city", alias));
    }

}
