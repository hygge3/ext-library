package ext.library.eatpick.entity.table;

import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.table.TableDef;

import java.io.Serial;

/**
 * 用户角色关联表 表定义层。
 *
 * @author Auto Codegen By Ext
 * @since 2024-11-29
 */
public class UserRoleTableDef extends TableDef {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 用户角色关联表
     */
    public static final UserRoleTableDef USER_ROLE = new UserRoleTableDef();

    /**
     * 角色 ID
     */
    public final QueryColumn ROLE_ID = new QueryColumn(this, "role_id");

    /**
     * 用户 ID
     */
    public final QueryColumn USER_ID = new QueryColumn(this, "user_id");

    /**
     * 所有字段。
     */
    public final QueryColumn ALL_COLUMNS = new QueryColumn(this, "*");

    /**
     * 默认字段，不包含逻辑删除或者 large 等字段。
     */
    public final QueryColumn[] DEFAULT_COLUMNS = new QueryColumn[]{USER_ID, ROLE_ID};

    public UserRoleTableDef() {
        super("", "sys_user_role");
    }

    private UserRoleTableDef(String schema, String name, String alisa) {
        super(schema, name, alisa);
    }

    public UserRoleTableDef as(String alias) {
        String key = getNameWithSchema() + "." + alias;
        return getCache(key, k -> new UserRoleTableDef("", "sys_user_role", alias));
    }

}
