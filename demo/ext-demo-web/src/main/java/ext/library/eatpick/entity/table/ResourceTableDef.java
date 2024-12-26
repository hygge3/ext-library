package ext.library.eatpick.entity.table;

import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.table.TableDef;

import java.io.Serial;

/**
 * 资源 表定义层。
 *
 * @author Auto Codegen By Ext
 * @since 2024-11-29
 */
public class ResourceTableDef extends TableDef {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 资源
     */
    public static final ResourceTableDef RESOURCE = new ResourceTableDef();

    /**
     * 主键
     */
    public final QueryColumn ID = new QueryColumn(this, "id");

    /**
     * 名称
     */
    public final QueryColumn TITLE = new QueryColumn(this, "title");

    /**
     * 备注
     */
    public final QueryColumn REMARK = new QueryColumn(this, "remark");

    /**
     * 记录创建用户 ID
     */
    public final QueryColumn CREATE_BY = new QueryColumn(this, "create_by");

    /**
     * 记录上次修改用户 ID
     */
    public final QueryColumn UPDATE_BY = new QueryColumn(this, "update_by");

    /**
     * 属性
     */
    public final QueryColumn ATTRIBUTE = new QueryColumn(this, "attribute");

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
    public final QueryColumn[] DEFAULT_COLUMNS = new QueryColumn[]{ID, TITLE, ATTRIBUTE, REMARK, CREATE_BY, CREATE_TIME, UPDATE_BY, UPDATE_TIME, };

    public ResourceTableDef() {
        super("", "data_resource");
    }

    private ResourceTableDef(String schema, String name, String alisa) {
        super(schema, name, alisa);
    }

    public ResourceTableDef as(String alias) {
        String key = getNameWithSchema() + "." + alias;
        return getCache(key, k -> new ResourceTableDef("", "data_resource", alias));
    }

}
