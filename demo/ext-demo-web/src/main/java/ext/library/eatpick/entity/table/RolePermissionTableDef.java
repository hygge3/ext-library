package ext.library.eatpick.entity.table;

import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.table.TableDef;

import java.io.Serial;

/**
 * 角色权限关联表 表定义层。
 *
 * @author Auto Codegen By Ext
 * @since 2024-11-29
 */
public class RolePermissionTableDef extends TableDef {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 角色权限关联表
     */
    public static final RolePermissionTableDef ROLE_PERMISSION = new RolePermissionTableDef();

    /**
     * 角色 ID
     */
    public final QueryColumn ROLE_ID = new QueryColumn(this, "role_id");

    /**
     * 权限 ID
     */
    public final QueryColumn PERMISSION_ID = new QueryColumn(this, "permission_id");

    /**
     * 所有字段。
     */
    public final QueryColumn ALL_COLUMNS = new QueryColumn(this, "*");

    /**
     * 默认字段，不包含逻辑删除或者 large 等字段。
     */
    public final QueryColumn[] DEFAULT_COLUMNS = new QueryColumn[]{ROLE_ID, PERMISSION_ID};

    public RolePermissionTableDef() {
        super("", "sys_role_permission");
    }

    private RolePermissionTableDef(String schema, String name, String alisa) {
        super(schema, name, alisa);
    }

    public RolePermissionTableDef as(String alias) {
        String key = getNameWithSchema() + "." + alias;
        return getCache(key, k -> new RolePermissionTableDef("", "sys_role_permission", alias));
    }

}
