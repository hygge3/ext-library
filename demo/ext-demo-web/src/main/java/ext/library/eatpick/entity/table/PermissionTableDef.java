package ext.library.eatpick.entity.table;

import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.table.TableDef;

import java.io.Serial;

/**
 * 权限 表定义层。
 *
 * @author Auto Codegen By Ext
 * @since 2024-11-29
 */
public class PermissionTableDef extends TableDef {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 权限
     */
    public static final PermissionTableDef PERMISSION = new PermissionTableDef();

    /**
     * 主键
     */
    public final QueryColumn ID = new QueryColumn(this, "id");

    /**
     * 序列号
     */
    public final QueryColumn SEQ = new QueryColumn(this, "seq");

    /**
     * 编码
     */
    public final QueryColumn CODE = new QueryColumn(this, "code");

    /**
     * 名称
     */
    public final QueryColumn NAME = new QueryColumn(this, "name");

    /**
     * 路由地址
     */
    public final QueryColumn PATH = new QueryColumn(this, "path");

    /**
     * 上级 ID
     */
    public final QueryColumn PARENT_ID = new QueryColumn(this, "parent_id");

    /**
     * 所有字段。
     */
    public final QueryColumn ALL_COLUMNS = new QueryColumn(this, "*");

    /**
     * 默认字段，不包含逻辑删除或者 large 等字段。
     */
    public final QueryColumn[] DEFAULT_COLUMNS = new QueryColumn[]{ID, PARENT_ID, CODE, NAME, PATH, SEQ};

    public PermissionTableDef() {
        super("", "sys_permission");
    }

    private PermissionTableDef(String schema, String name, String alisa) {
        super(schema, name, alisa);
    }

    public PermissionTableDef as(String alias) {
        String key = getNameWithSchema() + "." + alias;
        return getCache(key, k -> new PermissionTableDef("", "sys_permission", alias));
    }

}
